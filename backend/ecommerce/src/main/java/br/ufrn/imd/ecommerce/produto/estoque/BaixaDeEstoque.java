package br.ufrn.imd.ecommerce.produto.estoque;

import java.util.Map;

/** API pública do módulo produto para quem precisa baixar estoque; Java puro, sem Spring. */
public interface BaixaDeEstoque {
    /**
     * Baixa todos os itens ou nenhum, atomicamente. Havendo recusa, nada é alterado e o erro
     * refere-se ao primeiro código na ordem de iteração do mapa, com inexistência prevalecendo
     * sobre falta de estoque.
     *
     * @param quantidadesPorCodigo unidades positivas por código positivo, sem nulos e sem ser vazio
     * @return preço e estoque resultante de cada código baixado, na ordem do mapa recebido
     */
    Map<Integer, ItemBaixado> baixarItens(Map<Integer, Integer> quantidadesPorCodigo);
}
