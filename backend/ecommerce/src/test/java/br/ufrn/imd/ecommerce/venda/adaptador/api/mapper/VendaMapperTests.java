package br.ufrn.imd.ecommerce.venda.adaptador.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.ItemVendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.ItemVendidoResponse;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.VendaResponse;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVenda;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVendido;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ResultadoVenda;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class VendaMapperTests {
    private final VendaMapper mapper = Mappers.getMapper(VendaMapper.class);

    @Test
    void converteItensERespostaSemAlterarValores() {
        var itens =
                mapper.paraItens(List.of(new ItemVendaRequest(1, 2), new ItemVendaRequest(3, 4)));
        assertThat(itens).containsExactly(new ItemVenda(1, 2), new ItemVenda(3, 4));
        var resposta =
                mapper.paraResposta(
                        new ResultadoVenda(
                                new BigDecimal("9.90"),
                                List.of(new ItemVendido(1, 2, new BigDecimal("9.90"), 5))));
        assertThat(resposta)
                .isEqualTo(
                        new VendaResponse(
                                new BigDecimal("9.90"),
                                List.of(new ItemVendidoResponse(1, 2, new BigDecimal("9.90"), 5))));
    }

    @Test
    void preservaNulosParaOServicoRecusar() {
        assertThat(mapper.paraItens(null)).isNull();
        assertThat(mapper.paraItens(Arrays.asList(new ItemVendaRequest(1, 1), null)))
                .containsExactly(new ItemVenda(1, 1), null);
        assertThat(mapper.paraItem((ItemVendaRequest) null)).isNull();
        assertThat(mapper.paraItem((ItemVendido) null)).isNull();
        assertThat(mapper.paraResposta(null)).isNull();
    }
}
