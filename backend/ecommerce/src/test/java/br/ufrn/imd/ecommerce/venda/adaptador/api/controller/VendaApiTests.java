package br.ufrn.imd.ecommerce.venda.adaptador.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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
class VendaApiTests {
    // faixa própria: o repositório é compartilhado entre as classes de teste do mesmo contexto
    private static final AtomicInteger CODIGOS = new AtomicInteger(50_000);
    @Autowired private MockMvc mvc;

    @Test
    void exemploDaEspecificacaoRetorna201ComTotalESubtotais() throws Exception {
        int a = cadastrar("59.90", 15);
        int b = cadastrar("69.90", 8);
        vender(
                        "{\"itens\":[{\"codigo\":"
                                + a
                                + ",\"quantidade\":2},{\"codigo\":"
                                + b
                                + ",\"quantidade\":1}]}")
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(
                        content()
                                .string(
                                        "{\"total\":189.70,\"itens\":[{\"codigo\":"
                                                + a
                                                + ",\"quantidade\":2,\"subtotal\":119.80,\"estoque\":13},{\"codigo\":"
                                                + b
                                                + ",\"quantidade\":1,\"subtotal\":69.90,\"estoque\":7}]}"))
                .andExpect(jsonPath("$.itens", hasSize(2)));
        assertThat(estoque(a)).isEqualTo(13);
        assertThat(estoque(b)).isEqualTo(7);
    }

    @Test
    void codigoRepetidoEAgrupadoNumUnicoItem() throws Exception {
        int a = cadastrar("10", 5);
        vender(corpo(a, 2, a, 3))
                .andExpect(status().isCreated())
                .andExpect(
                        content()
                                .string(
                                        "{\"total\":50.00,\"itens\":[{\"codigo\":"
                                                + a
                                                + ",\"quantidade\":5,\"subtotal\":50.00,\"estoque\":0}]}"));
        assertThat(estoque(a)).isZero();
    }

