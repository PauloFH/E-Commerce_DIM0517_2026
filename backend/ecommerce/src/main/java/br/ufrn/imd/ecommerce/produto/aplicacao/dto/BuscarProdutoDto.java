package br.ufrn.imd.ecommerce.produto.aplicacao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Entrada da consulta de produto, independente do transporte.
 *
 * @param codigo identificador obrigatório e positivo
 */
public record BuscarProdutoDto(
        @NotNull(message = "produto.codigo.obrigatorio")
                @Positive(message = "produto.codigo.positivo")
                Integer codigo) {}
