package br.ufrn.imd.ecommerce.venda.adaptador.api.mapper;

import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.ItemVendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.ItemVendidoResponse;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.VendaResponse;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVenda;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ItemVendido;
import br.ufrn.imd.ecommerce.venda.aplicacao.dto.ResultadoVenda;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Conversões da fronteira HTTP; não executa regras de negócio. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface VendaMapper {
    /**
     * Converte os itens do corpo HTTP sem alterar seus valores; lista e itens nulos permanecem
     * nulos para o serviço recusar.
     *
     * @param itens itens do corpo da venda
     * @return entrada do caso de uso
     */
    List<ItemVenda> paraItens(List<ItemVendaRequest> itens);

    /**
     * Converte um item do corpo HTTP.
     *
     * @param item item do corpo da venda
     * @return entrada do caso de uso
     */
    ItemVenda paraItem(ItemVendaRequest item);

    /**
     * Converte o resultado da venda em representação de transporte.
     *
     * @param resultado venda concluída
     * @return resposta HTTP
     */
    VendaResponse paraResposta(ResultadoVenda resultado);

    /**
     * Converte um item vendido em representação de transporte.
     *
     * @param item item vendido
     * @return item da resposta HTTP
     */
    ItemVendidoResponse paraItem(ItemVendido item);
}
