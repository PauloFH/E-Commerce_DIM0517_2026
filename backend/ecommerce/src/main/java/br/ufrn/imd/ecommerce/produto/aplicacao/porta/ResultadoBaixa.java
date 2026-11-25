package br.ufrn.imd.ecommerce.produto.aplicacao.porta;

import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.util.Map;

/** Resultado de uma baixa de estoque: sucesso ou o primeiro motivo da recusa. */
public sealed interface ResultadoBaixa {
    /**
     * Baixa aplicada em todos os itens.
     *
     * @param produtos produtos com o estoque resultante, um por código baixado; imutável
     */
    record Baixado(Map<Integer, Produto> produtos) implements ResultadoBaixa {}

    /**
     * Nada foi baixado porque o produto não existe.
     *
     * @param codigo primeiro código inexistente
     */
    record ProdutoInexistente(Integer codigo) implements ResultadoBaixa {}

    /**
     * Nada foi baixado porque faltou estoque.
     *
     * @param codigo primeiro código sem estoque suficiente
     * @param disponivel unidades disponíveis desse produto
     */
    record EstoqueInsuficiente(Integer codigo, int disponivel) implements ResultadoBaixa {}
}
