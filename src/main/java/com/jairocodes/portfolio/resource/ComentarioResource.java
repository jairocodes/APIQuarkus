package com.jairocodes.portfolio.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.jairocodes.portfolio.model.Comentario;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import com.jairocodes.portfolio.service.impl.ComentarioService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

@Path("/Comentario")
@Tag(name = "Comentarios", description = "Comentarios que dejan los visitantes sobre los proyectos del portafolio")
public class ComentarioResource {

    @Inject
    ComentarioService comentario;

    @Inject
    ReactiveMailer reactiveMailer;

    @ConfigProperty(name = "app.mail.admin-recipient")
    String adminRecipient;

    @ConfigProperty(name = "app.reports.output-dir")
    String reportsOutputDir;

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener un comentario por id",
            description = "Devuelve el comentario con el identificador indicado, junto con el proyecto al que pertenece.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Comentario encontrado"),
            @APIResponse(responseCode = "204", description = "No existe un comentario con ese id (respuesta sin cuerpo)"),
            @APIResponse(responseCode = "404", description = "El id no es un número entero válido")
    })
    public Comentario getComentario(
            @Parameter(description = "Identificador del comentario", example = "1")
            @PathParam("id") Integer id) {
        return comentario.getComentario(id);
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Registrar un comentario",
            description = "Valida y guarda el comentario de un visitante, agrega una línea al reporte CSV del día "
                    + "y envía un correo de agradecimiento al autor. La operación es atómica: si algún paso falla, "
                    + "no se guarda el comentario ni se modifica el reporte.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Comentario registrado. El cuerpo es el texto \"Comentario guardado\""),
            @APIResponse(responseCode = "400", description = "Datos inválidos: campos vacíos, email mal formado, proyecto "
                    + "faltante o inexistente"),
            @APIResponse(responseCode = "500", description = "No se pudo generar el reporte o enviar el correo; no se guardó nada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ApiExceptionMapper.ErrorResponse.class)))
    })
    public Response createComentario(
            @RequestBody(description = "Comentario a registrar. El proyecto se indica solo con su proyecto_id.", required = true)
            @Valid Comentario Rcomentario) {
        comentario.registrarComentario(Rcomentario);
        return Response.ok("Comentario guardado").build();
    }

    @GET
    @Operation(summary = "Listar comentarios paginados",
            description = "Devuelve los comentarios en páginas de 10 elementos. Una página posterior a la última "
                    + "devuelve la lista vacía.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Página de comentarios con el número de página y el total de páginas"),
            @APIResponse(responseCode = "400", description = "El número de página es menor que 1")
    })
    public PaginatedResponse<Comentario> list(
            @Parameter(description = "Número de página, empezando en 1", example = "1")
            @QueryParam("page") @DefaultValue("1") @Min(1) int page) {
        return comentario.paginated(page);
    }

    private void sendAdminReport(File csvFile) throws IOException {
        reactiveMailer.send(Mail.withText(
                adminRecipient,
                "Informe",
                "Este correo informa cuales son los comentarios realizados en el dia de hoy"
        ).addAttachment(csvFile.getName(), Files.readAllBytes(csvFile.toPath()), "text/csv"))
                .await().indefinitely();
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void executeDailyTask() throws IOException {
        File csvFile = java.nio.file.Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv").toFile();
        if (!csvFile.exists()) {
            return;
        }
        sendAdminReport(csvFile);
    }

}
