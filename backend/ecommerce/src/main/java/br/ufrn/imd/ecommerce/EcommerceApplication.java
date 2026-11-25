package br.ufrn.imd.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Ponto de entrada e pacote raiz dos módulos da loja. */
@SpringBootApplication
public class EcommerceApplication {

    /**
     * Inicia a API HTTP e configura os adaptadores.
     *
     * @param args argumentos de inicialização do Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
