package com.jairocodes.portfolio.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import com.jairocodes.portfolio.model.Project;
import com.jairocodes.portfolio.repository.api.IProjectRepo;

import java.util.List;

@ApplicationScoped
public class ProjectRepository implements IProjectRepo {

     @Inject
     EntityManager em;

//     @Override
//     public void saveProject(Project pro) {
//          persist(pro);}

     @Override
     public Project getProject(Integer id){
          return findById(id);
     }

     @Override
     public List<Project> getProjects(){
          return listAll();
        }
}
