package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ResultadoBaixa;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Armazenamento em PostgreSQL. A consistência do estoque fica com o banco, sem lock na aplicação: a
 * soma é um único UPDATE e a baixa trava as linhas com {@code ORDER BY codigo FOR UPDATE} em ordem
 * de código, o que vale entre instâncias da API e evita deadlock.
 */
public final class ProdutoRepositoryPostgres implements ProdutoRepository {
    private static final String ESTOURO_NUMERICO = "22003";

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;

    /**
     * @param dataSource conexões com o banco, já migrado pelo Flyway
     */
    public ProdutoRepositoryPostgres(DataSource dataSource) {
        this.jdbc = JdbcClient.create(dataSource);
        this.transacao = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @Override
    public Optional<Produto> buscar(Integer codigo) {
        return jdbc.sql("SELECT codigo, preco, estoque FROM produto WHERE codigo = ?")
                .param(codigo)
                .query(this::mapear)
                .optional();
    }

    /** {@inheritDoc} O conflito de código é resolvido pelo banco, sem sobrescrever. */
    @Override
    public boolean inserir(Produto produto) {
        return jdbc.sql(
                                "INSERT INTO produto (codigo, preco, estoque) VALUES (?, ?, ?)"
                                        + " ON CONFLICT (codigo) DO NOTHING")
                        .params(produto.getCodigo(), produto.getPreco(), produto.getEstoque())
                        .update()
                > 0;
    }

    /** {@inheritDoc} O estouro do integer vira {@link ArithmeticException}, sem alterar a linha. */
    @Override
    public Optional<Integer> somarEstoque(Integer codigo, int quantidade) {
        try {
            return jdbc.sql(
                            "UPDATE produto SET estoque = estoque + ? WHERE codigo = ?"
                                    + " RETURNING estoque")
                    .params(quantidade, codigo)
                    .query(Integer.class)
                    .optional();
        } catch (DataAccessException e) {
            if (e.getRootCause() instanceof SQLException sql
                    && ESTOURO_NUMERICO.equals(sql.getSQLState())) {
                throw new ArithmeticException("Estoque ultrapassa o limite de inteiro");
            }
            throw e;
        }
    }

    /**
     * {@inheritDoc} Trava os produtos em ordem de código e decide o resultado percorrendo o mapa na
     * ordem recebida; só então atualiza. Em recusa a transação é desfeita.
     */
    @Override
    public ResultadoBaixa baixarEstoque(Map<Integer, Integer> quantidadesPorCodigo) {
        // LinkedHashMap preserva a ordem de iteração do mapa recebido, que define o "primeiro".
        Map<Integer, Integer> quantidades = new LinkedHashMap<>(quantidadesPorCodigo);

        for (var item : quantidades.entrySet()) {
            if (item.getValue() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser positiva");
            }
        }

        if (quantidades.isEmpty()) {
            return new ResultadoBaixa.Baixado(Map.of());
        }

        return transacao.execute(status -> {
            Map<Integer, Produto> atuais = travar(quantidades);
            Optional<ResultadoBaixa> recusa = recusar(quantidades, atuais);

            if (recusa.isPresent()) {
                status.setRollbackOnly();
                return recusa.get();
            }

            return aplicar(quantidades, atuais);
        });
    }

    private Map<Integer, Produto> travar(Map<Integer, Integer> quantidades) {
        Integer[] codigos = quantidades.keySet().toArray(Integer[]::new);
        Map<Integer, Produto> travados = new LinkedHashMap<>();
        jdbc.sql(
                        "SELECT codigo, preco, estoque FROM produto WHERE codigo = ANY(?)"
                                + " ORDER BY codigo FOR UPDATE")
                .param(codigos)
                .query(this::mapear)
                .list()
                .forEach(produto -> travados.put(produto.getCodigo(), produto));
        return travados;
    }

    private Optional<ResultadoBaixa> recusar(
            Map<Integer, Integer> quantidades, Map<Integer, Produto> atuais) {
        for (var codigo : quantidades.keySet()) {
            if (!atuais.containsKey(codigo)) {
                return Optional.of(new ResultadoBaixa.ProdutoInexistente(codigo));
            }
        }

        for (var item : quantidades.entrySet()) {
            int disponivel = atuais.get(item.getKey()).getEstoque();

            if (disponivel < item.getValue()) {
                return Optional.of(
                        new ResultadoBaixa.EstoqueInsuficiente(item.getKey(), disponivel));
            }
        }

        return Optional.empty();
    }

    private ResultadoBaixa aplicar(
            Map<Integer, Integer> quantidades, Map<Integer, Produto> atuais) {
        Map<Integer, Produto> baixados = new LinkedHashMap<>();

        for (var item : quantidades.entrySet()) {
            int estoque =
                    jdbc.sql(
                                    "UPDATE produto SET estoque = estoque - ? WHERE codigo = ?"
                                            + " RETURNING estoque")
                            .params(item.getValue(), item.getKey())
                            .query(Integer.class)
                            .single();
            baixados.put(
                    item.getKey(),
                    new Produto(item.getKey(), atuais.get(item.getKey()).getPreco(), estoque));
        }

        return new ResultadoBaixa.Baixado(Collections.unmodifiableMap(baixados));
    }

    private Produto mapear(ResultSet rs, int linha) throws SQLException {
        return new Produto(rs.getInt("codigo"), rs.getBigDecimal("preco"), rs.getInt("estoque"));
    }
}
