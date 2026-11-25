package br.ufrn.imd.ecommerce.produto.adaptador.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
        mvc.perform(
                        put("/loja/produto/{codigo}/entrada", codigo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.estoque").value(25))
                .andExpect(jsonPath("$.*", hasSize(2)));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(25));
    }

    @Test
    void duplicacaoPreservaProdutoERetorna409() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 5);
        mvc.perform(
                        post("/loja/produto")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codigo\":" + codigo + ",\"preco\":99}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O produto " + codigo + " já existe."))
                .andExpect(jsonPath("$.argumentos[0]").value(codigo));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(5));
    }

    @Test
    void produtoAusenteRetorna404() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Não encontrado"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Produto " + codigo + " não encontrado."))
                .andExpect(jsonPath("$.instance").value("/loja/produto/" + codigo + "/estoque"));
        mvc.perform(
                        put("/loja/produto/{codigo}/entrada", codigo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":1}"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.5", "\"10\"", "\"abc\"", "true", "2147483648", "\"\""})
    void rejeitaCoercaoDeInteiros(String valor) throws Exception {
        mvc.perform(
                        post("/loja/produto")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codigo\":" + valor + ",\"preco\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("O corpo da requisição é inválido."));
        mvc.perform(
                        put("/loja/produto/1/entrada")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":" + valor + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição é inválido."));
        saida(1, "{\"quantidade\":" + valor + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição é inválido."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"59.90\"", "\"abc\"", "true", "\"\""})
    void rejeitaCoercaoDoPreco(String valor) throws Exception {
        mvc.perform(
                        post("/loja/produto")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"codigo\":"
                                                + CODIGOS.incrementAndGet()
                                                + ",\"preco\":"
                                                + valor
                                                + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O corpo da requisição é inválido."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{", "[]"})
    void rejeitaCorpoAusenteNuloOuMalformado(String corpo) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/loja/produto"));
        mvc.perform(
                        put("/loja/produto/1/entrada")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        saida(1, corpo)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"codigo\":null,\"preco\":null}",
                "{\"codigo\":0,\"preco\":1}",
                "{\"codigo\":-1,\"preco\":1}",
                "{\"codigo\":1,\"preco\":0}",
                "{\"codigo\":1,\"preco\":-1}"
            })
    void validaCadastroNoServico(String corpo) throws Exception {
        mvc.perform(post("/loja/produto").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Os dados da requisição são inválidos."))
                .andExpect(jsonPath("$.erros[0].campo").isString())
                .andExpect(jsonPath("$.erros[0].mensagem").isString())
                .andExpect(jsonPath("$.erros[0].valor").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"{}", "{\"quantidade\":null}", "{\"quantidade\":0}", "{\"quantidade\":-1}"})
    void validaReposicaoNoServico(String corpo) throws Exception {
        mvc.perform(
                        put("/loja/produto/1/entrada")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Os dados da requisição são inválidos."));
        saida(1, corpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Os dados da requisição são inválidos."));
    }

    @Test
    void parametroInvalidoEValidacaoDeCodigoRetornam400() throws Exception {
        mvc.perform(get("/loja/produto/abc/estoque"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O parâmetro da requisição é inválido."));
        mvc.perform(get("/loja/produto/0/estoque"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.erros[0].mensagem")
                                .value("O código do produto deve ser positivo."));
        mvc.perform(
                        put("/loja/produto/-1/entrada")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estouroRetorna400SemAlteracao() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, Integer.MAX_VALUE);
        mvc.perform(
                        put("/loja/produto/{codigo}/entrada", codigo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "O estoque do produto "
                                                + codigo
                                                + " excede o limite permitido."));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(Integer.MAX_VALUE));
    }

    @Test
    void traduzErrosConformeIdiomaEUsaPortuguesComoPadrao() throws Exception {
        mvc.perform(get("/loja/produto/1234567/estoque").header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Product 1234567 not found."))
                .andExpect(jsonPath("$.argumentos[0]").value(1234567));
        mvc.perform(get("/loja/produto/1234567/estoque").header("Accept-Language", "fr"))
                .andExpect(jsonPath("$.title").value("Não encontrado"))
                .andExpect(jsonPath("$.detail").value("Produto 1234567 não encontrado."));
        mvc.perform(
                        get("/loja/produto/1234567/estoque")
                                .header("Accept-Language", "fr, en;q=0.9, pt-BR;q=0.5"))
                .andExpect(jsonPath("$.detail").value("Product 1234567 not found."));
        mvc.perform(get("/loja/produto/0/estoque").header("Accept-Language", "en-US"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("The request data is invalid."))
                .andExpect(jsonPath("$.erros[0].campo").value("codigo"))
                .andExpect(
                        jsonPath("$.erros[0].mensagem").value("The product code must be positive."))
                .andExpect(jsonPath("$.erros[0].chave").doesNotExist())
                .andExpect(jsonPath("$.erros[0].valor").doesNotExist());
        mvc.perform(
                        post("/loja/produto")
                                .header("Accept-Language", "en")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{"))
                .andExpect(jsonPath("$.detail").value("The request body is invalid."));
        mvc.perform(get("/loja/produto/abc/estoque").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.detail").value("The request parameter is invalid."));
    }

    @Test
    void saidaSubtraiDoEstoqueComContratoHttp() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 15);
        saida(codigo, "{\"quantidade\":10}")
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.estoque").value(5))
                .andExpect(jsonPath("$.*", hasSize(2)));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(5));
    }

    @Test
    void saidaQueZeraOEstoqueRetorna200() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 15);
        saida(codigo, "{\"quantidade\":15}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.estoque").value(0));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(0));
    }

    @Test
    void saidaMaiorQueOEstoqueRetorna400SemAlteracao() throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        cadastrar(codigo, 15);
        saida(codigo, "{\"quantidade\":16}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente: há 15 unidades."))
                .andExpect(jsonPath("$.argumentos[0]").value(15));
        saida(codigo, "{\"quantidade\":16}", "en")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Insufficient stock: 15 units available."));
        mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                .andExpect(jsonPath("$.estoque").value(15));
    }

    @Test
    void saidaDeProdutoAusenteRetorna404() throws Exception {
        int codigoAusente = CODIGOS.incrementAndGet();
        saida(codigoAusente, "{\"quantidade\":1}")
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(
                        jsonPath("$.detail")
                                .value("Produto " + codigoAusente + " não encontrado."));
    }

    @Test
    void saidaComCodigoInvalidoRetorna400() throws Exception {
        saida(0, "{\"quantidade\":1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Os dados da requisição são inválidos."));
    }

    private ResultActions saida(int codigo, String corpo) throws Exception {
        return saida(codigo, corpo, "pt-BR");
    }

    private ResultActions saida(int codigo, String corpo, String idioma) throws Exception {
        return mvc.perform(
                put("/loja/produto/{codigo}/saida", codigo)
                        .header("Accept-Language", idioma)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo));
    }

    private void cadastrar(int codigo, int estoque) throws Exception {
        mvc.perform(
                        post("/loja/produto")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codigo\":" + codigo + ",\"preco\":59.90}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(codigo))
                .andExpect(jsonPath("$.preco").value(59.90))
                .andExpect(jsonPath("$.estoque").value(0))
                .andExpect(jsonPath("$.*", hasSize(3)));

        if (estoque > 0) {
            mvc.perform(
                            put("/loja/produto/{codigo}/entrada", codigo)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"quantidade\":" + estoque + "}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(codigo))
                    .andExpect(jsonPath("$.estoque").value(estoque));
        }
    }
}
