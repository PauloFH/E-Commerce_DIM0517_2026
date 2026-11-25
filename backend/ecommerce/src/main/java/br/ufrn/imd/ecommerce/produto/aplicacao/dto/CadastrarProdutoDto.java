package br.ufrn.imd.ecommerce.produto.aplicacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Entrada do cadastro, com restrições verificadas pelo serviço.
 * @param codigo identificador obrigatório e positivo
 * @param preco preço obrigatório e positivo em reais
 */
public record CadastrarProdutoDto(
        @NotNull(message = "produto.codigo.obrigatorio")
        @Positive(message = "produto.codigo.positivo") Integer codigo,
        @NotNull(message = "produto.preco.obrigatorio")
        @Positive(message = "produto.preco.positivo") BigDecimal preco) {
}
