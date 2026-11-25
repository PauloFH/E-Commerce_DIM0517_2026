package br.ufrn.imd.ecommerce.venda.aplicacao.dto;

import java.math.BigDecimal;

/**
 * Item de uma venda concluída, agrupado por código.
 *
 * @param codigo identificador do produto
 * @param quantidade unidades vendidas, somando as repetições do código
 * @param subtotal preço unitário vezes quantidade, com duas casas decimais
 * @param estoque unidades que restaram do produto
 */
public record ItemVendido(
        Integer codigo, Integer quantidade, BigDecimal subtotal, Integer estoque) {}
