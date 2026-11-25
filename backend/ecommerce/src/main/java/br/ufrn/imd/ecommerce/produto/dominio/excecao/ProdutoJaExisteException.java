package br.ufrn.imd.ecommerce.produto.dominio.excecao;

import br.ufrn.imd.ecommerce.compartilhado.ExcecaoNegocio;

/** Código de produto já cadastrado; a apresentação da mensagem pertence à API. */
public final class ProdutoJaExisteException extends ExcecaoNegocio {
    /**
     * Registra uma mensagem sem tradução ou informação de transporte.
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    public ProdutoJaExisteException(String chave, Object... argumentos) {
        super(Tipo.CONFLITO, chave, argumentos);
    }
}
