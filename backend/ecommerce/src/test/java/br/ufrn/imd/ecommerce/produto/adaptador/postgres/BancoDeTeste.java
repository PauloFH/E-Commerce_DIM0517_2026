package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL único da execução dos testes, iniciado sob demanda e removido pelo Testcontainers
 * quando a JVM termina. Não usa {@code disabledWithoutDocker}: sem Docker os testes falham.
 */
final class BancoDeTeste {
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18-alpine");

    private static DataSource compartilhado;

    private BancoDeTeste() {}

    static synchronized String url() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        return POSTGRES.getJdbcUrl();
    }

    static String usuario() {
        return POSTGRES.getUsername();
    }

    static String senha() {
        return POSTGRES.getPassword();
    }

    /** Pool novo, para simular outra instância da API no mesmo banco. */
    static DataSource novoDataSource(int conexoes) {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url());
        dataSource.setUsername(usuario());
        dataSource.setPassword(senha());
        dataSource.setMaximumPoolSize(conexoes);
        return dataSource;
    }

    /** Pool compartilhado pelos testes, com o schema migrado e a tabela de produto vazia. */
    static synchronized DataSource limpo() {
        if (compartilhado == null) {
            compartilhado = novoDataSource(16);
            Flyway.configure().dataSource(compartilhado).load().migrate();
        }
        limparTabela(compartilhado);
        return compartilhado;
    }

    static void limparTabela(DataSource dataSource) {
        JdbcClient.create(dataSource).sql("TRUNCATE produto").update();
    }
}
