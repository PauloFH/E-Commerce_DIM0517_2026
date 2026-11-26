package br.ufrn.imd.ecommerce.venda.adaptador.api;

import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.requisicao.VendaRequest;
import br.ufrn.imd.ecommerce.venda.adaptador.api.dto.resposta.VendaResponse;
import br.ufrn.imd.ecommerce.venda.adaptador.api.mapper.VendaMapper;
import br.ufrn.imd.ecommerce.venda.aplicacao.servico.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador HTTP; converte transporte sem implementar regras da venda. */
@RestController
public class VendaController implements VendaApi {
    private final VendaService service;
    private final VendaMapper mapper;

    /**
     * Recebe dependências do adaptador.
     *
     * @param service casos de uso
     * @param mapper conversor de transporte
     */
    public VendaController(VendaService service, VendaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<VendaResponse> vender(VendaRequest request) {
        var resultado = service.vender(mapper.paraItens(request.itens()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.paraResposta(resultado));
    }
}
