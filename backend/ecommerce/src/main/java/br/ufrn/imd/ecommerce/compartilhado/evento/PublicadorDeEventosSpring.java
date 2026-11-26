package br.ufrn.imd.ecommerce.compartilhado.evento;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Entrega os eventos de domínio ao mecanismo de publicação do Spring. */
@Component
class PublicadorDeEventosSpring implements PublicadorDeEventos {
    private final ApplicationEventPublisher publisher;

    PublicadorDeEventosSpring(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(Object evento) {
        publisher.publishEvent(evento);
    }
}
