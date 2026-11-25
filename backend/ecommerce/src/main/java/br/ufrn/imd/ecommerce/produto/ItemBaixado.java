package br.ufrn.imd.ecommerce.produto;

import java.math.BigDecimal;

/**
 * Situação de um produto depois de uma baixa de estoque, exposta aos outros módulos.
 *
 * @param preco preço unitário em reais
 * @param estoque unidades disponíveis após a baixa
 */
public record ItemBaixado(BigDecimal preco, int estoque) {}
