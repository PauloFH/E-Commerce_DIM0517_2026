package br.ufrn.imd.ecommerce.produto.adaptador.api.controller;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProdutoApiTests {
    private static final AtomicInteger CODIGOS = new AtomicInteger(1000);
    @Autowired private MockMvc mvc;

    @Test
    void cadastraConsultaERepoeComContratoHttp() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 15);
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.preco").doesNotExist())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$.estoque").value(15));
        mvc.perform(put("/loja/produto/{codigo}/entrada", codigo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.estoque").value(25))
                .andExpect(jsonPath("$.*", hasSize(2)));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo)).andExpect(jsonPath("$.estoque").value(25));
    }

    @Test
    void duplicacaoPreservaProdutoERetorna409() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 5);
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":" + codigo + ",\"preco\":99}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("produto.ja.existe"))
                .andExpect(jsonPath("$.argumentos[0]").value(codigo));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo)).andExpect(jsonPath("$.estoque").value(5));
    }

    @Test
    void produtoAusenteRetorna404() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("produto.nao.encontrado"))
                .andExpect(jsonPath("$.instance").value("/loja/produto/" + codigo + "/estoque"));
        mvc.perform(put("/loja/produto/{codigo}/entrada", codigo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":1}"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.5", "\"10\"", "\"abc\"", "true", "2147483648", "\"\""})
    void rejeitaCoercaoDeInteiros(String valor) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":" + valor + ",\"preco\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
        mvc.perform(put("/loja/produto/1/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":" + valor + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"59.90\"", "\"abc\"", "true", "\"\""})
    void rejeitaCoercaoDoPreco(String valor) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":" + CODIGOS.incrementAndGet() + ",\"preco\":" + valor + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "[]"})
    void rejeitaCorpoAusenteNuloOuMalformado(String corpo) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/loja/produto"));
        mvc.perform(put("/loja/produto/1/entrada").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"codigo\":null,\"preco\":null}",
            "{\"codigo\":0,\"preco\":1}",
            "{\"codigo\":-1,\"preco\":1}",
            "{\"codigo\":1,\"preco\":0}",
            "{\"codigo\":1,\"preco\":-1}"})
    void validaCadastroNoServico(String corpo) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("requisicao.dados.invalidos"))
                .andExpect(jsonPath("$.erros[0].campo").isString())
                .andExpect(jsonPath("$.erros[0].chave").isString())
                .andExpect(jsonPath("$.erros[0].valor").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"quantidade\":null}", "{\"quantidade\":0}", "{\"quantidade\":-1}"})
    void validaReposicaoNoServico(String corpo) throws Exception {
        mvc.perform(put("/loja/produto/1/entrada").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.dados.invalidos"));
    }

    @Test
    void parametroInvalidoEValidacaoDeCodigoRetornam400() throws Exception {
        mvc.perform(get("/loja/produto/abc/estoque"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.parametro.invalido"));
        mvc.perform(get("/loja/produto/0/estoque"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].chave").value("produto.codigo.positivo"));
        mvc.perform(put("/loja/produto/-1/entrada").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estouroRetorna400SemAlteracao() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, Integer.MAX_VALUE);
        mvc.perform(put("/loja/produto/{codigo}/entrada", codigo)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("produto.estoque.limite.excedido"));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(Integer.MAX_VALUE));
    }

    private void cadastrar(int codigo, int estoque) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":" + codigo + ",\"preco\":59.90}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.preco").value(59.90))
                .andExpect(jsonPath("$.estoque").value(0))
                .andExpect(jsonPath("$.*", hasSize(3)));

        if (estoque > 0) {
            mvc.perform(put("/loja/produto/{codigo}/entrada", codigo)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"quantidade\":" + estoque + "}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(codigo))
                    .andExpect(jsonPath("$.estoque").value(estoque));
        }
    }
}
