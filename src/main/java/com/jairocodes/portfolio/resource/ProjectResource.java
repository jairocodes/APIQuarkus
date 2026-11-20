package com.jairocodes.portfolio.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import com.jairocodes.portfolio.model.Project;
import com.jairocodes.portfolio.service.impl.ProjectService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/Project")
@Tag(name = "Proyectos", description = "Proyectos publicados en el portafolio")
public class ProjectResource {
    @Inject
    ProjectService project;

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener un proyecto por id",
            description = "Devuelve el proyecto con el identificador indicado, incluido el administrador que lo publicó.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Proyecto encontrado"),
            @APIResponse(responseCode = "204", description = "No existe un proyecto con ese id (respuesta sin cuerpo)"),
            @APIResponse(responseCode = "404", description = "El id no es un número entero válido")
    })
    public Project getProject(
            @Parameter(description = "Identificador del proyecto", example = "1")
            @PathParam("id") Integer id) {
        return project.getProject(id);
    }

    @GET
    @Operation(summary = "Listar proyectos", description = "Devuelve todos los proyectos del portafolio.")
    @APIResponse(responseCode = "200", description = "Lista de proyectos (vacía si no hay ninguno)")
    public List<Project> getProjects(){
        return project.getProjects();
    }
}
