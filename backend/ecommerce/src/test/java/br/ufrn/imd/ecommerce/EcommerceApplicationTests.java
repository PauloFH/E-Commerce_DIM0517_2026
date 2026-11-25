package br.ufrn.imd.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.produto.BaixaDeEstoque;
import br.ufrn.imd.ecommerce.produto.adaptador.api.mapper.ProdutoMapper;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import br.ufrn.imd.ecommerce.venda.adaptador.api.mapper.VendaMapper;
import br.ufrn.imd.ecommerce.venda.aplicacao.servico.VendaService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EcommerceApplicationTests {
    @Autowired private ProdutoRepository repository;
    @Autowired private ProdutoService service;
    @Autowired private ProdutoMapper mapper;
    @Autowired private BaixaDeEstoque baixaDeEstoque;
    @Autowired private VendaService vendaService;
    @Autowired private VendaMapper vendaMapper;
    @Autowired private Validator validator;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
        assertThat(service).isNotNull();
        assertThat(mapper).isNotNull();
        assertThat(validator).isNotNull();
        assertThat(baixaDeEstoque).isSameAs(service);
        assertThat(vendaService).isNotNull();
        assertThat(vendaMapper).isNotNull();
    }
}
