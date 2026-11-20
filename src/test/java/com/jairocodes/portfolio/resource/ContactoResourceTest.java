package com.jairocodes.portfolio.resource;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ContactoResourceTest {

    private static final String ADMIN = "admin@portafolio.test";

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void limpiarBuzon() {
        mailbox.clear();
    }

    @Test
    void mensajeValidoSeEnviaAlAdministradorSinExponerSuCorreo() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nombre": "Ana", "email": "ana@correo.test", "mensaje": "Hola, me interesa tu proyecto"}
                        """)
                .when().post("/Contacto")
                .then()
                .statusCode(200)
                .body(equalTo("Mensaje enviado"))
                .body(not(containsString(ADMIN)));

        List<Mail> recibidos = mailbox.getMailsSentTo(ADMIN);
        assertEquals(1, recibidos.size());
        assertEquals("ana@correo.test", recibidos.get(0).getReplyTo());
        assertTrue(recibidos.get(0).getText().contains("Hola, me interesa tu proyecto"));
    }

    @Test
    void emailMalFormadoDevuelve400YNoEnviaCorreo() {
        assertRechazado("""
                {"nombre": "Ana", "email": "no-es-un-correo", "mensaje": "Hola"}
                """);
    }

    @Test
    void camposVaciosDevuelven400YNoEnviaCorreo() {
        assertRechazado("""
                {"nombre": " ", "email": "ana@correo.test", "mensaje": ""}
                """);
    }

    @Test
    void mensajeDemasiadoLargoDevuelve400YNoEnviaCorreo() {
        assertRechazado("""
                {"nombre": "Ana", "email": "ana@correo.test", "mensaje": "%s"}
                """.formatted("a".repeat(2001)));
    }

    @Test
    void cuerpoVacioDevuelve400YNoEnviaCorreo() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/Contacto")
                .then()
                .statusCode(400);

        assertTrue(mailbox.getMailsSentTo(ADMIN).isEmpty());
    }

    private void assertRechazado(String json) {
        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when().post("/Contacto")
                .then()
                .statusCode(400);

        assertTrue(mailbox.getMailsSentTo(ADMIN).isEmpty(), "No debe enviarse correo");
    }
}
