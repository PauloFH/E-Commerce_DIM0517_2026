package br.ufrn.imd.ecommerce.auditoria;

import br.ufrn.imd.ecommerce.auditoria.registro.MovimentoEstoque;
import br.ufrn.imd.ecommerce.auditoria.registro.MovimentoEstoque.Tipo;
import br.ufrn.imd.ecommerce.auditoria.registro.Movimentos;
import br.ufrn.imd.ecommerce.produto.EstoqueEntrou;
import br.ufrn.imd.ecommerce.produto.EstoqueSaiu;
import br.ufrn.imd.ecommerce.venda.VendaRealizada;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Registra as movimentações depois da confirmação da operação de estoque. */
@Component
@Profile("!postgres")
class AuditoriaListener {
    private static final Logger LOG = LoggerFactory.getLogger(AuditoriaListener.class);
    private final Movimentos movimentos;

    AuditoriaListener(Movimentos movimentos) {
        this.movimentos = movimentos;
    }

    @ApplicationModuleListener
    public void aoEntrar(EstoqueEntrou evento) {
        registrar(
                List.of(
                        new MovimentoEstoque(
                                Tipo.ENTRADA,
                                evento.codigo(),
                                evento.quantidade(),
                                evento.estoque(),
                                Instant.now())));
    }

    @ApplicationModuleListener
    public void aoSair(EstoqueSaiu evento) {
        registrar(
                List.of(
                        new MovimentoEstoque(
                                Tipo.SAIDA,
                                evento.codigo(),
                                evento.quantidade(),
                                evento.estoque(),
                                Instant.now())));
    }

    @ApplicationModuleListener
    public void aoVender(VendaRealizada evento) {
        Instant instante = Instant.now();
        var registros =
                evento.itens().stream()
                        .map(item ->
                                new MovimentoEstoque(
                                        Tipo.VENDA,
                                        item.codigo(),
                                        item.quantidade(),
                                        item.estoque(),
                                        instante))
                        .toList();
        registrar(registros);
    }

    /** Só anuncia registros cuja transação de auditoria foi confirmada. */
    private void registrar(List<MovimentoEstoque> registros) {
        movimentos.registrar(registros);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        registros.forEach(
                                movimento -> LOG.info("Movimento de estoque: {}", movimento));
                    }
                });
    }
}
