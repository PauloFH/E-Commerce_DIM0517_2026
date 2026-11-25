package br.ufrn.imd.ecommerce.produto.dominio.excecao;

import java.util.List;

/** Dados incompatíveis com o contrato da aplicação; a apresentação da mensagem pertence à API. */
public final class DadosInvalidosException extends RuntimeException {
    private final String chave;
    private final List<Object> argumentos;

    /**
     * Registra uma mensagem sem tradução ou informação de transporte.
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    public DadosInvalidosException(String chave, Object... argumentos) {
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
