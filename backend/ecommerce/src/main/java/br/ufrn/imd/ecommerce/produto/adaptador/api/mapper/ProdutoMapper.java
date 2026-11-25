package br.ufrn.imd.ecommerce.produto.adaptador.api.mapper;

import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.SomarEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.EstoqueDto;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.ProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.CadastrarProdutoDto;
import br.ufrn.imd.ecommerce.produto.aplicacao.dto.SomarEstoqueDto;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** Conversões da fronteira HTTP; não executa regras de negócio. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProdutoMapper {
    /**
     * Converte o corpo HTTP sem alterar seus valores.
     * @param request corpo do cadastro
     * @return entrada do caso de uso
     */
    CadastrarProdutoDto paraCadastro(CadastrarProdutoRequest request);

    /**
     * Combina código da rota e quantidade do corpo.
     * @param codigo identificador da rota
     * @param request corpo da reposição
     * @return entrada do caso de uso
     */
    SomarEstoqueDto paraReposicao(Integer codigo, SomarEstoqueRequest request);

    /**
     * Converte o produto em representação de transporte.
     * @param produto produto encontrado
     * @return resposta HTTP
     */
    ProdutoDto paraResposta(Produto produto);

    /**
     * Expõe somente código e estoque na consulta.
     * @param produto produto encontrado
     * @return resposta sem preço
     */
    EstoqueDto paraEstoque(Produto produto);
}
