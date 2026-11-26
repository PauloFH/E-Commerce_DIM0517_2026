package br.ufrn.imd.ecommerce.venda.dominio.excecao;

import br.ufrn.imd.ecommerce.compartilhado.ExcecaoNegocio;

/** Venda incompatível com o contrato da aplicação; a apresentação da mensagem pertence à API. */
public final class VendaInvalidaException extends ExcecaoNegocio {
    /**
     * Registra uma mensagem sem tradução ou informação de transporte.
     *
     * @param chave chave da mensagem
     * @param argumentos valores usados na apresentação da mensagem
     */
    public VendaInvalidaException(String chave, Object... argumentos) {
        super(Tipo.DADOS_INVALIDOS, chave, argumentos);
    }
}
