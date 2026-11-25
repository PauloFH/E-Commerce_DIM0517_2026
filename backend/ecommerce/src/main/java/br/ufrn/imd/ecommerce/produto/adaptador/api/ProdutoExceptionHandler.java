package br.ufrn.imd.ecommerce.produto.adaptador.api;

import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoJaExisteException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Tradução HTTP das exceções internas do módulo produto. */
@RestControllerAdvice(assignableTypes = ProdutoController.class)
public class ProdutoExceptionHandler {
    /**
     * Apresenta a ausência de produto.
     * @param exception erro de negócio
     * @param request requisição atual
     * @return problema 404
     */
    @ExceptionHandler(ProdutoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(ProdutoNaoEncontradoException exception, HttpServletRequest request) {
        return problema(HttpStatus.NOT_FOUND, exception.getChave(), exception.getArgumentos(), request);
    }

    /**
     * Apresenta a duplicação de código.
     * @param exception erro de negócio
     * @param request requisição atual
     * @return problema 409
     */
    @ExceptionHandler(ProdutoJaExisteException.class)
    public ProblemDetail jaExiste(ProdutoJaExisteException exception, HttpServletRequest request) {
        return problema(HttpStatus.CONFLICT, exception.getChave(), exception.getArgumentos(), request);
    }

    /**
     * Apresenta uma entrada incompatível com o caso de uso.
     * @param exception erro de negócio
     * @param request requisição atual
     * @return problema 400
     */
    @ExceptionHandler(DadosInvalidosException.class)
    public ProblemDetail dadosInvalidos(DadosInvalidosException exception, HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, exception.getChave(), exception.getArgumentos(), request);
    }

    /** Monta o contrato de erro sem expor detalhes internos da exceção. */
    private ProblemDetail problema(HttpStatus status, String chave, List<Object> argumentos, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, chave);
        problema.setType(URI.create("about:blank"));
        problema.setInstance(URI.create(request.getRequestURI()));
        problema.setProperty("argumentos", argumentos);
        return problema;
    }
}
