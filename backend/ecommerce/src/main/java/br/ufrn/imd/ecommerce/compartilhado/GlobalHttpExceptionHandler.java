package br.ufrn.imd.ecommerce.compartilhado;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Tratamento central de erros de negócio, transporte e validação declarativa. */
@RestControllerAdvice
public class GlobalHttpExceptionHandler {
    /**
     * Traduz a categoria de negócio sem depender de classes internas dos módulos.
     *
     * @param exception erro com chave e argumentos da mensagem
     * @param request requisição atual
     * @return problema 400, 404 ou 409 conforme a categoria
     */
    @ExceptionHandler(ExcecaoNegocio.class)
    public ProblemDetail negocio(ExcecaoNegocio exception, HttpServletRequest request) {
        HttpStatus status =
                switch (exception.getTipo()) {
                    case DADOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
                    case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
                    case CONFLITO -> HttpStatus.CONFLICT;
                };
        ProblemDetail problema = problema(status, exception.getChave(), request);
        problema.setProperty("argumentos", exception.getArgumentos());
        return problema;
    }

    /**
     * Trata corpo ausente, JSON ilegível ou tipos incompatíveis.
     *
     * @param request requisição atual
     * @return problema 400 sem detalhes do parser
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "requisicao.json.invalido", request);
    }

    /**
     * Trata parâmetros de rota incompatíveis com o tipo declarado.
     *
     * @param request requisição atual
     * @return problema 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(HttpServletRequest request) {
        return problema(HttpStatus.BAD_REQUEST, "requisicao.parametro.invalido", request);
    }

    /**
     * Apresenta campos e chaves de validação, sem publicar os valores rejeitados.
     *
     * @param exception violações encontradas pelo Validator
     * @param request requisição atual
     * @return problema 400 com extensão erros ordenada por campo e chave
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail dadosInvalidos(
            ConstraintViolationException exception, HttpServletRequest request) {
        var erros =
                exception.getConstraintViolations().stream()
                        .map(violacao ->
                                Map.of(
                                        "campo",
                                        violacao.getPropertyPath().toString(),
                                        "chave",
                                        violacao.getMessage()))
                        .sorted(
                                Comparator.comparing(
                                                (Map<String, String> erro) -> erro.get("campo"))
                                        .thenComparing(erro -> erro.get("chave")))
                        .toList();
        ProblemDetail problema =
                problema(HttpStatus.BAD_REQUEST, "requisicao.dados.invalidos", request);
        problema.setProperty("erros", erros);
        return problema;
    }

    /** Monta os campos padrão sem depender das classes internas dos módulos. */
    private ProblemDetail problema(HttpStatus status, String chave, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, chave);
        problema.setType(URI.create("about:blank"));
        problema.setInstance(URI.create(request.getRequestURI()));
        return problema;
    }
}
