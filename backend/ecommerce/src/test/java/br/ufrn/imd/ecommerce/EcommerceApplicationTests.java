package br.ufrn.imd.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.produto.adaptador.api.mapper.ProdutoMapper;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EcommerceApplicationTests {
    @Autowired private ProdutoRepository repository;
    @Autowired private ProdutoService service;
    @Autowired private ProdutoMapper mapper;
    @Autowired private Validator validator;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
        assertThat(service).isNotNull();
        assertThat(mapper).isNotNull();
        assertThat(validator).isNotNull();
    }
}
