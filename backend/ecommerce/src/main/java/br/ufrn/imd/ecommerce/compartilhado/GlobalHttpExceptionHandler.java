package br.ufrn.imd.ecommerce.compartilhado;

import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/** Contrato comum de erros de transporte e validação declarativa. */
@RestControllerAdvice
public class GlobalHttpExceptionHandler {
    /**
     * Trata corpo ausente, JSON ilegível ou tipos incompatíveis.
     * @param request requisição atual
     * @return problema 400 sem detalhes do parser
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpServletRequest request) {
        return problema("requisicao.json.invalido", request);
    }

    /**
     * Trata parâmetros de rota incompatíveis com o tipo declarado.
     * @param request requisição atual
     * @return problema 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail parametroInvalido(HttpServletRequest request) {
        return problema("requisicao.parametro.invalido", request);
    }

    /**
     * Apresenta campos e chaves de validação, sem publicar os valores rejeitados.
     * @param exception violações encontradas pelo Validator
     * @param request requisição atual
     * @return problema 400 com extensão erros ordenada por campo e chave
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail dadosInvalidos(ConstraintViolationException exception, HttpServletRequest request) {
        var erros = exception.getConstraintViolations().stream()
                .map(violacao -> Map.of("campo", violacao.getPropertyPath().toString(), "chave", violacao.getMessage()))
                .sorted(Comparator.comparing((Map<String, String> erro) -> erro.get("campo"))
                        .thenComparing(erro -> erro.get("chave")))
                .toList();
        ProblemDetail problema = problema("requisicao.dados.invalidos", request);
        problema.setProperty("erros", erros);
        return problema;
    }

    /** Monta os campos padrão sem depender de módulos de negócio. */
    private ProblemDetail problema(String chave, HttpServletRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, chave);
        problema.setType(URI.create("about:blank"));
        problema.setInstance(URI.create(request.getRequestURI()));
        return problema;
    }
}
