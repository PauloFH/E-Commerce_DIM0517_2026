package br.ufrn.imd.ecommerce;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.type;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import br.ufrn.imd.ecommerce.compartilhado.ExcecaoNegocio;
import br.ufrn.imd.ecommerce.produto.dominio.modelo.Produto;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ArquiteturaTests {
    private static final JavaClasses CLASSES =
            new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("br.ufrn.imd.ecommerce");

    @Test
    void verificaFronteirasDosModulos() {
        var modules = ApplicationModules.of(EcommerceApplication.class);
        modules.verify();
        // contains, e não containsExactly: novos módulos não quebram o teste
        assertThat(modules.stream().map(module -> module.getIdentifier().toString()))
                .contains("produto", "venda", "compartilhado");
    }

    @Test
    void geraDocumentacaoDosModulos() {
        // diagramas C4/PlantUML e canvas de cada módulo em target/spring-modulith-docs
        new Documenter(ApplicationModules.of(EcommerceApplication.class)).writeDocumentation();
    }

    @Test
    void dominioEAplicacaoNaoDependemDosAdaptadores() {
        var contratoNegocio =
                type(ExcecaoNegocio.class)
                        .or(type(ExcecaoNegocio.Tipo.class))
                        .or(type(ExcecaoNegocio.Tipo[].class));
        classes()
                .that()
                .resideInAPackage("..dominio..")
                .should()
                .onlyDependOnClassesThat(
                        resideInAnyPackage("java..", "..dominio..").or(contratoNegocio))
                .check(CLASSES);
        classes()
                .that(contratoNegocio)
                .should()
                .onlyDependOnClassesThat(resideInAnyPackage("java..").or(contratoNegocio))
                .check(CLASSES);
        // Os contratos públicos são expostos na raiz e em interfaces nomeadas.
        classes()
                .that()
                .resideInAPackage("..aplicacao..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "java..",
                        "jakarta.validation..",
                        "..aplicacao..",
                        "..dominio..",
                        "br.ufrn.imd.ecommerce.produto",
                        "br.ufrn.imd.ecommerce.produto.estoque")
                .check(CLASSES);
    }

    @Test
    void apiNaoAcessaPersistenciaNemRepositorio() {
        noClasses()
                .that()
                .resideInAPackage("..adaptador.api..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..adaptador.memoria..", "..adaptador.postgres..", "..aplicacao.porta..")
                .because("o controller só conversa com o service")
                .check(CLASSES);
    }

    @Test
    void vendaSoUsaAApiPublicaDeProduto() {
        noClasses()
                .that()
                .resideInAPackage("..venda..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..produto.aplicacao..",
                        "..produto.dominio..",
                        "..produto.adaptador..",
                        "..produto.config..")
                .because("outros módulos só enxergam os contratos públicos de produto")
                .check(CLASSES);
    }

    @Test
    void produtoTemSomenteTresAtributos() {
        var atributos =
                Arrays.stream(Produto.class.getDeclaredFields())
                        .collect(Collectors.toMap(Field::getName, Field::getType));
        assertThat(atributos)
                .isEqualTo(
                        Map.of(
                                "codigo",
                                Integer.class,
                                "preco",
                                BigDecimal.class,
                                "estoque",
                                Integer.class));
    }
}
