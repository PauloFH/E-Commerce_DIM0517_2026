package br.ufrn.imd.ecommerce.produto.adaptador.api.mapper;

import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.CadastrarProdutoRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.requisicao.SomarEstoqueRequest;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.EstoqueDto;
import br.ufrn.imd.ecommerce.produto.adaptador.api.dto.resposta.ProdutoDto;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoMapperTests {
    private final ProdutoMapper mapper = Mappers.getMapper(ProdutoMapper.class);

    @Test
    void converteCadastroReposicaoERespostaSemAlterarValores() {
        var entrada = mapper.paraCadastro(new CadastrarProdutoRequest(1, new BigDecimal("10.50")));
        assertThat(entrada.codigo()).isEqualTo(1);
        assertThat(entrada.preco()).isEqualByComparingTo("10.50");
        var reposicao = mapper.paraReposicao(1, new SomarEstoqueRequest(3));
        assertThat(reposicao.codigo()).isEqualTo(1);
        assertThat(reposicao.quantidade()).isEqualTo(3);
        var resposta = mapper.paraResposta(new Produto(1, entrada.preco(), 8));
        assertThat(resposta).isEqualTo(new ProdutoDto(1, entrada.preco(), 8));
        assertThat(mapper.paraEstoque(new Produto(1, entrada.preco(), 8)))
                .isEqualTo(new EstoqueDto(1, 8));
    }
}
