package com.jairocodes.portfolio.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@QuarkusTest
class ProjectResourceTest {

    @Test
    void listarProyectosDevuelveTodosLosProyectos() {
        given()
                .when().get("/Project")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .body("$", hasSize(2))
                .body("nameProject", containsInAnyOrder("APIQuarkus", "InterfazGraficaPrimefaces"));
    }

    @Test
    void obtenerProyectoPorIdDevuelveElProyectoConSuAdmin() {
        given()
                .when().get("/Project/1")
                .then()
                .statusCode(200)
                .body("proyecto_id", equalTo(1))
                .body("nameProject", equalTo("APIQuarkus"))
                .body("urlRepo", equalTo("https://github.com/jairocodes/APIQuarkus"))
                .body("admin.admin_id", equalTo(1));
    }

    @Test
    void elCorreoDelAdministradorNoSeExpone() {
        given()
                .when().get("/Project/1")
                .then()
                .statusCode(200)
                .body("admin.nombre", equalTo("Admin Pruebas"))
                .body("admin", not(hasKey("email")))
                .body(not(containsString("admin@portafolio.test")));

        given()
                .when().get("/Project")
                .then()
                .statusCode(200)
                .body(not(containsString("admin@portafolio.test")));
    }

    @Test
    void obtenerProyectoInexistenteDevuelve204SinCuerpo() {
        given()
                .when().get("/Project/999")
                .then()
                .statusCode(204);
    }

    @Test
    void idNoNumericoDevuelve404() {
        given()
                .when().get("/Project/abc")
                .then()
                .statusCode(404);
    }

    @Test
    void metodoNoPermitidoDevuelve405() {
        given()
                .when().delete("/Project")
                .then()
                .statusCode(405);
    }
}
