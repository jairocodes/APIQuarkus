package test.Resources;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.Comentario;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import test.Services.Implementacion.ComentarioService;

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

    @GET
    @Path("/comentarioId")
    //se utiliza para obtener un comentario
    public Comentario getComentario() {
        return comentario.getComentario();
    }


    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    //se utiliza para guardar un comentario
    public Response createComentario(Comentario Rcomentario) {
        try{
            comentario.save(Rcomentario);
            sendThankYouEmail(Rcomentario);
            comentario.saveFile(Rcomentario);
            return Response.ok("Comentario guardado").build();
        }catch (PersistenceException e){
            throw e;
        }
    }

    @GET
    //http://localhost:8080/Comentario/paginated?page=2
    public PaginatedReponse<Comentario> list(@QueryParam("page")@DefaultValue("1") int page) {
        return comentario.paginated(page);
    }

    private void sendThankYouEmail(Comentario comentario) {
        reactiveMailer.send(Mail.withText(
                comentario.getEmailUser(),
                "Agradecimiento",
                "Hola, Gracias por realizar el comentario\n\ncomentario realizado \n\n" + comentario.getComentario()
        )).await().indefinitely();
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
        String fileName = "C:\\Users\\admin\\Desktop\\Project_quarkus\\api-quarkus\\" + LocalDate.now() + "_comentarios.csv";
        File csvFile = new File(fileName);
        sendAdminReport(csvFile);
    }

}
