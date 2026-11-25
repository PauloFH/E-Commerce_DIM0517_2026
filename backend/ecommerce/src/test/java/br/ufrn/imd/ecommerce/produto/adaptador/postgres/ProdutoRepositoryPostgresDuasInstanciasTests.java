package br.ufrn.imd.ecommerce.produto.adaptador.postgres;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ResultadoBaixa;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Duas instâncias da API, com pools separados, disputando os mesmos produtos no mesmo banco. */
class ProdutoRepositoryPostgresDuasInstanciasTests {
    private static final int THREADS = 16;
    private static final int OPERACOES = 100;
    private static final int ESTOQUE_INICIAL = 800;
    private static final int REPOSICOES = 4 * 50;

    @Test
    @Timeout(60)
    void baixasEmInstanciasDiferentesNaoDeixamEstoqueNegativoNemPerdemAtualizacoes()
            throws Exception {
        var pools =
                List.of(
                        BancoDeTeste.novoDataSource(THREADS + 4),
                        BancoDeTeste.novoDataSource(THREADS + 4));
        BancoDeTeste.limpo();
        var instancias = pools.stream().map(ProdutoRepositoryPostgres::new).toList();
        instancias.getFirst().inserir(new Produto(1, BigDecimal.ONE, ESTOQUE_INICIAL));
        instancias.getFirst().inserir(new Produto(2, BigDecimal.TEN, ESTOQUE_INICIAL));

        var inicio = new CountDownLatch(1);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int t = 0; t < THREADS; t++) {
            var instancia = instancias.get(t % 2);
            boolean ordemInvertida = t % 4 >= 2;
            tarefas.add(() -> {
                inicio.await();
                int baixados = 0;
                for (int i = 0; i < OPERACOES; i++) {
                    Map<Integer, Integer> quantidades = new LinkedHashMap<>();
                    quantidades.put(ordemInvertida ? 2 : 1, 1);
                    quantidades.put(ordemInvertida ? 1 : 2, 1);
                    if (instancia.baixarEstoque(quantidades) instanceof ResultadoBaixa.Baixado) {
                        baixados++;
                    }
                }
                return baixados;
            });
        }
        for (int t = 0; t < 4; t++) {
            var instancia = instancias.get(t % 2);
            tarefas.add(() -> {
                inicio.await();
                for (int i = 0; i < REPOSICOES / 4; i++) {
                    instancia.somarEstoque(1, 1);
                }
                return 0;
            });
        }

        int baixados = 0;
        try (var executor = Executors.newFixedThreadPool(tarefas.size())) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            inicio.countDown();
            for (var resultado : resultados) {
                baixados += resultado.get(50, TimeUnit.SECONDS);
            }
        }

        for (var instancia : instancias) {
            assertThat(instancia.buscar(1).orElseThrow().getEstoque())
                    .isEqualTo(ESTOQUE_INICIAL + REPOSICOES - baixados)
                    .isNotNegative();
            assertThat(instancia.buscar(2).orElseThrow().getEstoque())
                    .isEqualTo(ESTOQUE_INICIAL - baixados)
                    .isNotNegative();
        }
        assertThat(baixados).isGreaterThanOrEqualTo(ESTOQUE_INICIAL);
    }
}
