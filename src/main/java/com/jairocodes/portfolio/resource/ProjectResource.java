package com.jairocodes.portfolio.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import com.jairocodes.portfolio.model.Project;
import com.jairocodes.portfolio.service.impl.ProjectService;

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
