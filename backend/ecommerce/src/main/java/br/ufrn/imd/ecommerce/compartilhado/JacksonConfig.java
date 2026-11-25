package br.ufrn.imd.ecommerce.compartilhado;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

/** Configuração de tipos JSON usada por todos os adaptadores HTTP. */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {
    /**
     * Impede arredondamento de decimais e conversão de texto ou booleanos em inteiros.
     * @return customização do JsonMapper fornecido pelo Boot
     */
    @Bean
    public JsonMapperBuilderCustomizer inteirosEstritos() {
        return builder -> builder.withCoercionConfig(LogicalType.Integer, config -> config
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
