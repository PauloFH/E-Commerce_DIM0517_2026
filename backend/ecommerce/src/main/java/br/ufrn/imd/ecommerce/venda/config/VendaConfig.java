package br.ufrn.imd.ecommerce.venda.config;

import br.ufrn.imd.ecommerce.produto.estoque.BaixaDeEstoque;
import br.ufrn.imd.ecommerce.venda.aplicacao.servico.VendaService;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composição Spring das dependências do módulo venda. */
@Configuration(proxyBeanMethods = false)
public class VendaConfig {
    /**
     * Constrói o serviço sem anotações Spring na aplicação.
     *
     * @param baixaDeEstoque API pública do módulo produto
     * @param validator validador Jakarta fornecido pelo Boot
     * @return serviço funcional
     */
    @Bean
    public VendaService vendaService(BaixaDeEstoque baixaDeEstoque, Validator validator) {
        return new VendaService(baixaDeEstoque, validator);
    }
}
