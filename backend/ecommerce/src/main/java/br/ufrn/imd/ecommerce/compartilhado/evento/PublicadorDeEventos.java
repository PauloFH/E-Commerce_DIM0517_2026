package br.ufrn.imd.ecommerce.compartilhado.evento;

/** Porta de publicação de eventos de domínio. */
@FunctionalInterface
public interface PublicadorDeEventos {
    /** Publica um evento de uma operação concluída. */
    void publicar(Object evento);
}
