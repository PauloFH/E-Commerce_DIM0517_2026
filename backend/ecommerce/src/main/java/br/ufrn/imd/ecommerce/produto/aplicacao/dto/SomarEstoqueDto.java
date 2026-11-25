package br.ufrn.imd.ecommerce.produto.aplicacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entrada da reposição de estoque.
 *
 * @param codigo identificador obrigatório e positivo
 * @param quantidade unidades obrigatórias e positivas a acrescentar
 */
public record SomarEstoqueDto(
        @NotNull(message = "produto.codigo.obrigatorio")
                @Positive(message = "produto.codigo.positivo")
                Integer codigo,
        @NotNull(message = "produto.quantidade.obrigatoria")
                @Positive(message = "produto.quantidade.positiva")
                Integer quantidade) {}
