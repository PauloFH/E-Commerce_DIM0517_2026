package br.ufrn.imd.ecommerce.produto.adaptador.memoria;

import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Armazenamento volátil com exclusão mútua entre todas as operações.
 * A atomicidade vale somente para esta instância; reiniciar o processo apaga os dados.
 */
public final class ProdutoRepositoryEmMemoria implements ProdutoRepository {
    // HashMap basta: todo acesso passa pelo monitor desta instância (synchronized).
    private final Map<Integer, Produto> produtos = new HashMap<>();

    /** Cria um armazenamento vazio, sem dependências externas. */
    public ProdutoRepositoryEmMemoria() {
    }

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
    public synchronized boolean baixarEstoque(Map<Integer, Integer> quantidadesPorCodigo) {
        Map<Integer, Integer> quantidades = Map.copyOf(quantidadesPorCodigo);

        for (var item : quantidades.entrySet()) {
            if (item.getValue() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser positiva");
            }
        }

        for (var item : quantidades.entrySet()) {
            Produto produto = produtos.get(item.getKey());

            if (produto == null || produto.getEstoque() < item.getValue()) {
                return false;
            }
        }

        for (var item : quantidades.entrySet()) {
            Produto produto = produtos.get(item.getKey());
            produtos.put(item.getKey(), new Produto(produto.getCodigo(), produto.getPreco(),
                    produto.getEstoque() - item.getValue()));
        }

        return true;
    }
}
