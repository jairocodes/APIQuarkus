package com.jairocodes.portfolio.service.impl;

import com.jairocodes.portfolio.model.Comentario;
import com.jairocodes.portfolio.model.Project;
import com.jairocodes.portfolio.repository.ComentarioRepository;
import com.jairocodes.portfolio.resource.PaginatedResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    private static final String ENCABEZADO = "Comentario,Fecha,nombre usuario,email usuario,Proyecto";

    @Mock
    ComentarioRepository comentarioRepository;

    @Mock
    ReactiveMailer reactiveMailer;

    @InjectMocks
    ComentarioService comentarioService;

    @TempDir
    Path directorioReportes;

    @BeforeEach
    void configurarDirectorioDeReportes() {
        comentarioService.reportsOutputDir = directorioReportes.toString();
    }

    @Nested
    class ObtenerPorId {

        @Test
        void devuelveElComentarioDelRepositorio() {
            Comentario comentario = comentario("Excelente", "ana@correo.test");
            when(comentarioRepository.getComentario(1)).thenReturn(comentario);

            assertSame(comentario, comentarioService.getComentario(1));
        }

        @Test
        void devuelveNullSiNoExiste() {
            when(comentarioRepository.getComentario(99)).thenReturn(null);

            assertNull(comentarioService.getComentario(99));
        }
    }

    @Nested
    class Paginacion {

        @Test
        void paginaUnoConsultaElIndiceCeroConTamanoDiez() {
            List<Comentario> datos = List.of(comentario("Uno", "uno@correo.test"), comentario("Dos", "dos@correo.test"));
            PanacheQuery<Comentario> query = queryPaginada(0, datos, 3);

            PaginatedResponse<Comentario> respuesta = comentarioService.paginated(1);

            ArgumentCaptor<Page> pagina = ArgumentCaptor.forClass(Page.class);
            verify(query).page(pagina.capture());
            assertEquals(0, pagina.getValue().index);
            assertEquals(10, pagina.getValue().size);

            assertEquals(datos, respuesta.data());
            assertEquals(1, respuesta.page());
            assertEquals(3, respuesta.totalPages());
        }

        @Test
        void paginaSinResultadosDevuelveListaVaciaYConservaElNumeroDePagina() {
            queryPaginada(4, List.of(), 2);

            PaginatedResponse<Comentario> respuesta = comentarioService.paginated(5);

            assertTrue(respuesta.data().isEmpty());
            assertEquals(5, respuesta.page());
            assertEquals(2, respuesta.totalPages());
        }

        @SuppressWarnings("unchecked")
        private PanacheQuery<Comentario> queryPaginada(int indice, List<Comentario> datos, int totalPaginas) {
            PanacheQuery<Comentario> query = mock(PanacheQuery.class);
            doReturn(query).when(comentarioRepository).findAll();
            doReturn(query).when(query).page(any(Page.class));
            when(query.list()).thenReturn(datos);
            when(query.page()).thenReturn(new Page(indice, 10));
            when(query.pageCount()).thenReturn(totalPaginas);
            return query;
        }
    }

    @Nested
    class EscrituraDelCsv {

        @Test
        void archivoNuevoIncluyeEncabezadoUnaSolaVez() throws IOException {
            comentarioService.saveFile(comentario("Primero", "uno@correo.test"));
            comentarioService.saveFile(comentario("Segundo", "dos@correo.test"));

            List<String> lineas = lineasDelReporte();
            assertEquals(3, lineas.size());
            assertEquals(ENCABEZADO, lineas.get(0));
            assertEquals(1, lineas.stream().filter(ENCABEZADO::equals).count());
            assertEquals("Primero,2025-03-01T10:00,Visitante,uno@correo.test,1", lineas.get(1));
            assertEquals("Segundo,2025-03-01T10:00,Visitante,dos@correo.test,1", lineas.get(2));
        }

        @Test
        void archivoExistenteNoRepiteElEncabezado() throws IOException {
            Files.writeString(reporte(), "contenido previo\n", StandardCharsets.UTF_8);

            comentarioService.saveFile(comentario("Nuevo", "nuevo@correo.test"));

            List<String> lineas = lineasDelReporte();
            assertEquals(List.of("contenido previo", "Nuevo,2025-03-01T10:00,Visitante,nuevo@correo.test,1"), lineas);
        }

        @Test
        void camposConComasComillasYSaltosDeLineaSeEscapan() throws IOException {
            comentarioService.saveFile(comentario("Hola, \"gran\" trabajo\nsaludos", "ana@correo.test"));

            String contenido = Files.readString(reporte(), StandardCharsets.UTF_8);
            assertEquals(ENCABEZADO + "\n"
                    + "\"Hola, \"\"gran\"\" trabajo\nsaludos\",2025-03-01T10:00,Visitante,ana@correo.test,1\n", contenido);
        }

        @Test
        void escribeEnUtf8() throws IOException {
            comentarioService.saveFile(comentario("Diseño excelente, ¡felicidades!", "ana@correo.test"));

            assertTrue(Files.readString(reporte(), StandardCharsets.UTF_8).contains("\"Diseño excelente, ¡felicidades!\""));
        }

        @Test
        void csvFieldNeutralizaFormulasYManejaNulos() {
            assertEquals("'=1+1", ComentarioService.csvField("=1+1"));
            assertEquals("'+52", ComentarioService.csvField("+52"));
            assertEquals("'-2", ComentarioService.csvField("-2"));
            assertEquals("'@SUMA(A1)", ComentarioService.csvField("@SUMA(A1)"));
            assertEquals("\"'=HYPERLINK(\"\"x\"\",\"\"y\"\")\"", ComentarioService.csvField("=HYPERLINK(\"x\",\"y\")"));
            assertEquals("", ComentarioService.csvField(null));
            assertEquals("texto normal", ComentarioService.csvField("texto normal"));
        }
    }

    @Nested
    class RegistroAtomico {

        @Test
        void guardaEscribeElCsvYEnviaElCorreoEnEseOrden() throws IOException {
            when(reactiveMailer.send(any(Mail[].class))).thenReturn(Uni.createFrom().voidItem());
            Comentario comentario = comentario("Gran proyecto", "ana@correo.test");

            comentarioService.registrarComentario(comentario);

            InOrder orden = inOrder(comentarioRepository, reactiveMailer);
            orden.verify(comentarioRepository).saveComment(comentario);
            orden.verify(comentarioRepository).flush();
            orden.verify(reactiveMailer).send(any(Mail[].class));
            assertEquals(2, lineasDelReporte().size());

            ArgumentCaptor<Mail> correo = ArgumentCaptor.forClass(Mail.class);
            verify(reactiveMailer).send(correo.capture());
            assertEquals(List.of("ana@correo.test"), correo.getValue().getTo());
            assertEquals("Agradecimiento", correo.getValue().getSubject());
            assertTrue(correo.getValue().getText().contains("Gran proyecto"));
        }

        @Test
        void siFallaLaBaseDeDatosNoEscribeElCsvNiEnviaCorreo() {
            doThrow(new PersistenceException("violacion de llave foranea")).when(comentarioRepository).flush();

            assertThrows(PersistenceException.class,
                    () -> comentarioService.registrarComentario(comentario("Hola", "ana@correo.test")));

            assertFalse(Files.exists(reporte()));
            verifyNoInteractions(reactiveMailer);
        }

        @Test
        void siFallaElCorreoConArchivoNuevoSeEliminaElArchivo() {
            when(reactiveMailer.send(any(Mail[].class)))
                    .thenReturn(Uni.createFrom().failure(new IllegalStateException("SMTP no disponible")));

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> comentarioService.registrarComentario(comentario("Hola", "ana@correo.test")));

            assertEquals("SMTP no disponible", error.getMessage());
            assertFalse(Files.exists(reporte()), "El archivo creado para este comentario debe eliminarse");
        }

        @Test
        void siFallaElCorreoConArchivoExistenteSeRestauraSuContenido() throws IOException {
            String contenidoPrevio = ENCABEZADO + "\nPrevio,2025-01-01T10:00,Luis,luis@correo.test,1\n";
            Files.writeString(reporte(), contenidoPrevio, StandardCharsets.UTF_8);
            when(reactiveMailer.send(any(Mail[].class)))
                    .thenReturn(Uni.createFrom().failure(new IllegalStateException("SMTP no disponible")));

            assertThrows(IllegalStateException.class,
                    () -> comentarioService.registrarComentario(comentario("Hola", "ana@correo.test")));

            assertEquals(contenidoPrevio, Files.readString(reporte(), StandardCharsets.UTF_8));
        }
    }

    private Path reporte() {
        return directorioReportes.resolve(LocalDate.now() + "_comentarios.csv");
    }

    private List<String> lineasDelReporte() throws IOException {
        return Files.readAllLines(reporte(), StandardCharsets.UTF_8);
    }

    private static Comentario comentario(String texto, String email) {
        Project proyecto = new Project();
        proyecto.setProyecto_id(1);
        Comentario comentario = new Comentario();
        comentario.setComentario(texto);
        comentario.setFecha(LocalDateTime.of(2025, 3, 1, 10, 0));
        comentario.setNombreUser("Visitante");
        comentario.setEmailUser(email);
        comentario.setProject(proyecto);
        return comentario;
    }
}
