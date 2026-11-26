package br.ufrn.imd.ecommerce.produto.aplicacao.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.ufrn.imd.ecommerce.produto.EstoqueEntrou;
import br.ufrn.imd.ecommerce.produto.EstoqueSaiu;
import br.ufrn.imd.ecommerce.produto.adaptador.memoria.ProdutoRepositoryEmMemoria;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.MovimentoEstoqueDto;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoJaExisteException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import br.ufrn.imd.ecommerce.produto.estoque.ItemBaixado;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProdutoServiceTests {
    private static ValidatorFactory factory;
    private ProdutoService service;
    private final List<Object> eventos = new ArrayList<>();

    @BeforeAll
    static void criarValidator() {
        factory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void fecharValidator() {
        factory.close();
    }

    @BeforeEach
    void criarServicoSemSpring() {
        service =
                new ProdutoService(
                        new ProdutoRepositoryEmMemoria(), factory.getValidator(), eventos::add);
    }

    @Test
    void publicaSomenteAsMovimentacoesConcluidas() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, 10));
        service.saida(new MovimentoEstoqueDto(1, 3));

        assertThatThrownBy(() -> service.saida(new MovimentoEstoqueDto(1, 8)))
                .isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.entrada(new MovimentoEstoqueDto(2, 1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        assertThat(eventos).containsExactly(new EstoqueEntrou(1, 10, 10), new EstoqueSaiu(1, 3, 7));
    }

    @Test
    void cadastraConsultaERepoeSemSpring() {
        var criado = service.cadastrar(new CadastrarProdutoDto(1, new BigDecimal("10.50")));
        assertThat(criado.getEstoque()).isZero();
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isZero();
        service.entrada(new MovimentoEstoqueDto(1, 15));
        assertThat(service.entrada(new MovimentoEstoqueDto(1, 10))).isEqualTo(25);
        var produto = service.consultar(new BuscarProdutoDto(1));
        assertThat(produto.getPreco()).isEqualByComparingTo("10.50");
        assertThat(produto.getEstoque()).isEqualTo(25);
    }

    @Test
    void criaSoComORepositorioEValidaMesmoAssim() {
        var semValidatorExplicito = new ProdutoService(new ProdutoRepositoryEmMemoria());
        assertThatThrownBy(() ->
                        semValidatorExplicito.cadastrar(
                                new CadastrarProdutoDto(null, BigDecimal.ONE)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(
                        semValidatorExplicito
                                .cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE))
                                .getEstoque())
                .isZero();
    }

    @Test
    void duplicacaoNaoSobrescreveProduto() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        assertThatThrownBy(() -> service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.TEN)))
                .isInstanceOf(ProdutoJaExisteException.class)
                .hasMessage("produto.ja.existe");
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isZero();
    }

    @Test
    void ausenciaEEstouroTemErrosDeNegocio() {
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.entrada(new MovimentoEstoqueDto(1, 1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, Integer.MAX_VALUE));
        assertThatThrownBy(() -> service.entrada(new MovimentoEstoqueDto(1, 1)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.estoque.limite.excedido");
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque())
                .isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void saidaSubtraiDoEstoque() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, 15));
        assertThat(service.saida(new MovimentoEstoqueDto(1, 10))).isEqualTo(5);
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(5);
    }

    @Test
    void saidaIgualAoEstoqueZera() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, 15));
        assertThat(service.saida(new MovimentoEstoqueDto(1, 15))).isZero();
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isZero();
    }

    @Test
    void saidaMaiorQueOEstoqueFalhaSemAlterar() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, 15));
        assertThatThrownBy(() -> service.saida(new MovimentoEstoqueDto(1, 16)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.estoque.insuficiente")
                .satisfies(e ->
                        assertThat(((DadosInvalidosException) e).getArgumentos())
                                .containsExactly(15, 1));
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(15);
    }

    @Test
    void saidaDeProdutoInexistenteFalha() {
        assertThatThrownBy(() -> service.saida(new MovimentoEstoqueDto(1, 1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("produto.nao.encontrado");
    }

    @Test
    void baixarItensBaixaTodosNaOrdemRecebida() {
        cadastrarComEstoque(1, "59.90", 15);
        cadastrarComEstoque(2, "69.90", 8);
        var itens = service.baixarItens(mapa(2, 1, 1, 2));
        assertThat(itens.keySet()).containsExactly(2, 1);
        assertThat(itens.get(1)).isEqualTo(new ItemBaixado(new BigDecimal("59.90"), 13));
        assertThat(itens.get(2)).isEqualTo(new ItemBaixado(new BigDecimal("69.90"), 7));
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(13);
        assertThat(service.consultar(new BuscarProdutoDto(2)).getEstoque()).isEqualTo(7);
    }

    @Test
    void baixarItensSemEstoqueNaoAlteraNenhumProduto() {
        cadastrarComEstoque(1, "1", 15);
        cadastrarComEstoque(2, "1", 8);
        assertThatThrownBy(() -> service.baixarItens(mapa(1, 5, 2, 9)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.estoque.insuficiente")
                .satisfies(e ->
                        assertThat(((DadosInvalidosException) e).getArgumentos())
                                .containsExactly(8, 2));
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(15);
        assertThat(service.consultar(new BuscarProdutoDto(2)).getEstoque()).isEqualTo(8);
    }

    @Test
    void baixarItensComProdutoInexistenteNaoAlteraNada() {
        cadastrarComEstoque(1, "1", 15);
        assertThatThrownBy(() -> service.baixarItens(mapa(1, 5, 2, 1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("produto.nao.encontrado")
                .satisfies(e ->
                        assertThat(((ProdutoNaoEncontradoException) e).getArgumentos())
                                .containsExactly(2));
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(15);
    }

    @Test
    void baixarItensRejeitaMapaInvalidoSemAlterarNada() {
        cadastrarComEstoque(1, "1", 15);
        var chaveNula = new HashMap<Integer, Integer>();
        chaveNula.put(null, 1);
        var valorNulo = new HashMap<Integer, Integer>();
        valorNulo.put(1, null);
        assertThatThrownBy(() -> service.baixarItens(null))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("requisicao.dados.obrigatorios");
        assertThatThrownBy(() -> service.baixarItens(Map.of()))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("requisicao.dados.obrigatorios");
        assertThatThrownBy(() -> service.baixarItens(chaveNula))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("requisicao.dados.obrigatorios");
        assertThatThrownBy(() -> service.baixarItens(valorNulo))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("requisicao.dados.obrigatorios");
        assertThatThrownBy(() -> service.baixarItens(mapa(1, 1, 0, 1)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.codigo.positivo");
        assertThatThrownBy(() -> service.baixarItens(mapa(1, 1, 2, 0)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.quantidade.positiva");
        assertThatThrownBy(() -> service.baixarItens(mapa(1, -1)))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.quantidade.positiva");
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(15);
    }

    @Test
    @Timeout(15)
    void baixasDeVariosItensConcorrentesNuncaDeixamEstoqueNegativo() throws Exception {
        int inicial = 1000;
        int threads = 16;
        cadastrarComEstoque(1, "1", inicial);
        cadastrarComEstoque(2, "1", inicial);
        var largada = new CountDownLatch(1);
        var sucessos = new AtomicInteger();
        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tarefas.add(() -> {
                largada.await();
                for (int j = 0; j < 100; j++) {
                    try {
                        service.baixarItens(mapa(1, 3, 2, 1));
                        sucessos.incrementAndGet();
                    } catch (DadosInvalidosException esperada) {
                        // estoque esgotado: a baixa inteira é recusada
                    }
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            largada.countDown();
            for (var resultado : resultados) {
                resultado.get(10, TimeUnit.SECONDS);
            }
        }

        int primeiro = service.consultar(new BuscarProdutoDto(1)).getEstoque();
        int segundo = service.consultar(new BuscarProdutoDto(2)).getEstoque();
        assertThat(primeiro).isGreaterThanOrEqualTo(0);
        assertThat(segundo).isGreaterThanOrEqualTo(0);
        assertThat(sucessos.get() * 3 + primeiro).isEqualTo(inicial);
        assertThat(sucessos.get() + segundo).isEqualTo(inicial);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void saidaComQuantidadeInvalidaNaoAlteraNada(Integer quantidade) {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, 15));
        assertThatThrownBy(() -> service.saida(new MovimentoEstoqueDto(1, quantidade)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(15);
    }

    @Test
    @Timeout(15)
    void saidasConcorrentesNuncaDeixamEstoqueNegativo() throws Exception {
        int inicial = 1000;
        int quantidade = 3;
        int threads = 16;
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new MovimentoEstoqueDto(1, inicial));
        var largada = new CountDownLatch(1);
        var sucessos = new AtomicInteger();
        var estoqueNegativo = new AtomicBoolean();
        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tarefas.add(() -> {
                largada.await();
                for (int j = 0; j < 100; j++) {
                    try {
                        if (service.saida(new MovimentoEstoqueDto(1, quantidade)) < 0) {
                            estoqueNegativo.set(true);
                        }
                        sucessos.incrementAndGet();
                    } catch (DadosInvalidosException esperada) {
                        // estoque esgotado: a saída é recusada
                    }
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            largada.countDown();
            for (var resultado : resultados) {
                resultado.get(10, TimeUnit.SECONDS);
            }
        }

        int estoqueFinal = service.consultar(new BuscarProdutoDto(1)).getEstoque();
        assertThat(estoqueNegativo).isFalse();
        assertThat(estoqueFinal).isGreaterThanOrEqualTo(0);
        assertThat(sucessos.get() * quantidade + estoqueFinal).isEqualTo(inicial);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejeitaCodigoInvalido(Integer codigo) {
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(codigo)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.cadastrar(new CadastrarProdutoDto(codigo, BigDecimal.ONE)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.entrada(new MovimentoEstoqueDto(codigo, 1)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.saida(new MovimentoEstoqueDto(codigo, 1)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-0.01"})
    void rejeitaPrecoInvalido(String preco) {
        BigDecimal valor = null;
        if (preco != null) {
            valor = new BigDecimal(preco);
        }
        var dados = new CadastrarProdutoDto(1, valor);
        assertThatThrownBy(() -> service.cadastrar(dados))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejeitaQuantidadeInvalida(Integer quantidade) {
        assertThatThrownBy(() -> service.entrada(new MovimentoEstoqueDto(1, quantidade)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void rejeitaEntradasNulas() {
        assertThatThrownBy(() -> service.consultar(null))
                .isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.cadastrar(null))
                .isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.entrada(null)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.saida(null)).isInstanceOf(DadosInvalidosException.class);
    }

    private void cadastrarComEstoque(int codigo, String preco, int estoque) {
        service.cadastrar(new CadastrarProdutoDto(codigo, new BigDecimal(preco)));
        service.entrada(new MovimentoEstoqueDto(codigo, estoque));
    }

    private static Map<Integer, Integer> mapa(int... paresCodigoQuantidade) {
        var resultado = new LinkedHashMap<Integer, Integer>();

        for (int i = 0; i < paresCodigoQuantidade.length; i += 2) {
            resultado.put(paresCodigoQuantidade[i], paresCodigoQuantidade[i + 1]);
        }

        return resultado;
    }
}
