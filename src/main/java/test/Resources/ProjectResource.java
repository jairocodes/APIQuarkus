package test.Resources;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import model.Project;
import test.Services.Implementacion.ProjectService;

import java.util.List;

@Path("/Project")
public class ProjectResource {
    @Inject
    ProjectService project;

    @GET
    @Path("/{id}")
    public Project getProject(@PathParam("id") Integer id) {
        return project.getProject(id);
    }

    @GET
    public List<Project> getProjects(){
        return project.getProjects();
    }
}
