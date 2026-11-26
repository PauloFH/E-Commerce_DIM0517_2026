package br.ufrn.imd.ecommerce.venda.adaptador.api.documentacao;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class VendaOpenApiTests {
    @Autowired private MockMvc mvc;

    @Test
    void operacaoDeVendaTemSchemasERespostas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/loja/venda'].*", hasSize(1)))
                .andExpect(jsonPath("$.paths['/loja/venda'].post.summary").value("Registrar venda"))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/venda'].post.description",
                                containsString("atômica")))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/venda'].post.description",
                                containsString("não idempotente")))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/venda'].post.responses['201'].content['application/json'].schema['$ref']")
                                .value("#/components/schemas/VendaResponse"))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/venda'].post.responses['201'].content['application/json'].example.total")
                                .value(189.7))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/venda'].post.responses['201'].content['application/json'].example.itens",
                                hasSize(2)))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/venda'].post.responses['400'].content['application/problem+json'].schema['$ref']")
                                .value("#/components/schemas/Problema"))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/venda'].post.responses['404'].content['application/problem+json'].schema['$ref']")
                                .value("#/components/schemas/Problema"))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/venda'].post.requestBody.content['application/json'].schema.required",
                                contains("itens")))
                .andExpect(
                        jsonPath("$.components.schemas.VendaRequest.properties.itens.type")
                                .value("array"))
                .andExpect(
                        jsonPath("$.components.schemas.VendaRequest.properties.itens.items['$ref']")
                                .value("#/components/schemas/ItemVendaRequest"))
                .andExpect(
                        jsonPath("$.components.schemas.ItemVendaRequest.properties.*", hasSize(2)))
                .andExpect(
                        jsonPath("$.components.schemas.VendaResponse.properties.total.type")
                                .value("number"))
                .andExpect(
                        jsonPath(
                                        "$.components.schemas.VendaResponse.properties.itens.items['$ref']")
                                .value("#/components/schemas/ItemVendidoResponse"))
                .andExpect(
                        jsonPath(
                                "$.components.schemas.ItemVendidoResponse.properties.*",
                                hasSize(4)))
                .andExpect(
                        jsonPath(
                                        "$.components.schemas.ItemVendidoResponse.properties.subtotal.type")
                                .value("number"));
    }
}
