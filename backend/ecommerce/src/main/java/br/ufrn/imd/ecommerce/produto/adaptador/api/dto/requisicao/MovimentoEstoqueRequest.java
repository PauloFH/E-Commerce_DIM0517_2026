package br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao;

/**
 * Corpo HTTP de uma movimentação de estoque (entrada ou saída).
 *
 * @param quantidade unidades positivas obrigatórias a movimentar
 */
public record MovimentoEstoqueRequest(Integer quantidade) {}
