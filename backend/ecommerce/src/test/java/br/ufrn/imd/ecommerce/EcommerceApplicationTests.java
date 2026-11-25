package br.ufrn.imd.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.compartilhado.GlobalHttpExceptionHandler;
import br.ufrn.imd.ecommerce.produto.adaptador.api.mapper.ProdutoMapper;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import jakarta.validation.Validator;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
class EcommerceApplicationTests {
    @Autowired private ProdutoRepository repository;
    @Autowired private ProdutoService service;
    @Autowired private ProdutoMapper mapper;
    @Autowired private Validator validator;

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Autowired private GlobalHttpExceptionHandler handler;
    @Autowired private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
        assertThat(service).isNotNull();
        assertThat(mapper).isNotNull();
        assertThat(validator).isNotNull();
        assertThat(ReflectionTestUtils.getField(handler, "mensagens"))
                .isSameAs(context.getBean("messageSource"));
        assertThat(messageSource.getMessage("http.404", null, Locale.ENGLISH))
                .isEqualTo("Not Found");
        assertThat(messageSource.getMessage("http.404", null, Locale.forLanguageTag("pt-BR")))
                .isEqualTo("Não encontrado");
    }
}
