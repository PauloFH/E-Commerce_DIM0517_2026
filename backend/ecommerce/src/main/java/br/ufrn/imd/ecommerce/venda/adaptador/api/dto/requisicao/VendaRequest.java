package br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao;

import java.util.List;

/**
 * Corpo HTTP de uma venda.
 *
 * @param itens um ou mais itens obrigatórios
 */
public record VendaRequest(List<ItemVendaRequest> itens) {}
