package br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao;

/**
 * Item do corpo HTTP de uma venda.
 *
 * @param codigo identificador positivo obrigatório do produto
 * @param quantidade unidades positivas obrigatórias a vender
 */
public record ItemVendaRequest(Integer codigo, Integer quantidade) {}
