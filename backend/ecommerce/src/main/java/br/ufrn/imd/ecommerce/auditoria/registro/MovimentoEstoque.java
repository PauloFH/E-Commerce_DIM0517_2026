package br.ufrn.imd.ecommerce.auditoria.registro;

import java.time.Instant;

/** Registro de unidades movimentadas e do saldo após a operação; instante do registro em UTC. */
public record MovimentoEstoque(
        Tipo tipo, int codigo, int quantidade, int estoqueResultante, Instant instante) {
    public enum Tipo {
        ENTRADA,
        SAIDA,
        VENDA
    }
}
