package br.ufrn.imd.ecommerce.produto.adaptador.api;

import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.SomarEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.EstoqueDto;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.ProdutoDto;
import br.ufrn.imd.ecommerce.produto.adaptador.api.mapper.ProdutoMapper;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.BuscarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import br.ufrn.imd.ecommerce.produto.dominio.excecao.DadosInvalidosException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador HTTP; converte transporte sem implementar regras do produto. */
@RestController
public class ProdutoController implements ProdutoApi {
    private final ProdutoService service;
    private final ProdutoMapper mapper;

    /**
     * Recebe dependências do adaptador.
     * @param service casos de uso
     * @param mapper conversor de transporte
     */
    public ProdutoController(ProdutoService service, ProdutoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<ProdutoDto> cadastrar(CadastrarProdutoRequest request) {
        exigirCorpo(request);
        var produto = service.cadastrar(mapper.paraCadastro(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.paraResposta(produto));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<EstoqueDto> consultar(Integer codigo) {
        return ResponseEntity.ok(mapper.paraEstoque(service.consultar(new BuscarProdutoDto(codigo))));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<EstoqueDto> entrada(Integer codigo, SomarEstoqueRequest request) {
        exigirCorpo(request);
        Integer estoque = service.entrada(mapper.paraReposicao(codigo, request));
        return ResponseEntity.ok(new EstoqueDto(codigo, estoque));
    }

    /** Impede conversões de um corpo nulo em DTOs com significado diferente. */
    private void exigirCorpo(Object request) {
        if (request == null) {
            throw new DadosInvalidosException("requisicao.dados.obrigatorios");
        }
    }
}
