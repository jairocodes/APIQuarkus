package com.jairocodes.portfolio.service.impl;

import com.jairocodes.portfolio.resource.ContactoRequest;
import com.jairocodes.portfolio.service.IContactoService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Reenvia al administrador los mensajes de contacto de los visitantes. La direccion del
 * administrador solo se usa como destinatario: nunca se devuelve en la respuesta. El correo
 * lleva Reply-To con la direccion del visitante para poder responderle directamente.
 */
@ApplicationScoped
public class ContactoService implements IContactoService {

    static final String ASUNTO = "Nuevo mensaje de contacto desde el portafolio";

    @Inject
    ReactiveMailer reactiveMailer;

    @ConfigProperty(name = "app.mail.admin-recipient")
    String adminRecipient;

    @Override
    public void enviarMensaje(ContactoRequest contacto) {
        String cuerpo = "Recibiste un mensaje desde el formulario de contacto del portafolio.\n\n"
                + "Nombre: " + contacto.nombre() + "\n"
                + "Correo: " + contacto.email() + "\n\n"
                + "Mensaje:\n" + contacto.mensaje() + "\n\n"
                + "Puedes responder directamente a este correo.";

        reactiveMailer.send(Mail.withText(adminRecipient, ASUNTO, cuerpo)
                        .setReplyTo(contacto.email()))
                .await().indefinitely();
    }
}
