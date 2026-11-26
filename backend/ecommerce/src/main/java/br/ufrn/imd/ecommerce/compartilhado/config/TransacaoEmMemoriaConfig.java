package br.ufrn.imd.ecommerce.compartilhado.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/** Habilita a entrega após o commit no perfil que não possui banco. */
@Configuration(proxyBeanMethods = false)
@Profile("!postgres")
@EnableAsync
class TransacaoEmMemoriaConfig {
    @Bean
    PlatformTransactionManager transactionManager() {
        return new TransacaoEmMemoria();
    }

    /** Sinaliza a conclusão da operação sem controlar o estado do estoque em memória. */
    private static final class TransacaoEmMemoria extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {}

        @Override
        protected void doCommit(DefaultTransactionStatus status) {}

        @Override
        protected void doRollback(DefaultTransactionStatus status) {}
    }
}
