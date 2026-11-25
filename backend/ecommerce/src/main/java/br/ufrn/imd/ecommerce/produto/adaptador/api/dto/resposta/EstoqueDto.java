package br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta;

/**
 * Resultado HTTP de uma consulta ou movimentação de estoque.
 *
 * @param codigo identificador do produto
 * @param estoque unidades disponíveis no resultado da operação
 */
public record EstoqueDto(Integer codigo, Integer estoque) {}
