package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.produto.adaptador.ProdutoRepositoryContrato;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

class ProdutoRepositoryPostgresTests extends ProdutoRepositoryContrato {
    @Override
    protected ProdutoRepository criarRepository() {
        return new ProdutoRepositoryPostgres(BancoDeTeste.limpo());
    }

    @Test
    void tabelaTemSomenteCodigoPrecoEEstoque() {
        var colunas =
                JdbcClient.create(BancoDeTeste.limpo())
                        .sql(
                                "SELECT column_name FROM information_schema.columns"
                                        + " WHERE table_name = 'produto'")
                        .query(String.class)
                        .list();

        assertThat(colunas).containsExactlyInAnyOrder("codigo", "preco", "estoque");
    }
}
