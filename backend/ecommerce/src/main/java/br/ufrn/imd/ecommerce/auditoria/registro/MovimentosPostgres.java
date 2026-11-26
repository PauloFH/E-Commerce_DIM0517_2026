package br.ufrn.imd.ecommerce.auditoria.registro;

import java.time.ZoneOffset;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Persiste os movimentos na transação do listener que recebeu o evento. */
@Component
@Profile("postgres")
class MovimentosPostgres implements Movimentos {
    private final JdbcClient jdbc;

    MovimentosPostgres(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void registrar(List<MovimentoEstoque> movimentos) {
        for (MovimentoEstoque movimento : movimentos) {
            jdbc.sql(
                            """
                            INSERT INTO movimento_estoque
                                (tipo, codigo, quantidade, estoque_resultante, instante)
                            VALUES (?, ?, ?, ?, ?)
                            """)
                    .params(
                            movimento.tipo().name(),
                            movimento.codigo(),
                            movimento.quantidade(),
                            movimento.estoqueResultante(),
                            movimento.instante().atOffset(ZoneOffset.UTC))
                    .update();
        }
    }
}
