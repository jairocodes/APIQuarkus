package com.jairocodes.portfolio.service.impl;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import com.jairocodes.portfolio.model.Comentario;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.jairocodes.portfolio.repository.ComentarioRepository;
import com.jairocodes.portfolio.resource.PaginatedResponse;
import com.jairocodes.portfolio.service.IComentarioService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
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
        private static final Object REPORT_LOCK = new Object();

        @Inject
        ComentarioRepository comentarioRepository;

        @ConfigProperty(name = "app.reports.output-dir")
        String reportsOutputDir;

        @Inject
        ReactiveMailer reactiveMailer;

        @Override
        public void save(Comentario com){
            comentarioRepository.saveComment(com);
        }

        /**
         * Registra un comentario de forma atomica: lo guarda, agrega la linea al reporte CSV del dia
         * y envia el correo de agradecimiento. Si alguno de los pasos falla, se revierte todo:
         * la transaccion hace rollback y la linea agregada al CSV se elimina.
         * <p>
         * El correo es el ultimo paso porque es el unico que no se puede deshacer. El flush previo
         * hace que los errores de base de datos aparezcan antes de tocar el CSV o enviar el correo.
         * Escribir el CSV y enviar el correo se hace bajo un mismo candado para que revertir una
         * linea nunca afecte la de otra solicitud concurrente.
         */
        @Override
        @Transactional
        public void registrarComentario(Comentario comentario) {
            comentarioRepository.saveComment(comentario);
            comentarioRepository.flush();

            synchronized (REPORT_LOCK) {
                Path path = reportPath();
                boolean fileExisted = Files.exists(path);
                long previousSize = fileExisted ? fileSize(path) : 0L;
                try {
                    saveFile(path, comentario);
                    sendThankYouEmail(comentario);
                } catch (RuntimeException e) {
                    revertReport(path, fileExisted, previousSize, e);
                    throw e;
                }
            }
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
            saveFile(reportPath(), comentario);
        }

        private Path reportPath() {
            return Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv");
        }

        private void sendThankYouEmail(Comentario comentario) {
            reactiveMailer.send(Mail.withText(
                    comentario.getEmailUser(),
                    "Agradecimiento",
                    "Hola, Gracias por realizar el comentario\n\ncomentario realizado \n\n" + comentario.getComentario()
            )).await().indefinitely();
        }

        private static long fileSize(Path path) {
            try {
                return Files.size(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /** Deja el reporte como estaba antes de agregar la linea: lo trunca o lo elimina si era nuevo. */
        private static void revertReport(Path path, boolean fileExisted, long previousSize, RuntimeException cause) {
            try {
                if (fileExisted) {
                    try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
                        channel.truncate(previousSize);
                    }
                } else {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e) {
                LOG.error("No se pudo revertir el reporte de comentarios en " + path, e);
                cause.addSuppressed(e);
            }
        }

        private static void saveFile(Path path, Comentario comentario) {
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
