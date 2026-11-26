package br.ufrn.imd.ecommerce.produto.config;

import br.ufrn.imd.ecommerce.compartilhado.evento.PublicadorDeEventos;
import br.ufrn.imd.ecommerce.produto.adaptador.memoria.ProdutoRepositoryEmMemoria;
import br.ufrn.imd.ecommerce.produto.adaptador.postgres.ProdutoRepositoryPostgres;
import br.ufrn.imd.ecommerce.produto.aplicacao.porta.ProdutoRepository;
import br.ufrn.imd.ecommerce.produto.aplicacao.servico.ProdutoService;
import jakarta.validation.Validator;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Composição Spring das dependências do módulo produto. */
@Configuration(proxyBeanMethods = false)
public class ProdutoConfig {
    /**
     * @return armazenamento volátil, padrão quando o profile postgres não está ativo
     */
    @Bean
    @Profile("!postgres")
    public ProdutoRepository produtoRepositoryEmMemoria() {
        return new ProdutoRepositoryEmMemoria();
    }

    /**
     * @param dataSource conexões com o banco migrado pelo Flyway
     * @return armazenamento durável, ativo somente no profile postgres
     */
    @Bean
    @Profile("postgres")
    public ProdutoRepository produtoRepositoryPostgres(DataSource dataSource) {
        return new ProdutoRepositoryPostgres(dataSource);
    }

    /**
     * Constrói o serviço sem anotações Spring na aplicação.
     *
     * @param repository porta de armazenamento
     * @param validator validador Jakarta fornecido pelo Boot
     * @param publicador porta de publicação de eventos
     * @return serviço funcional
     */
    @Bean
    public ProdutoService produtoService(
            ProdutoRepository repository, Validator validator, PublicadorDeEventos publicador) {
        return new ProdutoService(repository, validator, publicador);
    }
}
