package br.ufrn.imd.ecommerce.produto.adaptador.memoria;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.*;

class ProdutoRepositoryEmMemoriaTests {
    private ProdutoRepositoryEmMemoria repository;

    @BeforeEach
    void criarRepository() {
        repository = new ProdutoRepositoryEmMemoria();
        repository.inserir(new Produto(1, BigDecimal.ONE, 100));
        repository.inserir(new Produto(2, BigDecimal.TEN, 100));
    }

    @Test
    void consultaCadastroESomaPreservamValores() {
        assertThat(repository.buscar(99)).isEmpty();
        assertThat(repository.somarEstoque(99, 1)).isEmpty();
        assertThat(repository.inserir(new Produto(1, BigDecimal.ZERO, 0))).isFalse();
        Produto anterior = repository.buscar(1).orElseThrow();
        assertThat(repository.somarEstoque(1, 5)).contains(105);
        assertThat(anterior.getEstoque()).isEqualTo(100);
        assertThat(repository.buscar(1).orElseThrow().getPreco()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void baixaTudoOuNada() {
        Map<Integer, Integer> quantidades = new LinkedHashMap<>();
        quantidades.put(1, 10);
        quantidades.put(2, 101);
        assertThat(repository.baixarEstoque(quantidades)).isFalse();
        assertEstoques(100, 100);
        assertThat(repository.baixarEstoque(Map.of(1, 10, 99, 1))).isFalse();
        assertEstoques(100, 100);
        assertThat(repository.baixarEstoque(Map.of(1, 10, 2, 100))).isTrue();
        assertEstoques(90, 0);
        assertThat(repository.baixarEstoque(Map.of())).isTrue();
    }

    @Test
    void estouroOuQuantidadeInvalidaNaoAlteramProdutos() {
        repository.inserir(new Produto(3, BigDecimal.ONE, Integer.MAX_VALUE));
        assertThatThrownBy(() -> repository.somarEstoque(3, 1)).isInstanceOf(ArithmeticException.class);
        assertThat(repository.buscar(3).orElseThrow().getEstoque()).isEqualTo(Integer.MAX_VALUE);
        assertThatThrownBy(() -> repository.baixarEstoque(Map.of(1, 1, 2, -1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertEstoques(100, 100);
    }

    @Test
    @Timeout(15)
    void reposicoesEBaixasConcorrentesNaoPerdemAtualizacoes() throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        List<Callable<Void>> tarefas = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tarefas.add(() -> {
                inicio.await();
                for (int j = 0; j < 100; j++) {
                    repository.somarEstoque(1, 1);
                    repository.somarEstoque(2, 1);
                    assertThat(repository.baixarEstoque(Map.of(1, 1, 2, 1))).isTrue();
                }
                return null;
            });
        }

        try (var executor = Executors.newFixedThreadPool(8)) {
            var resultados = tarefas.stream().map(executor::submit).toList();
            inicio.countDown();
            for (var resultado : resultados) {
                resultado.get(10, TimeUnit.SECONDS);
            }
        }
        assertEstoques(100, 100);
    }

    @Test
    @Timeout(15)
    void baixasConcorrentesNaoConsomemAlemDoDisponivel() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<Boolean>> tarefas = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                tarefas.add(() -> repository.baixarEstoque(Map.of(1, 1, 2, 1)));
            }
            int sucessos = 0;
            for (var resultado : executor.invokeAll(tarefas)) {
                if (resultado.get()) {
                    sucessos++;
                }
            }
            assertThat(sucessos).isEqualTo(100);
        }
        assertEstoques(0, 0);
    }

    private void assertEstoques(int primeiro, int segundo) {
        assertThat(repository.buscar(1).orElseThrow().getEstoque()).isEqualTo(primeiro);
        assertThat(repository.buscar(2).orElseThrow().getEstoque()).isEqualTo(segundo);
    }
}
