package br.ufrn.imd.ecommerce.produto.aplicacao.porta;

import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.util.Map;
import java.util.Optional;

/**
 * Porta de armazenamento. Cada operação deve ser atômica para todos os seus itens. Recebe códigos
 * positivos, produtos válidos e quantidades positivas, sem valores nulos. Adaptadores de banco
 * devem preservar estas garantias entre instâncias da aplicação.
 */
public interface ProdutoRepository {
    /**
     * Consulta um retrato imutável do produto.
     *
     * @param codigo identificador
     * @return produto ou vazio se ausente
     */
    Optional<Produto> buscar(Integer codigo);

    /**
     * Insere sem sobrescrever um produto existente.
     *
     * @param produto produto a armazenar
     * @return verdadeiro se inserido; falso se o código já existe
     */
    boolean inserir(Produto produto);

    /**
     * Acrescenta unidades atomicamente, sem alteração em caso de estouro.
     *
     * @param codigo identificador
     * @param quantidade unidades positivas a acrescentar
     * @return novo estoque ou vazio se o produto não existe
     * @throws ArithmeticException se o estoque ultrapassar Integer.MAX_VALUE
     */
    Optional<Integer> somarEstoque(Integer codigo, int quantidade);

    /**
     * Baixa todos os itens ou nenhum; mapa vazio é uma operação bem-sucedida. Se algum código não
     * existe, o resultado é {@code ProdutoInexistente} do primeiro código inexistente na ordem de
     * iteração do mapa, mesmo que outro item esteja sem estoque; senão, {@code EstoqueInsuficiente}
     * do primeiro item sem estoque. Nesses casos nada é alterado.
     *
     * @param quantidadesPorCodigo unidades positivas por código, sem valores nulos
     * @return {@code Baixado} com os produtos já atualizados, ou o motivo da recusa
     */
    ResultadoBaixa baixarEstoque(Map<Integer, Integer> quantidadesPorCodigo);
}
