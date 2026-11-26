package br.ufrn.imd.ecommerce.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import br.ufrn.imd.ecommerce.EcommerceApplication;
import br.ufrn.imd.ecommerce.produto.adaptador.api.ProdutoApi;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.MovimentoEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.venda.adaptador.api.VendaApi;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.ItemVendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.VendaRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

class AuditoriaPostgresTests {
    @Test
    void reprocessaVendaPendenteAoReiniciarSemDesfazerAOperacao() {
        try (var banco = new PostgreSQLContainer("postgres:18-alpine")) {
            banco.start();

            try (var aplicacao = iniciar(banco)) {
                var produtos = aplicacao.getBean(ProdutoApi.class);
                var vendas = aplicacao.getBean(VendaApi.class);
                var jdbc = aplicacao.getBean(JdbcClient.class);

                produtos.cadastrar(new CadastrarProdutoRequest(1, BigDecimal.ONE));
                produtos.cadastrar(new CadastrarProdutoRequest(2, BigDecimal.ONE));
                produtos.entrada(1, new MovimentoEstoqueRequest(10));
                produtos.entrada(2, new MovimentoEstoqueRequest(7));
                produtos.saida(1, new MovimentoEstoqueRequest(2));
                await().untilAsserted(() ->
                        assertThat(
                                        contar(
                                                jdbc,
                                                """
                                                SELECT count(*) FROM event_publication WHERE status = 'COMPLETED'
                                                """))
                                .isEqualTo(3));

                assertThat(movimentos(jdbc))
                        .containsExactlyInAnyOrder(
                                "ENTRADA:1:10:10", "ENTRADA:2:7:7", "SAIDA:1:2:8");

                var transacao =
                        new TransactionTemplate(
                                aplicacao.getBean(PlatformTransactionManager.class));
                transacao.executeWithoutResult(status -> {
                    produtos.entrada(1, new MovimentoEstoqueRequest(1));
                    status.setRollbackOnly();
                });

                assertThat(produtos.consultar(1).getBody().estoque()).isEqualTo(8);
                assertThat(contar(jdbc, "SELECT count(*) FROM event_publication")).isEqualTo(3);

                var pedidoInvalido = new VendaRequest(List.of(new ItemVendaRequest(1, 9)));
                assertThatThrownBy(() -> vendas.vender(pedidoInvalido))
                        .isInstanceOf(DadosInvalidosException.class);
                assertThat(contar(jdbc, "SELECT count(*) FROM event_publication")).isEqualTo(3);

                jdbc.sql(
                                """
                                ALTER TABLE movimento_estoque ADD CONSTRAINT falha_no_segundo_item
                                CHECK (tipo <> 'VENDA' OR codigo <> 2)
                                """)
                        .update();

                var pedido =
                        new VendaRequest(
                                List.of(new ItemVendaRequest(1, 3), new ItemVendaRequest(2, 4)));
                assertThat(vendas.vender(pedido).getStatusCode()).isEqualTo(HttpStatus.CREATED);

                await().untilAsserted(() ->
                        assertThat(
                                        contar(
                                                jdbc,
                                                """
                                                SELECT count(*) FROM event_publication
                                                WHERE status = 'FAILED' AND completion_date IS NULL
                                                """))
                                .isOne());
                assertThat(produtos.consultar(1).getBody().estoque()).isEqualTo(5);
                assertThat(produtos.consultar(2).getBody().estoque()).isEqualTo(3);
                assertThat(movimentos(jdbc)).hasSize(3);

                jdbc.sql("ALTER TABLE movimento_estoque DROP CONSTRAINT falha_no_segundo_item")
                        .update();
            }

            try (var aplicacao = iniciar(banco)) {
                var jdbc = aplicacao.getBean(JdbcClient.class);

                await().untilAsserted(() ->
                        assertThat(
                                        contar(
                                                jdbc,
                                                """
                                                SELECT count(*) FROM event_publication
                                                WHERE status = 'COMPLETED' AND completion_attempts = 2
                                                """))
                                .isOne());
                assertThat(movimentos(jdbc))
                        .containsExactlyInAnyOrder(
                                "ENTRADA:1:10:10",
                                "ENTRADA:2:7:7",
                                "SAIDA:1:2:8",
                                "VENDA:1:3:5",
                                "VENDA:2:4:3");
            }
        }
    }

    private ConfigurableApplicationContext iniciar(PostgreSQLContainer banco) {
        return new SpringApplicationBuilder(EcommerceApplication.class)
                .profiles("postgres")
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.datasource.url=" + banco.getJdbcUrl(),
                        "--spring.datasource.username=" + banco.getUsername(),
                        "--spring.datasource.password=" + banco.getPassword());
    }

    private long contar(JdbcClient jdbc, String consulta) {
        return jdbc.sql(consulta).query(Long.class).single();
    }

    private List<String> movimentos(JdbcClient jdbc) {
        return jdbc.sql(
                        """
                        SELECT concat_ws(':', tipo, codigo, quantidade, estoque_resultante)
                        FROM movimento_estoque
                        """)
                .query(String.class)
                .list();
    }
}
