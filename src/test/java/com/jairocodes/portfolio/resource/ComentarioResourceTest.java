package com.jairocodes.portfolio.resource;

import com.jairocodes.portfolio.repository.ComentarioRepository;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ComentarioResourceTest {

    @Inject
    MockMailbox mailbox;

    @Inject
    ComentarioRepository comentarioRepository;

    @ConfigProperty(name = "app.reports.output-dir")
    String reportsOutputDir;

    @BeforeEach
    void limpiarBuzon() {
        mailbox.clear();
    }

    // ---------- GET /Comentario/{id} ----------

    @Test
    void obtenerComentarioPorIdDevuelveElComentarioConSuProyecto() {
        given()
                .when().get("/Comentario/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("comentario_id", equalTo(1))
                .body("comentario", equalTo("Excelente proyecto"))
                .body("nombreUser", equalTo("Ana"))
                .body("emailUser", equalTo("ana@correo.test"))
                .body("project.proyecto_id", equalTo(1));
    }

    @Test
    void obtenerComentarioInexistenteDevuelve204SinCuerpo() {
        given()
                .when().get("/Comentario/999999")
                .then()
                .statusCode(204);
    }

    // ---------- GET /Comentario?page=N ----------

    @Test
    void primeraPaginaDevuelveDiezComentarios() {
        given()
                .queryParam("page", 1)
                .when().get("/Comentario")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("data", hasSize(10))
                .body("page", equalTo(1))
                .body("totalPages", greaterThanOrEqualTo(2));
    }

    @Test
    void sinParametroPageSeDevuelveLaPrimeraPagina() {
        given()
                .when().get("/Comentario")
                .then()
                .statusCode(200)
                .body("page", equalTo(1))
                .body("data", hasSize(10));
    }

    @Test
    void paginaPosteriorALaUltimaDevuelveListaVacia() {
        given()
                .queryParam("page", 999)
                .when().get("/Comentario")
                .then()
                .statusCode(200)
                .body("data", empty())
                .body("page", equalTo(999))
                .body("totalPages", greaterThanOrEqualTo(2));
    }

    @Test
    void paginaMenorQueUnoDevuelve400() {
        given()
                .queryParam("page", 0)
                .when().get("/Comentario")
                .then()
                .statusCode(400);
    }

    // ---------- POST /Comentario/save ----------

    @Test
    void guardarComentarioValidoGuardaEnviaCorreoYAgregaLineaAlCsv() throws IOException {
        String email = correoUnico();
        long totalAntes = contarComentarios();

        given()
                .contentType(ContentType.JSON)
                .body(comentarioJson("Gran proyecto, felicidades", "Visitante", email, 1))
                .when().post("/Comentario/save")
                .then()
                .statusCode(200)
                .body(equalTo("Comentario guardado"));

        assertEquals(totalAntes + 1, contarComentarios(), "El comentario debe quedar guardado");

        List<Mail> enviados = mailbox.getMailsSentTo(email);
        assertEquals(1, enviados.size(), "Debe enviarse un correo de agradecimiento");
        Mail correo = enviados.get(0);
        assertEquals("Agradecimiento", correo.getSubject());
        assertTrue(correo.getText().contains("Gran proyecto, felicidades"));

        assertTrue(lineasDelReporte().stream().anyMatch(linea -> linea.contains(email)),
                "El comentario debe agregarse al CSV del dia");
    }

    @Test
    void emailMalFormadoDevuelve400YNoGuardaNada() throws IOException {
        String email = "correo-sin-arroba-" + UUID.randomUUID();
        assertRechazado(comentarioJson("Comentario", "Visitante", email, 1), email);
    }

    @Test
    void camposVaciosDevuelven400YNoGuardaNada() throws IOException {
        String email = correoUnico();
        assertRechazado(comentarioJson("", " ", email, 1), email);
    }

    @Test
    void comentarioSinProyectoDevuelve400YNoGuardaNada() throws IOException {
        String email = correoUnico();
        String json = """
                {"comentario": "Sin proyecto", "nombreUser": "Visitante", "emailUser": "%s"}
                """.formatted(email);
        assertRechazado(json, email);
    }

    @Test
    void proyectoInexistenteDevuelve400YNoGuardaNada() throws IOException {
        // El error de llave foranea ocurre al hacer flush, antes de escribir el CSV y de enviar el correo.
        String email = correoUnico();
        assertRechazado(comentarioJson("Proyecto que no existe", "Visitante", email, 999), email);
    }

    // ---------- utilidades ----------

    private void assertRechazado(String json, String email) throws IOException {
        long totalAntes = contarComentarios();

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/Comentario/save")
                .then()
                .statusCode(400);

        assertEquals(totalAntes, contarComentarios(), "No debe guardarse el comentario");
        assertTrue(mailbox.getMailsSentTo(email).isEmpty(), "No debe enviarse correo");
        assertTrue(lineasDelReporte().stream().noneMatch(linea -> linea.contains(email)),
                "No debe agregarse la linea al CSV");
    }

    private long contarComentarios() {
        return QuarkusTransaction.requiringNew().call(comentarioRepository::count);
    }

    private List<String> lineasDelReporte() throws IOException {
        Path reporte = Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv");
        return Files.exists(reporte) ? Files.readAllLines(reporte, StandardCharsets.UTF_8) : List.of();
    }

    private static String correoUnico() {
        return "visitante-" + UUID.randomUUID() + "@correo.test";
    }

    private static String comentarioJson(String comentario, String nombre, String email, int proyectoId) {
        return """
                {"comentario": "%s", "nombreUser": "%s", "emailUser": "%s", "project": {"proyecto_id": %d}}
                """.formatted(comentario, nombre, email, proyectoId);
    }
}
