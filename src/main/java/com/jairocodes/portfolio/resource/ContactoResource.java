package com.jairocodes.portfolio.resource;

import com.jairocodes.portfolio.service.impl.ContactoService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/Contacto")
@Tag(name = "Contacto", description = "Mensajes de los visitantes al administrador del portafolio")
public class ContactoResource {

    @Inject
    ContactoService contactoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Contactar al administrador",
            description = "Envía el mensaje del visitante al correo del administrador, con Reply-To a la dirección "
                    + "del visitante para que pueda responderle directamente. La dirección del administrador "
                    + "nunca se expone en la API.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Mensaje enviado. El cuerpo es el texto \"Mensaje enviado\""),
            @APIResponse(responseCode = "400", description = "Datos inválidos: campos vacíos, email mal formado o textos demasiado largos"),
            @APIResponse(responseCode = "500", description = "No se pudo enviar el correo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiExceptionMapper.ErrorResponse.class)))
    })
    public Response contactar(
            @RequestBody(description = "Datos del visitante y su mensaje", required = true)
            @Valid @NotNull ContactoRequest contacto) {
        contactoService.enviarMensaje(contacto);
        return Response.ok("Mensaje enviado").build();
    }
}
