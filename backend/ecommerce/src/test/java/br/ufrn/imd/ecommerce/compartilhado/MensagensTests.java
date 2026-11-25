package br.ufrn.imd.ecommerce.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Garante que as mensagens da API existem em português e inglês para toda chave do código. */
class MensagensTests {
    private static final Path FONTES = Path.of("src/main/java");

    /** Literal com chave de mensagem; não casa pacotes (sem aspas) nem "about:blank". */
    private static final Pattern CHAVE = Pattern.compile("\"([a-z]+(?:\\.[a-z]+)+)\"");

    private static final Pattern STATUS = Pattern.compile("HttpStatus\\.([A-Z_]+)");

    @Test
    void osDoisIdiomasTemExatamenteAsMesmasChaves() throws IOException {
        assertThat(chaves("/messages_en.properties")).isEqualTo(chaves("/messages.properties"));
    }

    @Test
    void todaChaveUsadaNoCodigoExisteNosDoisIdiomas() throws IOException {
        Set<String> usadas = chavesUsadasNoCodigo();

        assertThat(usadas).contains("produto.nao.encontrado", "produto.estoque.insuficiente");
        assertThat(chaves("/messages.properties")).containsAll(usadas);
        assertThat(chaves("/messages_en.properties")).containsAll(usadas);
    }

    @Test
    void existeTituloParaCadaStatusDoHandler() throws IOException {
        String handler =
                Files.readString(
                        FONTES.resolve(
                                "br/ufrn/imd/ecommerce/compartilhado/GlobalHttpExceptionHandler.java"),
                        StandardCharsets.UTF_8);
        Set<String> titulos = new TreeSet<>();
        STATUS.matcher(handler)
                .results()
                .forEach(m -> titulos.add("http." + HttpStatus.valueOf(m.group(1)).value()));

        assertThat(titulos).isNotEmpty();
        assertThat(chaves("/messages.properties")).containsAll(titulos);
        assertThat(chaves("/messages_en.properties")).containsAll(titulos);
    }

    private static Set<String> chavesUsadasNoCodigo() throws IOException {
        Set<String> chaves = new TreeSet<>();
        try (Stream<Path> arquivos = Files.walk(FONTES)) {
            for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                CHAVE.matcher(Files.readString(arquivo, StandardCharsets.UTF_8))
                        .results()
                        .forEach(m -> chaves.add(m.group(1)));
            }
        }
        return chaves;
    }

    private static Set<String> chaves(String recurso) throws IOException {
        Properties properties = new Properties();
        try (InputStream entrada = MensagensTests.class.getResourceAsStream(recurso)) {
            assertThat(entrada).as(recurso).isNotNull();
            properties.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        }
        return new TreeSet<>(properties.stringPropertyNames());
    }
}
