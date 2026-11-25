package br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta;

import java.math.BigDecimal;

/** Representação HTTP de um produto.
 * @param codigo identificador
 * @param preco preço em reais
 * @param estoque unidades disponíveis */
public record ProdutoDto(Integer codigo, BigDecimal preco, Integer estoque) {
}
