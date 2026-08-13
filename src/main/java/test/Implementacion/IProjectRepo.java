package test.Implementacion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import model.Project;

import java.util.List;

public interface IProjectRepo extends PanacheRepositoryBase<Project, Integer> {
    public Project getProject(Integer id);
    List<Project> getProjects();
}
