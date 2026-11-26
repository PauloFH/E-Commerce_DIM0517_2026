package br.ufrn.imd.ecommerce.produto.aplicacao.servico;

import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.MovimentoEstoqueDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ResultadoBaixa;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoJaExisteException;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.ProdutoNaoEncontradoException;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import br.ufrn.imd.ecommerce.produto.estoque.BaixaDeEstoque;
import br.ufrn.imd.ecommerce.produto.estoque.ItemBaixado;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Casos de uso de produto, independentes de Spring e do armazenamento concreto. */
public final class ProdutoService implements BaixaDeEstoque {
    private final ProdutoRepository repository;
    private final Validator validator;

    /**
     * Cria o serviço fora do Spring com o Validator padrão do Jakarta Validation, compartilhado por
     * todas as instâncias criadas assim.
     *
     * @param repository porta de armazenamento
     */
    public ProdutoService(ProdutoRepository repository) {
        this(repository, ValidatorPadrao.INSTANCIA);
    }

    /** Uma única factory por processo, criada só no primeiro uso do construtor sem Validator. */
    private static final class ValidatorPadrao {
        private static final Validator INSTANCIA =
                Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Recebe as dependências usadas também em chamadas fora do Spring.
     *
     * @param repository porta de armazenamento
     * @param validator executor das restrições declaradas nos DTOs
     */
    public ProdutoService(ProdutoRepository repository, Validator validator) {
        this.repository = Objects.requireNonNull(repository);
        this.validator = Objects.requireNonNull(validator);
    }

    /**
     * Consulta um produto validando o identificador antes do acesso ao armazenamento.
     *
     * @param dados entrada da consulta
     * @return produto encontrado
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws DadosInvalidosException se a entrada for nula
     * @throws ProdutoNaoEncontradoException se o produto não existir
     */
    public Produto consultar(BuscarProdutoDto dados) {
        validar(dados);
        return repository
                .buscar(dados.codigo())
                .orElseThrow(() ->
                        new ProdutoNaoEncontradoException(
                                "produto.nao.encontrado", dados.codigo()));
    }

    /**
     * Cadastra um produto com estoque zero, sem substituir códigos existentes.
     *
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
     *
     * @param dados código e quantidade em unidades
     * @return estoque após a reposição
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws ProdutoNaoEncontradoException se o produto não existir
     * @throws DadosInvalidosException se a entrada for nula ou o estoque exceder o limite inteiro
     */
    public Integer entrada(MovimentoEstoqueDto dados) {
        validar(dados);
        Integer estoque;

        try {
            estoque =
                    repository
                            .somarEstoque(dados.codigo(), dados.quantidade())
                            .orElseThrow(() ->
                                    new ProdutoNaoEncontradoException(
                                            "produto.nao.encontrado", dados.codigo()));
        } catch (ArithmeticException exception) {
            throw new DadosInvalidosException("produto.estoque.limite.excedido", dados.codigo());
        }

        return estoque;
    }

    /**
     * Baixa estoque atomicamente; o estoque nunca fica negativo.
     *
     * @param dados código e quantidade em unidades
     * @return estoque após a saída
     * @throws ConstraintViolationException se algum campo for inválido
     * @throws ProdutoNaoEncontradoException se o produto não existir
     * @throws DadosInvalidosException se a entrada for nula ou a quantidade exceder o estoque
     */
    public Integer saida(MovimentoEstoqueDto dados) {
        validar(dados);

        return baixarItens(Map.of(dados.codigo(), dados.quantidade()))
                .get(dados.codigo())
                .estoque();
    }

    /**
     * Baixa vários produtos de uma só vez, todos ou nenhum.
     *
     * @param quantidadesPorCodigo unidades positivas por código positivo, sem nulos
     * @return preço e estoque resultante de cada código, na ordem do mapa recebido
     * @throws ProdutoNaoEncontradoException se algum produto não existir
     * @throws DadosInvalidosException se o mapa for nulo, vazio ou inválido, ou faltar estoque; os
     *     argumentos do estoque insuficiente são o disponível e o código
     */
    @Override
    public Map<Integer, ItemBaixado> baixarItens(Map<Integer, Integer> quantidadesPorCodigo) {
        validarBaixa(quantidadesPorCodigo);

        return switch (repository.baixarEstoque(quantidadesPorCodigo)) {
            case ResultadoBaixa.Baixado baixado -> {
                Map<Integer, ItemBaixado> itens = new LinkedHashMap<>();
                baixado.produtos()
                        .forEach((codigo, produto) ->
                                itens.put(
                                        codigo,
                                        new ItemBaixado(produto.getPreco(), produto.getEstoque())));
                yield itens;
            }
            case ResultadoBaixa.ProdutoInexistente inexistente ->
                    throw new ProdutoNaoEncontradoException(
                            "produto.nao.encontrado", inexistente.codigo());
            case ResultadoBaixa.EstoqueInsuficiente insuficiente ->
                    throw new DadosInvalidosException(
                            "produto.estoque.insuficiente",
                            insuficiente.disponivel(),
                            insuficiente.codigo());
        };
    }

    /** Garante a pré-condição da porta antes de qualquer efeito no armazenamento. */
    private void validarBaixa(Map<Integer, Integer> quantidadesPorCodigo) {
        if (quantidadesPorCodigo == null || quantidadesPorCodigo.isEmpty()) {
            throw new DadosInvalidosException("requisicao.dados.obrigatorios");
        }

        for (var item : quantidadesPorCodigo.entrySet()) {
            if (item.getKey() == null || item.getValue() == null) {
                throw new DadosInvalidosException("requisicao.dados.obrigatorios");
            }

            if (item.getKey() <= 0) {
                throw new DadosInvalidosException("produto.codigo.positivo");
            }

            if (item.getValue() <= 0) {
                throw new DadosInvalidosException("produto.quantidade.positiva");
            }
        }
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
