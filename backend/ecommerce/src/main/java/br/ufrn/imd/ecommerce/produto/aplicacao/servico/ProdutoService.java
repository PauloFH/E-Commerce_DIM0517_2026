package br.ufrn.imd.ecommerce.produto.aplicacao.servico;

import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.SomarEstoqueDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoJaExisteException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Objects;

/** Casos de uso de produto, independentes de Spring e do armazenamento concreto. */
public final class ProdutoService {
    private final ProdutoRepository repository;
    private final Validator validator;

    /**
     * Cria o serviço fora do Spring com o Validator padrão do Jakarta Validation,
     * compartilhado por todas as instâncias criadas assim.
     * @param repository porta de armazenamento
     */
    public ProdutoService(ProdutoRepository repository) {
        this(repository, ValidatorPadrao.INSTANCIA);
    }

    /** Uma única factory por processo, criada só no primeiro uso do construtor sem Validator. */
    private static final class ValidatorPadrao {
        private static final Validator INSTANCIA = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Recebe as dependências usadas também em chamadas fora do Spring.
     * @param repository porta de armazenamento
     * @param validator executor das restrições declaradas nos DTOs
     */
    public ProdutoService(ProdutoRepository repository, Validator validator) {
        this.repository = Objects.requireNonNull(repository);
        this.validator = Objects.requireNonNull(validator);
    }

    /**
     * Consulta um produto validando o identificador antes do acesso ao armazenamento.
     * @param dados entrada da consulta
     * @return produto encontrado
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws DadosInvalidosException se a entrada for nula
     * @throws ProdutoNaoEncontradoException se o produto não existir
     */
    public Produto consultar(BuscarProdutoDto dados) {
        validar(dados);
        return repository.buscar(dados.codigo()).orElseThrow(
                () -> new ProdutoNaoEncontradoException("produto.nao.encontrado", dados.codigo()));
    }

    /**
     * Cadastra um produto com estoque zero, sem substituir códigos existentes.
     * @param dados código e preço do produto
     * @return produto cadastrado com estoque zero
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws DadosInvalidosException se a entrada for nula
     * @throws ProdutoJaExisteException se o código já estiver cadastrado
     */
    public Produto cadastrar(CadastrarProdutoDto dados) {
        validar(dados);
        Produto produto = new Produto(dados.codigo(), dados.preco(), 0);

        if (!repository.inserir(produto)) {
            throw new ProdutoJaExisteException("produto.ja.existe", dados.codigo());
        }

        return produto;
    }

    /**
     * Repõe estoque atomicamente; chamadas repetidas somam novamente a quantidade.
     * @param dados código e quantidade em unidades
     * @return estoque após a reposição
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws ProdutoNaoEncontradoException se o produto não existir
     * @throws DadosInvalidosException se a entrada for nula ou o estoque exceder o limite inteiro
     */
    public Integer entrada(SomarEstoqueDto dados) {
        validar(dados);
        Integer estoque;

        try {
            estoque = repository.somarEstoque(dados.codigo(), dados.quantidade()).orElseThrow(
                    () -> new ProdutoNaoEncontradoException("produto.nao.encontrado", dados.codigo()));
        } catch (ArithmeticException exception) {
            throw new DadosInvalidosException("produto.estoque.limite.excedido", dados.codigo());
        }

        return estoque;
    }

    /** Executa as restrições declarativas antes de qualquer efeito no armazenamento. */
    private <T> void validar(T dados) {
        if (dados == null) {
            throw new DadosInvalidosException("requisicao.dados.obrigatorios");
        }

        var violacoes = validator.validate(dados);

        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }
    }
}
