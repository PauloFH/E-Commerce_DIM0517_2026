package br.ufrn.imd.ecommerce.venda.aplicacao.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import br.ufrn.imd.ecommerce.produto.adaptador.memoria.ProdutoRepositoryEmMemoria;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.MovimentoEstoqueDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import br.ufrn.imd.ecommerce.produto.estoque.BaixaDeEstoque;
import br.ufrn.imd.ecommerce.produto.estoque.ItemBaixado;
import br.ufrn.imd.ecommerce.venda.VendaRealizada;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVenda;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVendido;
import br.ufrn.imd.ecommerce.venda.dominio.excecao.VendaInvalidaException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class VendaServiceTests {
    private static ValidatorFactory factory;
    private ProdutoService produtos;
    private VendaService service;
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
    void criarServicosSemSpring() {
        produtos = new ProdutoService(new ProdutoRepositoryEmMemoria(), factory.getValidator());
        service = new VendaService(produtos, factory.getValidator(), eventos::add);
    }

    @Test
    void publicaApenasOVendidoComItensAgrupados() {
        cadastrar(123, "2.50", 8);
        service.vender(List.of(new ItemVenda(123, 2), new ItemVenda(123, 1)));

        assertThatThrownBy(() -> service.vender(List.of(new ItemVenda(123, 6))))
                .isInstanceOf(DadosInvalidosException.class);
        assertThat(eventos)
                .containsExactly(
                        new VendaRealizada(
                                new BigDecimal("7.50"),
                                List.of(new VendaRealizada.Item(123, 3, 5))));
    }

    @Test
    void exemploDaEspecificacaoCalculaTotalESubtotais() {
        cadastrar(123, "59.90", 15);
        cadastrar(456, "69.90", 8);
        var resultado = service.vender(List.of(new ItemVenda(123, 2), new ItemVenda(456, 1)));
        assertThat(resultado.total()).isEqualTo(new BigDecimal("189.70"));
        assertThat(resultado.itens())
                .containsExactly(
                        new ItemVendido(123, 2, new BigDecimal("119.80"), 13),
                        new ItemVendido(456, 1, new BigDecimal("69.90"), 7));
        assertThat(estoque(123)).isEqualTo(13);
        assertThat(estoque(456)).isEqualTo(7);
    }

    @Test
    void criaSoComABaixaDeEstoqueEValidaMesmoAssim() {
        cadastrar(1, "10", 5);
        var semValidatorExplicito = new VendaService(produtos);
        assertThatThrownBy(() -> semValidatorExplicito.vender(List.of()))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(semValidatorExplicito.vender(List.of(new ItemVenda(1, 2))).total())
                .isEqualTo(new BigDecimal("20.00"));
    }

    @Test
    void codigosRepetidosSaoAgrupadosNaOrdemDaPrimeiraAparicao() {
        var chamadas = new ArrayList<Map<Integer, Integer>>();
        BaixaDeEstoque baixa =
                quantidades -> {
                    chamadas.add(quantidades);
                    return Map.of(
                            1, new ItemBaixado(new BigDecimal("2.50"), 10),
                            2, new ItemBaixado(new BigDecimal("1.00"), 20));
                };
        var resultado =
                new VendaService(baixa, factory.getValidator())
                        .vender(
                                List.of(
                                        new ItemVenda(2, 1),
                                        new ItemVenda(1, 2),
                                        new ItemVenda(2, 3)));
        assertThat(chamadas).hasSize(1);
        assertThat(chamadas.get(0)).containsExactly(entry(2, 4), entry(1, 2));
        assertThat(resultado.itens())
                .containsExactly(
                        new ItemVendido(2, 4, new BigDecimal("4.00"), 20),
                        new ItemVendido(1, 2, new BigDecimal("5.00"), 10));
        assertThat(resultado.total()).isEqualTo(new BigDecimal("9.00"));
    }

    @Test
    void codigoRepetidoNaoDeixaEstoqueNegativo() {
        cadastrar(1, "1", 4);
        assertThatThrownBy(() -> service.vender(List.of(new ItemVenda(1, 3), new ItemVenda(1, 3))))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.estoque.insuficiente")
                .satisfies(e ->
                        assertThat(((DadosInvalidosException) e).getArgumentos())
                                .containsExactly(4, 1));
        assertThat(estoque(1)).isEqualTo(4);
        var resultado = service.vender(List.of(new ItemVenda(1, 2), new ItemVenda(1, 2)));
        assertThat(resultado.itens())
                .containsExactly(new ItemVendido(1, 4, new BigDecimal("4.00"), 0));
        assertThat(estoque(1)).isZero();
    }

    @Test
    void estoqueInsuficienteNaoAlteraNenhumItemValido() {
        cadastrar(1, "1", 10);
        cadastrar(2, "1", 5);
        assertThatThrownBy(() -> service.vender(List.of(new ItemVenda(1, 3), new ItemVenda(2, 6))))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("produto.estoque.insuficiente")
                .satisfies(e ->
                        assertThat(((DadosInvalidosException) e).getArgumentos())
                                .containsExactly(5, 2));
        assertThat(estoque(1)).isEqualTo(10);
        assertThat(estoque(2)).isEqualTo(5);
    }

    @Test
    void produtoInexistenteNaoAlteraNenhumItemValido() {
        cadastrar(1, "1", 10);
        assertThatThrownBy(() -> service.vender(List.of(new ItemVenda(1, 3), new ItemVenda(2, 1))))
                .isInstanceOf(ProdutoNaoEncontradoException.class)
                .hasMessage("produto.nao.encontrado");
        assertThat(estoque(1)).isEqualTo(10);
    }

    @Test
    void listaInvalidaFalhaSemChamarABaixa() {
        var chamadas = new AtomicInteger();
        var sut =
                new VendaService(
                        quantidades -> {
                            chamadas.incrementAndGet();
                            return Map.of();
                        },
                        factory.getValidator());
        var listaComNulo = Arrays.asList(new ItemVenda(1, 1), null);

        assertThat(chaves(() -> sut.vender(null)))
                .containsExactly("itens:venda.itens.obrigatorios");
        assertThat(chaves(() -> sut.vender(List.of())))
                .containsExactly("itens:venda.itens.obrigatorios");
        assertThat(chaves(() -> sut.vender(listaComNulo)))
                .containsExactly("itens[1].<list element>:venda.item.obrigatorio");
        assertThat(chaves(() -> sut.vender(List.of(new ItemVenda(null, 1)))))
                .containsExactly("itens[0].codigo:produto.codigo.obrigatorio");
        assertThat(chaves(() -> sut.vender(List.of(new ItemVenda(0, 1)))))
                .containsExactly("itens[0].codigo:produto.codigo.positivo");
        assertThat(chaves(() -> sut.vender(List.of(new ItemVenda(1, null)))))
                .containsExactly("itens[0].quantidade:produto.quantidade.obrigatoria");
        assertThat(chaves(() -> sut.vender(List.of(new ItemVenda(1, 1), new ItemVenda(2, -1)))))
                .containsExactly("itens[1].quantidade:produto.quantidade.positiva");
        assertThat(chamadas).hasValue(0);
    }

    @Test
    void somaQueEstouraOLimiteInteiroFalhaSemChamarABaixa() {
        var chamadas = new AtomicInteger();
        var sut =
                new VendaService(
                        quantidades -> {
                            chamadas.incrementAndGet();
                            return Map.of();
                        },
                        factory.getValidator());
        assertThatThrownBy(() ->
                        sut.vender(
                                List.of(new ItemVenda(7, Integer.MAX_VALUE), new ItemVenda(7, 1))))
                .isInstanceOf(VendaInvalidaException.class)
                .hasMessage("venda.quantidade.limite.excedido")
                .satisfies(e ->
                        assertThat(((VendaInvalidaException) e).getArgumentos())
                                .containsExactly(7));
        assertThat(chamadas).hasValue(0);
    }

    @Test
    void dinheiroUsaDuasCasasComArredondamentoBancario() {
        BaixaDeEstoque baixa =
                quantidades ->
                        Map.of(
                                1, new ItemBaixado(new BigDecimal("0.125"), 0),
                                2, new ItemBaixado(new BigDecimal("0.135"), 0),
                                3, new ItemBaixado(new BigDecimal("2"), 0));
        var resultado =
                new VendaService(baixa, factory.getValidator())
                        .vender(
                                List.of(
                                        new ItemVenda(1, 1),
                                        new ItemVenda(2, 1),
                                        new ItemVenda(3, 1)));
        assertThat(resultado.itens())
                .extracting(ItemVendido::subtotal)
                .containsExactly(
                        new BigDecimal("0.12"), new BigDecimal("0.14"), new BigDecimal("2.00"));
        assertThat(resultado.total()).isEqualTo(new BigDecimal("2.26"));
    }

    @Test
    @Timeout(20)
    void vendasConcorrentesNuncaDeixamEstoqueNegativo() throws Exception {
        int inicial = 1000;
        int threads = 16;
        cadastrar(1, "1", inicial);
        cadastrar(2, "1", inicial);
        var largada = new CountDownLatch(1);
        var vendas = new AtomicInteger();
        List<Callable<Void>> tarefas = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            tarefas.add(() -> {
                largada.await();
                for (int j = 0; j < 100; j++) {
                    try {
                        // o código 1 repetido soma 3 + 2 por venda, em uma única baixa
                        service.vender(
                                List.of(
                                        new ItemVenda(1, 3),
                                        new ItemVenda(2, 1),
                                        new ItemVenda(1, 2)));
                        vendas.incrementAndGet();
                    } catch (DadosInvalidosException esperada) {
                        // estoque esgotado: a venda inteira é recusada
                    }
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(threads)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            largada.countDown();
            for (var resultado : resultados) {
                resultado.get(15, TimeUnit.SECONDS);
            }
        }

        assertThat(estoque(1)).isGreaterThanOrEqualTo(0);
        assertThat(estoque(2)).isGreaterThanOrEqualTo(0);
        assertThat(vendas.get() * 5 + estoque(1)).isEqualTo(inicial);
        assertThat(vendas.get() + estoque(2)).isEqualTo(inicial);
        assertThat(vendas.get()).isEqualTo(200);
    }

    private void cadastrar(int codigo, String preco, int estoque) {
        produtos.cadastrar(new CadastrarProdutoDto(codigo, new BigDecimal(preco)));
        produtos.entrada(new MovimentoEstoqueDto(codigo, estoque));
    }

    private int estoque(int codigo) {
        return produtos.consultar(new BuscarProdutoDto(codigo)).getEstoque();
    }

    private static List<String> chaves(Runnable acao) {
        try {
            acao.run();
        } catch (ConstraintViolationException e) {
            return e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ":" + v.getMessage())
                    .sorted()
                    .collect(Collectors.toList());
        }
        throw new AssertionError("era esperada uma ConstraintViolationException");
    }
}
