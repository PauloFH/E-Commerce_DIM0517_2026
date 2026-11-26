package br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta;

import java.math.BigDecimal;

/**
 * Representação HTTP de um item vendido.
 *
 * @param codigo identificador do produto
 * @param quantidade unidades vendidas
 * @param subtotal preço unitário vezes quantidade, em reais
 * @param estoque unidades que restaram do produto
 */
public record ItemVendidoResponse(
        Integer codigo, Integer quantidade, BigDecimal subtotal, Integer estoque) {}
