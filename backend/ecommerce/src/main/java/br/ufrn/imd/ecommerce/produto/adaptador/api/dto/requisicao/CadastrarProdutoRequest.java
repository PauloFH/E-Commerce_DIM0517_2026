package br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao;

import java.math.BigDecimal;

/**
 * Corpo HTTP do cadastro; restrições são executadas nos DTOs da aplicação.
 *
 * @param codigo identificador positivo obrigatório
 * @param preco preço positivo obrigatório em reais
 */
public record CadastrarProdutoRequest(Integer codigo, BigDecimal preco) {}
