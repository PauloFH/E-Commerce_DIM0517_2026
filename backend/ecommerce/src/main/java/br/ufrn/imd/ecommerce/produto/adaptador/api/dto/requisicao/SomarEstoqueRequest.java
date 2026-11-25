package br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao;

/** Corpo HTTP da reposição.
 * @param quantidade unidades positivas obrigatórias a acrescentar */
public record SomarEstoqueRequest(Integer quantidade) {
}
