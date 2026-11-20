package com.jairocodes.portfolio.resource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Mensaje que un visitante envía al administrador del portafolio")
public record ContactoRequest(
        @Schema(description = "Nombre del visitante", example = "Ana López")
        @NotBlank @Size(max = 100) String nombre,

        @Schema(description = "Correo del visitante; el administrador responderá a esta dirección", example = "ana@correo.com")
        @NotBlank @Email @Size(max = 150) String email,

        @Schema(description = "Contenido del mensaje", example = "Hola, me interesa conversar sobre tu proyecto.")
        @NotBlank @Size(max = 2000) String mensaje) {
}
