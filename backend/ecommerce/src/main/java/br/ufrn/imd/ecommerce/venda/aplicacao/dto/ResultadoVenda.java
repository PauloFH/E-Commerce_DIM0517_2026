package br.ufrn.imd.ecommerce.venda.aplicacao.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado de uma venda concluída.
 *
 * @param total soma dos subtotais, com duas casas decimais
 * @param itens um item por código, na ordem da primeira aparição na venda
 */
public record ResultadoVenda(BigDecimal total, List<ItemVendido> itens) {}
