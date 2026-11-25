package br.ufrn.imd.ecommerce.produto.adaptador.memoria;

import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ResultadoBaixa;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Armazenamento volátil com exclusão mútua entre todas as operações. A atomicidade vale somente
 * para esta instância; reiniciar o processo apaga os dados.
 */
public final class ProdutoRepositoryEmMemoria implements ProdutoRepository {
    // HashMap basta: todo acesso passa pelo monitor desta instância (synchronized).
    private final Map<Integer, Produto> produtos = new HashMap<>();

    /** Cria um armazenamento vazio, sem dependências externas. */
    public ProdutoRepositoryEmMemoria() {}

    /** {@inheritDoc} A consulta não observa baixas parcialmente aplicadas. */
    @Override
    public synchronized Optional<Produto> buscar(Integer codigo) {
        return Optional.ofNullable(produtos.get(codigo));
    }

    /** {@inheritDoc} A verificação e a inserção usam o mesmo monitor das baixas. */
    @Override
    public synchronized boolean inserir(Produto produto) {
        return produtos.putIfAbsent(produto.getCodigo(), produto) == null;
    }

    /** {@inheritDoc} Substitui o produto somente após calcular o novo estoque. */
    @Override
    public synchronized Optional<Integer> somarEstoque(Integer codigo, int quantidade) {
        Produto produto = produtos.get(codigo);
        Optional<Integer> resultado = Optional.empty();

        if (produto != null) {
            int estoque = Math.addExact(produto.getEstoque(), quantidade);
            produtos.put(codigo, new Produto(codigo, produto.getPreco(), estoque));
            resultado = Optional.of(estoque);
        }

        return resultado;
    }

    /** {@inheritDoc} Confere todos os itens antes de publicar qualquer alteração. */
    @Override
    public synchronized ResultadoBaixa baixarEstoque(Map<Integer, Integer> quantidadesPorCodigo) {
        // LinkedHashMap preserva a ordem de iteração do mapa recebido, que define o "primeiro".
        Map<Integer, Integer> quantidades = new LinkedHashMap<>(quantidadesPorCodigo);

        for (var item : quantidades.entrySet()) {
            if (item.getValue() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser positiva");
            }
        }

        for (var codigo : quantidades.keySet()) {
            if (!produtos.containsKey(codigo)) {
                return new ResultadoBaixa.ProdutoInexistente(codigo);
            }
        }

        for (var item : quantidades.entrySet()) {
            int disponivel = produtos.get(item.getKey()).getEstoque();

            if (disponivel < item.getValue()) {
                return new ResultadoBaixa.EstoqueInsuficiente(item.getKey(), disponivel);
            }
        }

        Map<Integer, Produto> baixados = new LinkedHashMap<>();

        for (var item : quantidades.entrySet()) {
            Produto produto = produtos.get(item.getKey());
            Produto atualizado =
                    new Produto(
                            produto.getCodigo(),
                            produto.getPreco(),
                            produto.getEstoque() - item.getValue());
            produtos.put(item.getKey(), atualizado);
            baixados.put(item.getKey(), atualizado);
        }

        return new ResultadoBaixa.Baixado(Collections.unmodifiableMap(baixados));
    }
}
