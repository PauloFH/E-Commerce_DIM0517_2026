package br.ufrn.imd.ecommerce.produto.aplicacao.servico;

import br.ufrn.imd.ecommerce.produto.adaptador.memoria.ProdutoRepositoryEmMemoria;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.SomarEstoqueDto;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoJaExisteException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class ProdutoServiceTests {
    private static ValidatorFactory factory;
    private ProdutoService service;

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
        service = new ProdutoService(new ProdutoRepositoryEmMemoria(), factory.getValidator());
    }

    @Test
    void cadastraConsultaERepoeSemSpring() {
        var criado = service.cadastrar(new CadastrarProdutoDto(1, new BigDecimal("10.50")));
        assertThat(criado.getEstoque()).isZero();
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isZero();
        service.entrada(new SomarEstoqueDto(1, 15));
        assertThat(service.entrada(new SomarEstoqueDto(1, 10))).isEqualTo(25);
        var produto = service.consultar(new BuscarProdutoDto(1));
        assertThat(produto.getPreco()).isEqualByComparingTo("10.50");
        assertThat(produto.getEstoque()).isEqualTo(25);
    }

    @Test
    void criaSoComORepositorioEValidaMesmoAssim() {
        var semValidatorExplicito = new ProdutoService(new ProdutoRepositoryEmMemoria());
        assertThatThrownBy(() -> semValidatorExplicito.cadastrar(new CadastrarProdutoDto(null, BigDecimal.ONE)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(semValidatorExplicito.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE)).getEstoque()).isZero();
    }

    @Test
    void duplicacaoNaoSobrescreveProduto() {
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        assertThatThrownBy(() -> service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.TEN)))
                .isInstanceOf(ProdutoJaExisteException.class).hasMessage("produto.ja.existe");
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isZero();
    }

    @Test
    void ausenciaEEstouroTemErrosDeNegocio() {
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.entrada(new SomarEstoqueDto(1, 1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
        service.cadastrar(new CadastrarProdutoDto(1, BigDecimal.ONE));
        service.entrada(new SomarEstoqueDto(1, Integer.MAX_VALUE));
        assertThatThrownBy(() -> service.entrada(new SomarEstoqueDto(1, 1)))
                .isInstanceOf(DadosInvalidosException.class).hasMessage("produto.estoque.limite.excedido");
        assertThat(service.consultar(new BuscarProdutoDto(1)).getEstoque()).isEqualTo(Integer.MAX_VALUE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejeitaCodigoInvalido(Integer codigo) {
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(codigo)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.cadastrar(new CadastrarProdutoDto(codigo, BigDecimal.ONE)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.entrada(new SomarEstoqueDto(codigo, 1)))
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
        assertThatThrownBy(() -> service.cadastrar(dados)).isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> service.consultar(new BuscarProdutoDto(1)))
                .isInstanceOf(ProdutoNaoEncontradoException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejeitaQuantidadeInvalida(Integer quantidade) {
        assertThatThrownBy(() -> service.entrada(new SomarEstoqueDto(1, quantidade)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void rejeitaEntradasNulas() {
        assertThatThrownBy(() -> service.consultar(null)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.cadastrar(null)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> service.entrada(null)).isInstanceOf(DadosInvalidosException.class);
    }
}
