package br.ufrn.imd.ecommerce.produto.adaptador.api.documentacao;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoOpenApiTests {
    @Autowired private MockMvc mvc;

    @Test
    void interfacePublicaAsTresOperacoesComSchemasERespostas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.*.*", hasSize(3)))
                .andExpect(
                        jsonPath("$.paths['/loja/produto'].post.summary")
                                .value("Cadastrar produto"))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/produto'].post.responses['201'].content['application/json'].schema['$ref']")
                                .value("#/components/schemas/ProdutoDto"))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/produto'].post.responses['400'].content['application/problem+json']")
                                .exists())
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/produto'].post.responses['409'].content['application/problem+json'].schema['$ref']")
                                .value("#/components/schemas/Problema"))
                .andExpect(
                        jsonPath("$.components.schemas.Problema.properties.argumentos.type")
                                .value("array"))
                .andExpect(
                        jsonPath("$.components.schemas.Problema.properties.erros.items['$ref']")
                                .value("#/components/schemas/ErroCampo"))
                .andExpect(jsonPath("$.components.schemas.ErroCampo.properties.*", hasSize(2)))
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/produto/{codigo}/estoque'].get.responses['200'].content['application/json']")
                                .exists())
                .andExpect(
                        jsonPath("$.paths['/loja/produto/{codigo}/estoque'].get.responses['404']")
                                .exists())
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/produto/{codigo}/entrada'].put.description",
                                containsString("não idempotente")))
                .andExpect(
                        jsonPath("$.paths['/loja/produto/{codigo}/entrada'].put.responses['400']")
                                .exists())
                .andExpect(
                        jsonPath("$.paths['/loja/produto/{codigo}/entrada'].put.responses['404']")
                                .exists())
                .andExpect(
                        jsonPath("$.components.schemas.ProdutoDto.properties.codigo.type")
                                .value("integer"))
                .andExpect(
                        jsonPath("$.components.schemas.ProdutoDto.properties.preco.type")
                                .value("number"))
                .andExpect(
                        jsonPath("$.components.schemas.ProdutoDto.properties.estoque.type")
                                .value("integer"))
                .andExpect(jsonPath("$.components.schemas.EstoqueDto.properties.*", hasSize(2)))
                .andExpect(
                        jsonPath("$.components.schemas.EstoqueDto.properties.codigo.type")
                                .value("integer"))
                .andExpect(
                        jsonPath("$.components.schemas.CadastrarProdutoRequest.properties.estoque")
                                .doesNotExist())
                .andExpect(
                        jsonPath(
                                        "$.paths['/loja/produto/{codigo}/estoque'].get.responses['200'].content['application/json'].schema['$ref']")
                                .value("#/components/schemas/EstoqueDto"))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/produto'].post.requestBody.content['application/json'].schema.required",
                                containsInAnyOrder("codigo", "preco")))
                .andExpect(
                        jsonPath(
                                "$.paths['/loja/produto/{codigo}/entrada'].put.requestBody.content['application/json'].schema.required",
                                contains("quantidade")));
    }

    @Test
    void swaggerUiEstaDisponivel() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
