package test.Services.Implementacion;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.Comentario;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import test.Repository.ComentarioRepository;
import test.Resources.PaginatedResponse;
import test.Services.IComentarioService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ComentarioService implements IComentarioService {

        private static final Logger LOG = Logger.getLogger(ComentarioService.class);

        @Inject
        ComentarioRepository comentarioRepository;

        @ConfigProperty(name = "app.reports.output-dir")
        String reportsOutputDir;

        @Override
        public void save(Comentario com){
            comentarioRepository.saveComment(com);
        }


        public Comentario getComentario(Integer id){
            return comentarioRepository.getComentario(id);
        }

        public List<Comentario> getComentarios(){
            return comentarioRepository.getComentarios();
        }


        public PaginatedResponse<Comentario> paginated (int page){
            Page p = new Page(page -1, 10);
            var query = comentarioRepository.findAll().page(p);
            return new PaginatedResponse<>(query);
        }

        public void saveFile (Comentario comentario){
            Path path = Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv");
            File file = path.toFile();
            boolean fileExists = file.exists();

            try (FileWriter writer = new FileWriter(file, true)) {
                if (!fileExists) {
                    writer.write("Comentario,Fecha,nombre usuario,email usuario,Proyecto\n");
                }
                writer.write(comentario.getComentario() + "," + comentario.getFecha() + "," + comentario.getNombreUser() + "," + comentario.getEmailUser() + "," + comentario.getProject().getProyecto_id() + "\n");
            } catch (IOException e) {
                LOG.error("No se pudo escribir el reporte de comentarios en " + path, e);
                throw new UncheckedIOException(e);
            }
        }

}
