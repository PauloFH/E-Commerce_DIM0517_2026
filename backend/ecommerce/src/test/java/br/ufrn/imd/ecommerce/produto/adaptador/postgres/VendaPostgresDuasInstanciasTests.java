package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.compartilhado.ExcecaoNegocio;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVenda;
import br.ufrn.imd.ecommerce.venda.aplicacao.servico.VendaService;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Duas instâncias da API, cada uma com seu pool e seus serviços, vendendo o mesmo estoque. */
class VendaPostgresDuasInstanciasTests {
    private static final int THREADS = 16;
    private static final int VENDAS = 50;
    private static final int ESTOQUE_INICIAL = 300;

    private final List<HikariDataSource> pools =
            List.of(BancoDeTeste.novoDataSource(THREADS), BancoDeTeste.novoDataSource(THREADS));

    @AfterEach
    void fecharPools() {
        pools.forEach(HikariDataSource::close);
    }

    @Test
    @Timeout(60)
    void vendasEmInstanciasDiferentesNaoDeixamEstoqueNegativoNemPerdemUnidades() throws Exception {
        BancoDeTeste.limpo();
        var repositorios = pools.stream().map(ProdutoRepositoryPostgres::new).toList();
        repositorios.getFirst().inserir(new Produto(1, BigDecimal.ONE, ESTOQUE_INICIAL));
        repositorios.getFirst().inserir(new Produto(2, BigDecimal.TEN, ESTOQUE_INICIAL));
        var vendas =
                repositorios.stream()
                        .map(repositorio -> new VendaService(new ProdutoService(repositorio)))
                        .toList();

        // ordens opostas e código repetido na mesma venda
        var pedidos =
                List.of(
                        List.of(new ItemVenda(1, 1), new ItemVenda(2, 1)),
                        List.of(new ItemVenda(2, 1), new ItemVenda(1, 1)),
                        List.of(new ItemVenda(1, 1), new ItemVenda(2, 2), new ItemVenda(1, 1)));
        Map<Integer, AtomicInteger> vendidos =
                new ConcurrentHashMap<>(Map.of(1, new AtomicInteger(), 2, new AtomicInteger()));
        var recusadas = new AtomicInteger();

        var inicio = new CountDownLatch(1);
        List<Callable<Void>> tarefas = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            var venda = vendas.get(t % 2);
            var pedido = pedidos.get(t % pedidos.size());
            tarefas.add(() -> {
                inicio.await();
                for (int i = 0; i < VENDAS; i++) {
                    try {
                        venda.vender(pedido);
                        // conta o pedido, não a resposta: um item repetido descartado apareceria
                        for (ItemVenda item : pedido) {
                            vendidos.get(item.codigo()).addAndGet(item.quantidade());
                        }
                    } catch (ExcecaoNegocio recusa) {
                        recusadas.incrementAndGet();
                    }
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(THREADS)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            inicio.countDown();
            for (var resultado : resultados) {
                resultado.get(50, TimeUnit.SECONDS);
            }
        }

        for (var repositorio : repositorios) {
            for (int codigo : List.of(1, 2)) {
                assertThat(repositorio.buscar(codigo).orElseThrow().getEstoque())
                        .isEqualTo(ESTOQUE_INICIAL - vendidos.get(codigo).get())
                        .isNotNegative();
            }
        }
        assertThat(vendidos.get(1).get()).isPositive();
        assertThat(vendidos.get(2).get()).isPositive();
        // a procura passa do estoque: parte das vendas tem de ser recusada sem baixar nada
        assertThat(recusadas.get()).isPositive();
    }
}
