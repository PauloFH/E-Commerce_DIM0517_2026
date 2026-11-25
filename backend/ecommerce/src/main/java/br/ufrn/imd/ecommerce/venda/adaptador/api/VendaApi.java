package br.ufrn.imd.ecommerce.venda.adaptador.api;

import br.ufrn.imd.ecommerce.compartilhado.Problema;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.VendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.VendaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** Contrato HTTP e documentação OpenAPI, separados da implementação do controller. */
@Tag(name = "Vendas", description = "Registro de vendas com baixa de estoque")
@RequestMapping("/loja/venda")
public interface VendaApi {
    /**
     * Registra uma venda: baixa o estoque de cada produto e calcula o total.
     *
     * @param request um ou mais itens, cada um com código e quantidade positivos
     * @return 201 com total e itens vendidos; 400 se inválida ou sem estoque; 404 se algum produto
     *     não existir
     */
    @Operation(
            summary = "Registrar venda",
            description =
                    "Recebe um ou mais itens com código e quantidade positivos, baixa o estoque de cada produto e calcula o total. A venda é atômica: se algum item for inválido, inexistente ou sem estoque, nenhum estoque muda. Itens com o mesmo código têm as quantidades somadas e aparecem uma única vez na resposta. Operação não idempotente: cada chamada bem-sucedida baixa o estoque novamente.")
    @ApiResponse(
            responseCode = "201",
            description = "Venda registrada",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VendaResponse.class),
                            examples =
                                    @ExampleObject(
                                            value =
                                                    "{\"total\":189.70,\"itens\":[{\"codigo\":123,\"quantidade\":2,\"subtotal\":119.80,\"estoque\":13},{\"codigo\":456,\"quantidade\":1,\"subtotal\":69.90,\"estoque\":7}]}")))
    @ApiResponse(
            responseCode = "400",
            description = "Lista de itens ausente ou vazia, item inválido ou estoque insuficiente",
            content =
                    @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problema.class),
                            examples =
                                    @ExampleObject(
                                            value =
                                                    "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"produto.estoque.insuficiente\",\"instance\":\"/loja/venda\",\"argumentos\":[3,123]}")))
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content =
                    @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = Problema.class),
                            examples =
                                    @ExampleObject(
                                            value =
                                                    "{\"type\":\"about:blank\",\"title\":\"Not Found\",\"status\":404,\"detail\":\"produto.nao.encontrado\",\"instance\":\"/loja/venda\",\"argumentos\":[999]}")))
    @PostMapping(consumes = "application/json", produces = "application/json")
    ResponseEntity<VendaResponse> vender(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content =
                                    @Content(
                                            mediaType = "application/json",
                                            schema =
                                                    @Schema(
                                                            implementation = VendaRequest.class,
                                                            requiredProperties = {"itens"}),
                                            examples =
                                                    @ExampleObject(
                                                            value =
                                                                    "{\"itens\":[{\"codigo\":123,\"quantidade\":2},{\"codigo\":456,\"quantidade\":1}]}")))
                    @RequestBody
                    VendaRequest request);
}
