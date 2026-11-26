package br.ufrn.imd.ecommerce.auditoria.registro;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Histórico volátil, restrito à instância atual. */
@Component
@Profile("!postgres")
public class MovimentosEmMemoria implements Movimentos {
    private final List<MovimentoEstoque> historico = new ArrayList<>();

    @Override
    public synchronized void registrar(List<MovimentoEstoque> movimentos) {
        historico.addAll(movimentos);
    }

    public synchronized List<MovimentoEstoque> consultar() {
        return List.copyOf(historico);
    }
}
