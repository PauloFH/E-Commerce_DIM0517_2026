package br.ufrn.imd.ecommerce;

import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class ArquiteturaTests {
    @Test
    void verificaFronteirasDosModulos() {
        var modules = ApplicationModules.of(EcommerceApplication.class);
        modules.verify();
        assertThat(modules.stream().map(module -> module.getIdentifier().toString()))
                .containsExactlyInAnyOrder("produto", "compartilhado");
    }

    @Test
    void dominioEAplicacaoNaoDependemDosAdaptadores() {
        var classes = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.ufrn.imd.ecommerce");
        classes().that().resideInAPackage("..produto.dominio..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..produto.dominio..")
                .check(classes);
        classes().that().resideInAPackage("..produto.aplicacao..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java..", "jakarta.validation..", "..produto.aplicacao..", "..produto.dominio..")
                .check(classes);
    }

    @Test
    void produtoTemSomenteTresAtributos() {
        var atributos = Arrays.stream(Produto.class.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, Field::getType));
        assertThat(atributos).isEqualTo(Map.of(
                "codigo", Integer.class, "preco", BigDecimal.class, "estoque", Integer.class));
    }
}
