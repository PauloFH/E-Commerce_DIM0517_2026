package br.ufrn.imd.ecommerce.produto.aplicacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entrada de uma movimentação de estoque (entrada ou saída).
 *
 * @param codigo identificador obrigatório e positivo
 * @param quantidade unidades obrigatórias e positivas a movimentar
 */
public record MovimentoEstoqueDto(
        @NotNull(message = "produto.codigo.obrigatorio")
                @Positive(message = "produto.codigo.positivo")
                Integer codigo,
        @NotNull(message = "produto.quantidade.obrigatoria")
                @Positive(message = "produto.quantidade.positiva")
                Integer quantidade) {}
