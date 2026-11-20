package com.jairocodes.portfolio.service.impl;

import jakarta.inject.Inject;
import com.jairocodes.portfolio.model.Project;
import jakarta.enterprise.context.ApplicationScoped;
import com.jairocodes.portfolio.repository.api.IProjectRepo;
import com.jairocodes.portfolio.service.IProjectService;

import java.util.List;

@ApplicationScoped
public class ProjectService implements IProjectService {

    @Inject
    IProjectRepo projectRepository;


    public Project getProject(Integer id){
        return projectRepository.getProject(id);
    }

    public List<Project> getProjects(){
        return projectRepository.getProjects();
    }

}
