package test.Services;

import model.Project;

import java.util.List;

public interface IProjectService {
    public Project getProject(Integer id);
    public List<Project> getProjects();
}
