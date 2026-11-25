package br.ufrn.imd.ecommerce.produto.adaptador.api;

import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.SomarEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.EstoqueDto;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.ProdutoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Contrato HTTP e documentação OpenAPI, separados da implementação do controller. */
@Tag(name = "Produtos", description = "Cadastro, consulta e reposição de estoque")
@RequestMapping("/loja/produto")
public interface ProdutoApi {
    /**
     * Cadastra com estoque zero, sem sobrescrever códigos existentes.
     * @param request código positivo e preço positivo em reais, ambos obrigatórios
     * @return 201 com código, preço e estoque zero do produto criado; 400 se inválido ou 409 se duplicado
     */
    @Operation(summary = "Cadastrar produto", description = "Código inteiro positivo e preço positivo em reais, ambos obrigatórios. O estoque inicial é sempre zero.")
    @ApiResponse(responseCode = "201", description = "Produto criado com estoque zero",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoDto.class),
                    examples = @ExampleObject(value = "{\"codigo\":123,\"preco\":59.90,\"estoque\":0}")))
    @ApiResponse(responseCode = "400", description = "Entrada inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Código já cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<ProdutoDto> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CadastrarProdutoRequest.class,
                            requiredProperties = {"codigo", "preco"}),
                            examples = @ExampleObject(value = "{\"codigo\":123,\"preco\":59.90}")))
            @RequestBody CadastrarProdutoRequest request);

    /**
     * Consulta o estoque pelo identificador, sem expor o preço.
     * @param codigo identificador inteiro positivo
     * @return 200 com código e estoque; 400 se inválido ou 404 se ausente
     */
    @Operation(summary = "Consultar estoque", description = "Retorna somente código e estoque em unidades inteiras, sem preço.")
    @ApiResponse(responseCode = "200", description = "Estoque encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstoqueDto.class),
            examples = @ExampleObject(value = "{\"codigo\":123,\"estoque\":15}")))
    @ApiResponse(responseCode = "400", description = "Código inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping(value = "/{codigo}/estoque", produces = "application/json")
    ResponseEntity<EstoqueDto> consultar(
            @Parameter(description = "Identificador inteiro positivo", example = "1", schema = @Schema(type = "integer", format = "int32", minimum = "1"))
            @PathVariable("codigo") Integer codigo);

    /**
     * Acrescenta unidades ao estoque; repetir a requisição acrescenta novamente.
     * @param codigo identificador inteiro positivo
     * @param request quantidade inteira positiva obrigatória
     * @return 200 com código e novo estoque; 400 se inválido ou exceder o limite inteiro; 404 se ausente
     */
    @Operation(summary = "Repor estoque", description = "Quantidade inteira positiva obrigatória. Operação não idempotente: cada chamada soma novamente. Estoque limitado a 2147483647 unidades.")
    @ApiResponse(responseCode = "200", description = "Estoque atualizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstoqueDto.class),
            examples = @ExampleObject(value = "{\"codigo\":123,\"estoque\":25}")))
    @ApiResponse(responseCode = "400", description = "Entrada inválida ou limite de estoque excedido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PutMapping(value = "/{codigo}/entrada", consumes = "application/json", produces = "application/json")
    ResponseEntity<EstoqueDto> entrada(
            @Parameter(description = "Identificador inteiro positivo", example = "1", schema = @Schema(type = "integer", format = "int32", minimum = "1"))
            @PathVariable("codigo") Integer codigo,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SomarEstoqueRequest.class,
                            requiredProperties = {"quantidade"}), examples = @ExampleObject(value = "{\"quantidade\":10}")))
            @RequestBody SomarEstoqueRequest request);
}
