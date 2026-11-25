package br.ufrn.imd.ecommerce.produto.config;

import br.ufrn.imd.ecommerce.produto.adaptador.memoria.ProdutoRepositoryEmMemoria;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composição Spring das dependências do módulo produto. */
@Configuration(proxyBeanMethods = false)
public class ProdutoConfig {
    /** @return armazenamento volátil usado enquanto não houver adaptador de banco */
    @Bean
    public ProdutoRepository produtoRepository() {
        return new ProdutoRepositoryEmMemoria();
    }

    /**
     * Constrói o serviço sem anotações Spring na aplicação.
     * @param repository porta de armazenamento
     * @param validator validador Jakarta fornecido pelo Boot
     * @return serviço funcional
     */
    @Bean
    public ProdutoService produtoService(ProdutoRepository repository, Validator validator) {
        return new ProdutoService(repository, validator);
    }
}
