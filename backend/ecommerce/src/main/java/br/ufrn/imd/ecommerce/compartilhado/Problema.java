package br.ufrn.imd.ecommerce.compartilhado;

import java.net.URI;
import java.util.List;

/**
 * Formato dos erros publicado no OpenAPI: o ProblemDetail com as extensões do handler global.
 *
 * @param type sempre about:blank
 * @param title descrição traduzida do status
 * @param status código HTTP
 * @param detail mensagem traduzida
 * @param codigoErro chave estável da mensagem, para o cliente reagir sem depender do texto
 * @param instance caminho da requisição
 * @param argumentos valores da mensagem, presentes em erros de negócio
 * @param erros campos rejeitados, presentes em erros de validação
 */
public record Problema(
        URI type,
        String title,
        int status,
        String detail,
        String codigoErro,
        URI instance,
        List<Object> argumentos,
        List<ErroCampo> erros) {

    /**
     * Campo rejeitado pela validação, sem o valor recebido.
     *
     * @param campo caminho do campo
     * @param chave chave estável da mensagem
     * @param mensagem mensagem traduzida
     */
    public record ErroCampo(String campo, String chave, String mensagem) {}
}
