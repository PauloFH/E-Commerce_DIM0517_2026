package br.ufrn.imd.ecommerce.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import br.ufrn.imd.ecommerce.auditoria.registro.MovimentoEstoque;
import br.ufrn.imd.ecommerce.auditoria.registro.MovimentoEstoque.Tipo;
import br.ufrn.imd.ecommerce.auditoria.registro.MovimentosEmMemoria;
import br.ufrn.imd.ecommerce.produto.adaptador.api.ProdutoApi;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.MovimentoEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import br.ufrn.imd.ecommerce.venda.VendaRealizada;
import br.ufrn.imd.ecommerce.venda.adaptador.api.VendaApi;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.ItemVendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.VendaRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@ApplicationModuleTest(extraIncludes = {"produto", "venda", "compartilhado"})
class AuditoriaMemoriaTests {
    @Autowired private ProdutoApi produtos;
    @Autowired private VendaApi vendas;
    @Autowired private MovimentosEmMemoria movimentos;

    @Test
    void registraEntradaSaidaEVendaAgrupada(Scenario scenario) {
        produtos.cadastrar(new CadastrarProdutoRequest(321, new BigDecimal("2.50")));
        produtos.entrada(321, new MovimentoEstoqueRequest(10));
        produtos.saida(321, new MovimentoEstoqueRequest(2));
        assertThatThrownBy(() -> produtos.saida(321, new MovimentoEstoqueRequest(9)))
                .isInstanceOf(DadosInvalidosException.class);

        var pedido =
                new VendaRequest(
                        List.of(new ItemVendaRequest(321, 1), new ItemVendaRequest(321, 2)));

        scenario.stimulate(tx -> vendas.vender(pedido))
                .andWaitForStateChange(movimentos::consultar, registros -> registros.size() >= 3)
                .andVerifyEvents(eventos ->
                        assertThat(eventos.ofType(VendaRealizada.class))
                                .extracting(VendaRealizada::itens)
                                .containsExactly(List.of(new VendaRealizada.Item(321, 3, 5))));

        assertThat(movimentos.consultar())
                .extracting(
                        MovimentoEstoque::tipo,
                        MovimentoEstoque::codigo,
                        MovimentoEstoque::quantidade,
                        MovimentoEstoque::estoqueResultante)
                .containsExactlyInAnyOrder(
                        tuple(Tipo.ENTRADA, 321, 10, 10),
                        tuple(Tipo.SAIDA, 321, 2, 8),
                        tuple(Tipo.VENDA, 321, 3, 5));
    }
}
