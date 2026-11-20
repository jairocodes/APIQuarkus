package com.jairocodes.portfolio.service;

import com.jairocodes.portfolio.model.Project;

import java.util.List;

public interface IProjectService {
    public Project getProject(Integer id);
    public List<Project> getProjects();
}
