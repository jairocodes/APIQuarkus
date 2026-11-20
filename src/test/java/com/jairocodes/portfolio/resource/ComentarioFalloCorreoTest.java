package com.jairocodes.portfolio.resource;

import com.jairocodes.portfolio.repository.ComentarioRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifica que POST /Comentario/save sea atomico cuando falla el envio del correo:
 * el servidor SMTP apunta a un puerto cerrado, por lo que el envio falla de verdad.
 */
@QuarkusTest
@TestProfile(ComentarioFalloCorreoTest.SmtpNoDisponible.class)
class ComentarioFalloCorreoTest {

    @Inject
    ComentarioRepository comentarioRepository;

    @ConfigProperty(name = "app.reports.output-dir")
    String reportsOutputDir;

    @Test
    void siFallaElCorreoNoSeGuardaElComentarioNiSeModificaElCsv() throws IOException {
        Path reporte = Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv");
        long totalAntes = contarComentarios();
        String email = "visitante-" + UUID.randomUUID() + "@correo.test";

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"comentario": "Comentario", "nombreUser": "Visitante", "emailUser": "%s", "project": {"proyecto_id": 1}}
                        """.formatted(email))
                .when().post("/Comentario/save")
                .then()
                .statusCode(500)
                .body("message", equalTo("Ocurrio un error inesperado"));

        assertEquals(totalAntes, contarComentarios(), "La transaccion debe hacer rollback");
        assertFalse(Files.exists(reporte), "El CSV creado para este comentario debe eliminarse");
    }

    @Test
    void siFallaElCorreoElCsvExistenteQuedaComoEstaba() throws IOException {
        Path reporte = Path.of(reportsOutputDir, LocalDate.now() + "_comentarios.csv");
        String contenidoPrevio = "Comentario,Fecha,nombre usuario,email usuario,Proyecto\nPrevio,2025-01-01T10:00,Ana,ana@correo.test,1\n";
        Files.writeString(reporte, contenidoPrevio, StandardCharsets.UTF_8);
        long totalAntes = contarComentarios();

        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("""
                            {"comentario": "Otro comentario", "nombreUser": "Visitante", "emailUser": "otro@correo.test", "project": {"proyecto_id": 1}}
                            """)
                    .when().post("/Comentario/save")
                    .then()
                    .statusCode(500);

            assertEquals(totalAntes, contarComentarios(), "La transaccion debe hacer rollback");
            assertEquals(contenidoPrevio, Files.readString(reporte, StandardCharsets.UTF_8),
                    "La linea agregada al CSV debe revertirse");
        } finally {
            Files.deleteIfExists(reporte);
        }
    }

    private long contarComentarios() {
        return QuarkusTransaction.requiringNew().call(comentarioRepository::count);
    }

    public static class SmtpNoDisponible implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            try {
                Path directorio = Files.createTempDirectory("reportes-fallo-correo");
                directorio.toFile().deleteOnExit();
                return Map.of(
                        "quarkus.mailer.mock", "false",
                        "quarkus.mailer.host", "127.0.0.1",
                        "quarkus.mailer.port", "1",
                        "quarkus.mailer.ssl", "false",
                        "quarkus.mailer.start-tls", "DISABLED",
                        "quarkus.mailer.login", "DISABLED",
                        "app.reports.output-dir", directorio.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
