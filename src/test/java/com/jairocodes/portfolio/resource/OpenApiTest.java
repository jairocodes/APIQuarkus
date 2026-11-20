package com.jairocodes.portfolio.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class OpenApiTest {

    @Test
    void especificacionOpenApiDocumentaTodosLosEndpoints() {
        given()
                .queryParam("format", "json")
                .when().get("/q/openapi")
                .then()
                .statusCode(200)
                .body("info.title", equalTo("API Portafolio de Proyectos"))
                .body("info.version", equalTo("1.1.0"))
                .body("info.description", notNullValue())
                .body("paths", hasKey("/Project"))
                .body("paths", hasKey("/Project/{id}"))
                .body("paths", hasKey("/Comentario"))
                .body("paths", hasKey("/Comentario/{id}"))
                .body("paths", hasKey("/Comentario/save"));
    }

    @Test
    void swaggerUiEstaDisponible() {
        given()
                .when().get("/q/swagger-ui/")
                .then()
                .statusCode(200);
    }
}
