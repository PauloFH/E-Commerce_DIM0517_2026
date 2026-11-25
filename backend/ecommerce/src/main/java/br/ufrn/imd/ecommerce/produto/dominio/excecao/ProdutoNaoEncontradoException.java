package br.ufrn.imd.ecommerce.produto.dominio.excecao;

import br.ufrn.imd.ecommerce.compartilhado.ExcecaoNegocio;

/** Produto solicitado não existe; a apresentação da mensagem pertence à API. */
public final class ProdutoNaoEncontradoException extends ExcecaoNegocio {
    /**
     * Registra uma mensagem sem tradução ou informação de transporte.
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    public ProdutoNaoEncontradoException(String chave, Object... argumentos) {
        super(Tipo.NAO_ENCONTRADO, chave, argumentos);
    }
}
