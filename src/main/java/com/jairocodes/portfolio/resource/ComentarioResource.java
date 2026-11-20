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
import com.jairocodes.portfolio.service.impl.ComentarioService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

@Path("/Comentario")
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
    public Comentario getComentario(@PathParam("id") Integer id) {
        return comentario.getComentario(id);
    }

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createComentario(@Valid Comentario Rcomentario) {
        comentario.registrarComentario(Rcomentario);
        return Response.ok("Comentario guardado").build();
    }

    @GET
    //http://localhost:8080/Comentario?page=2
    public PaginatedResponse<Comentario> list(@QueryParam("page") @DefaultValue("1") @Min(1) int page) {
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
