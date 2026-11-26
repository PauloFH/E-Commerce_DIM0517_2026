package br.ufrn.imd.ecommerce.compartilhado.config;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/** Seleciona o idioma das respostas pelo cabeçalho Accept-Language. */
@Configuration(proxyBeanMethods = false)
public class LocaleConfig {
    /**
     * @return catálogo de mensagens traduzidas da API
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource mensagens = new ResourceBundleMessageSource();
        mensagens.setBasename("messages");
        mensagens.setDefaultEncoding("UTF-8");
        mensagens.setFallbackToSystemLocale(false);
        return mensagens;
    }

    /**
     * @return resolvedor com português brasileiro como idioma padrão
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolvedor = new AcceptHeaderLocaleResolver();
        resolvedor.setSupportedLocales(
                List.of(Locale.forLanguageTag("pt-BR"), Locale.of("pt"), Locale.ENGLISH));
        resolvedor.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        return resolvedor;
    }
}
