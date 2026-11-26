package br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representação HTTP de uma venda concluída.
 *
 * @param total soma dos subtotais, em reais
 * @param itens um item por código vendido
 */
public record VendaResponse(BigDecimal total, List<ItemVendidoResponse> itens) {}
