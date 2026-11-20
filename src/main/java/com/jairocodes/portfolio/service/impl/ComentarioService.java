package com.jairocodes.portfolio.service.impl;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.jairocodes.portfolio.model.Comentario;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.jairocodes.portfolio.repository.ComentarioRepository;
import com.jairocodes.portfolio.resource.PaginatedResponse;
import com.jairocodes.portfolio.service.IComentarioService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ComentarioService implements IComentarioService {

        private static final Logger LOG = Logger.getLogger(ComentarioService.class);
        private static final String CSV_HEADER = "Comentario,Fecha,nombre usuario,email usuario,Proyecto\n";

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
            boolean fileExists = Files.exists(path);

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (!fileExists) {
                    writer.write(CSV_HEADER);
                }
                writer.write(toCsvLine(comentario));
            } catch (IOException e) {
                LOG.error("No se pudo escribir el reporte de comentarios en " + path, e);
                throw new UncheckedIOException(e);
            }
        }

        private static String toCsvLine(Comentario comentario) {
            Integer proyectoId = comentario.getProject() != null ? comentario.getProject().getProyecto_id() : null;
            return String.join(",",
                    csvField(comentario.getComentario()),
                    csvField(comentario.getFecha()),
                    csvField(comentario.getNombreUser()),
                    csvField(comentario.getEmailUser()),
                    csvField(proyectoId)) + "\n";
        }

        /**
         * Convierte un valor en un campo CSV segun RFC 4180: si contiene comas, comillas o
         * saltos de linea se encierra entre comillas y las comillas internas se duplican.
         * Los valores que empiezan con =, +, -, @, tabulador o retorno de carro se prefijan
         * con ' para que una hoja de calculo no los interprete como formulas (CSV injection).
         */
        static String csvField(Object value) {
            if (value == null) {
                return "";
            }
            String text = value.toString();
            if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
                text = "'" + text;
            }
            if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
                text = "\"" + text.replace("\"", "\"\"") + "\"";
            }
            return text;
        }

}
