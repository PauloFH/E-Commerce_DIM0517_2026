package br.ufrn.imd.ecommerce.compartilhado;

import java.util.List;

/** Contrato público de erros de negócio, independente de Spring e de HTTP. */
public abstract class ExcecaoNegocio extends RuntimeException {
    /** Categorias de negócio cuja apresentação pertence ao adaptador de entrada. */
    public enum Tipo {
        /** Dados incompatíveis com o caso de uso. */
        DADOS_INVALIDOS,
        /** Recurso solicitado não existe. */
        NAO_ENCONTRADO,
        /** Operação conflita com o estado atual. */
        CONFLITO
    }

    private final Tipo tipo;
    private final String chave;
    private final List<Object> argumentos;

    /**
     * Registra a categoria e a mensagem sem traduzir ou escolher um status HTTP.
     *
     * @param tipo categoria do erro de negócio
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    protected ExcecaoNegocio(Tipo tipo, String chave, Object... argumentos) {
        super(chave);
        this.tipo = tipo;
        this.chave = chave;
        this.argumentos = List.of(argumentos);
    }

    /**
     * @return categoria do erro de negócio
     */
    public Tipo getTipo() {
        return tipo;
    }

    /**
     * @return chave da mensagem
     */
    public String getChave() {
        return chave;
    }

    /**
     * @return argumentos imutáveis da mensagem
     */
    public List<Object> getArgumentos() {
        return argumentos;
    }
}
