package br.ufrn.imd.ecommerce.venda;

import java.math.BigDecimal;
import java.util.List;

/** Venda concluída e seus itens agrupados por produto. */
public record VendaRealizada(BigDecimal total, List<Item> itens) {
    public VendaRealizada {
        itens = List.copyOf(itens);
    }

    /** Dados de estoque de um item vendido. */
    public record Item(Integer codigo, Integer quantidade, Integer estoque) {}
}
