package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.math.BigDecimal;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("postgres")
class ProdutoPostgresProfileTests {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", BancoDeTeste::url);
        registry.add("spring.datasource.username", BancoDeTeste::usuario);
        registry.add("spring.datasource.password", BancoDeTeste::senha);
    }

    @Autowired private ProdutoRepository repository;
    @Autowired private Flyway flyway;

    @Test
    void profilePostgresMigraOBancoEUsaOAdaptadorPostgres() {
        assertThat(repository).isInstanceOf(ProdutoRepositoryPostgres.class);
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
        assertThat(repository.inserir(new Produto(900_001, new BigDecimal("59.90"), 0))).isTrue();
        assertThat(repository.buscar(900_001).orElseThrow().getPreco())
                .isEqualByComparingTo("59.90");
    }
}
