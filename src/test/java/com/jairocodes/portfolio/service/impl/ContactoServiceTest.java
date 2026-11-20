package com.jairocodes.portfolio.service.impl;

import com.jairocodes.portfolio.resource.ContactoRequest;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    ReactiveMailer reactiveMailer;

    @InjectMocks
    ContactoService contactoService;

    @BeforeEach
    void configurarDestinatario() {
        contactoService.adminRecipient = "admin@portafolio.test";
    }

    @Test
    void enviaElMensajeAlAdministradorConReplyToAlVisitante() {
        when(reactiveMailer.send(any(Mail[].class))).thenReturn(Uni.createFrom().voidItem());

        contactoService.enviarMensaje(new ContactoRequest("Ana", "ana@correo.test", "Hola, me interesa tu proyecto"));

        ArgumentCaptor<Mail> correo = ArgumentCaptor.forClass(Mail.class);
        verify(reactiveMailer).send(correo.capture());
        Mail enviado = correo.getValue();
        assertEquals(List.of("admin@portafolio.test"), enviado.getTo());
        assertEquals("ana@correo.test", enviado.getReplyTo());
        assertEquals(ContactoService.ASUNTO, enviado.getSubject());
        assertTrue(enviado.getText().contains("Nombre: Ana"));
        assertTrue(enviado.getText().contains("Correo: ana@correo.test"));
        assertTrue(enviado.getText().contains("Hola, me interesa tu proyecto"));
    }

    @Test
    void siFallaElEnvioPropagaElError() {
        when(reactiveMailer.send(any(Mail[].class)))
                .thenReturn(Uni.createFrom().failure(new IllegalStateException("SMTP no disponible")));

        assertThrows(IllegalStateException.class,
                () -> contactoService.enviarMensaje(new ContactoRequest("Ana", "ana@correo.test", "Hola")));
    }
}
