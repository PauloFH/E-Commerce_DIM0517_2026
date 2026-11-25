package br.ufrn.imd.ecommerce.produto.dominio.excecao;

import java.util.List;

/** Código de produto já cadastrado; a apresentação da mensagem pertence à API. */
public final class ProdutoJaExisteException extends RuntimeException {
    private final String chave;
    private final List<Object> argumentos;

    /**
     * Registra uma mensagem sem tradução ou informação de transporte.
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    public ProdutoJaExisteException(String chave, Object... argumentos) {
        super(chave);
        this.chave = chave;
        this.argumentos = List.of(argumentos);
    }

    /** @return chave da mensagem */
    public String getChave() {
        return chave;
    }

    /** @return argumentos imutáveis da mensagem */
    public List<Object> getArgumentos() {
        return argumentos;
    }
}
