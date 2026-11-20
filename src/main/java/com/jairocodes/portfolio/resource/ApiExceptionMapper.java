package com.jairocodes.portfolio.resource;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.UncheckedIOException;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Error no manejado al procesar la solicitud", exception);

        if (exception instanceof PersistenceException) {
            return error(Response.Status.BAD_REQUEST, "No se pudo guardar la informacion, verifica los datos enviados");
        }
        if (exception instanceof UncheckedIOException) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "No se pudo generar el reporte");
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado");
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(message))
                .build();
    }

    public record ErrorResponse(String message) {}
}
