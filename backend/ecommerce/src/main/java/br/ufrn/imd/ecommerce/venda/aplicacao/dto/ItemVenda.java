package br.ufrn.imd.ecommerce.venda.aplicacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Item solicitado em uma venda.
 *
 * @param codigo identificador obrigatório e positivo do produto
 * @param quantidade unidades obrigatórias e positivas a vender
 */
public record ItemVenda(
        @NotNull(message = "produto.codigo.obrigatorio")
                @Positive(message = "produto.codigo.positivo")
                Integer codigo,
        @NotNull(message = "produto.quantidade.obrigatoria")
                @Positive(message = "produto.quantidade.positiva")
                Integer quantidade) {}