    @Test
    void codigoRepetidoNaoDeixaEstoqueNegativo() throws Exception {
        int a = cadastrar("10", 5);
        vender(corpo(a, 3, a, 3))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("produto.estoque.insuficiente"))
                .andExpect(jsonPath("$.argumentos[0]").value(5))
                .andExpect(jsonPath("$.argumentos[1]").value(a));
        assertThat(estoque(a)).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"itens\":null}", "{\"itens\":[]}"})
    void listaAusenteNulaOuVaziaRetorna400(String json) throws Exception {
        vender(json)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("requisicao.dados.invalidos"))
                .andExpect(jsonPath("$.erros[0].campo").value("itens"))
                .andExpect(jsonPath("$.erros[0].chave").value("venda.itens.obrigatorios"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "[]", "{\"itens\":{}}", "{\"itens\":\"abc\"}", "{"})
    void corpoIlegivelOuComTipoErradoRetorna400(String json) throws Exception {
        vender(json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"quantidade\":1}",
                "{\"codigo\":%d}",
                "{\"codigo\":null,\"quantidade\":1}",
                "{\"codigo\":%d,\"quantidade\":null}",
                "{\"codigo\":0,\"quantidade\":1}",
                "{\"codigo\":-1,\"quantidade\":1}",
                "{\"codigo\":%d,\"quantidade\":0}",
                "{\"codigo\":%d,\"quantidade\":-3}",
                "null"
            })
    void itemInvalidoRetorna400SemAlterarOsItensValidosAnteriores(String item) throws Exception {
        int a = cadastrar("10", 10);
        vender(
                        "{\"itens\":[{\"codigo\":"
                                + a
                                + ",\"quantidade\":1},"
                                + item.replace("%d", "" + a)
                                + "]}")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("requisicao.dados.invalidos"))
                .andExpect(jsonPath("$.erros[0].campo").value(startsWith("itens[1]")));
        assertThat(estoque(a)).isEqualTo(10);
    }

    @Test
    void errosApontamALinhaECampoDoItem() throws Exception {
        int a = cadastrar("10", 10);
        vender(corpo(a, 1, a, 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros", hasSize(1)))
                .andExpect(jsonPath("$.erros[0].campo").value("itens[1].quantidade"))
                .andExpect(jsonPath("$.erros[0].chave").value("produto.quantidade.positiva"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"abc\"", "2.5", "true", "\"\"", "\"2\"", "2147483648"})
    void codigoOuQuantidadeComTipoInvalidoRetorna400SemAlterarNada(String valor) throws Exception {
        int a = cadastrar("10", 10);
        vender(
                        "{\"itens\":[{\"codigo\":"
                                + a
                                + ",\"quantidade\":1},{\"codigo\":"
                                + valor
                                + ",\"quantidade\":1}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
        vender(
                        "{\"itens\":[{\"codigo\":"
                                + a
                                + ",\"quantidade\":1},{\"codigo\":"
                                + a
                                + ",\"quantidade\":"
                                + valor
                                + "}]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("requisicao.json.invalido"));
        assertThat(estoque(a)).isEqualTo(10);
    }

    @Test
    void quantidadeMaiorQueOEstoqueRetorna400SemAlterarOsDemais() throws Exception {
        int a = cadastrar("10", 10);
        int b = cadastrar("10", 5);
        vender(corpo(a, 3, b, 6))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("produto.estoque.insuficiente"))
                .andExpect(jsonPath("$.argumentos[0]").value(5))
                .andExpect(jsonPath("$.argumentos[1]").value(b))
                .andExpect(jsonPath("$.instance").value("/loja/venda"));
        assertThat(estoque(a)).isEqualTo(10);
        assertThat(estoque(b)).isEqualTo(5);
    }

    @Test
    void somaDeQuantidadesQueEstouraOLimiteRetorna400() throws Exception {
        int a = cadastrar("10", 10);
        vender(corpo(a, Integer.MAX_VALUE, a, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("venda.quantidade.limite.excedido"))
                .andExpect(jsonPath("$.argumentos[0]").value(a));
        assertThat(estoque(a)).isEqualTo(10);
    }

    @Test
    void produtoInexistenteRetorna404SemAlterarOsItensValidos() throws Exception {
        int a = cadastrar("10", 10);
        int inexistente = CODIGOS.incrementAndGet();
        vender(corpo(a, 4, inexistente, 1))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("produto.nao.encontrado"))
                .andExpect(jsonPath("$.argumentos[0]").value(inexistente));
        assertThat(estoque(a)).isEqualTo(10);
    }

    @Test
    @Timeout(30)
    void vendasSimultaneasNuncaDeixamEstoqueNegativo() throws Exception {
        int a = cadastrar("1", 50);
        int threads = 10;
        var largada = new CountDownLatch(1);
        var criadas = new AtomicInteger();
        var recusadas = new AtomicInteger();
        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tarefas.add(() -> {
                largada.await();
                for (int j = 0; j < 10; j++) {
                    int status = vender(corpo(a, 1, a, 1)).andReturn().getResponse().getStatus();

                    if (status == 201) {
                        criadas.incrementAndGet();
                    } else if (status == 400) {
                        recusadas.incrementAndGet();
                    }
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            largada.countDown();
            for (var resultado : resultados) {
                resultado.get(25, TimeUnit.SECONDS);
            }
        }

        // cada venda baixa 2 unidades (o código repetido é somado): cabem 25 vendas em 50
        assertThat(criadas.get()).isEqualTo(25);
        assertThat(recusadas.get()).isEqualTo(75);
        assertThat(estoque(a)).isZero();
    }

    private ResultActions vender(String json) throws Exception {
        return mvc.perform(
                post("/loja/venda").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String corpo(int codigo1, int quantidade1, int codigo2, int quantidade2) {
        return "{\"itens\":[{\"codigo\":"
                + codigo1
                + ",\"quantidade\":"
                + quantidade1
                + "},{\"codigo\":"
                + codigo2
                + ",\"quantidade\":"
                + quantidade2
                + "}]}";
    }

    private int cadastrar(String preco, int estoque) throws Exception {
        int codigo = CODIGOS.incrementAndGet();
        mvc.perform(
                        post("/loja/produto")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"codigo\":" + codigo + ",\"preco\":" + preco + "}"))
                .andExpect(status().isCreated());
        mvc.perform(
                        put("/loja/produto/{codigo}/entrada", codigo)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantidade\":" + estoque + "}"))
                .andExpect(status().isOk());
        return codigo;
    }

    private int estoque(int codigo) throws Exception {
        var corpo =
                mvc.perform(get("/loja/produto/{codigo}/estoque", codigo))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.parse(corpo).read("$.estoque", Integer.class);
    }
}
