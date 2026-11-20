package com.jairocodes.portfolio.service.impl;

import com.jairocodes.portfolio.model.Project;
import com.jairocodes.portfolio.repository.api.IProjectRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    IProjectRepo projectRepository;

    @InjectMocks
    ProjectService projectService;

    @Test
    void getProjectDevuelveElProyectoDelRepositorio() {
        Project proyecto = proyecto(1, "APIQuarkus");
        when(projectRepository.getProject(1)).thenReturn(proyecto);

        assertSame(proyecto, projectService.getProject(1));
        verify(projectRepository).getProject(1);
    }

    @Test
    void getProjectDevuelveNullSiNoExiste() {
        when(projectRepository.getProject(99)).thenReturn(null);

        assertNull(projectService.getProject(99));
    }

    @Test
    void getProjectsDevuelveTodosLosProyectos() {
        List<Project> proyectos = List.of(proyecto(1, "APIQuarkus"), proyecto(2, "InterfazGraficaPrimefaces"));
        when(projectRepository.getProjects()).thenReturn(proyectos);

        assertEquals(proyectos, projectService.getProjects());
    }

    @Test
    void getProjectsDevuelveListaVaciaSiNoHayProyectos() {
        when(projectRepository.getProjects()).thenReturn(List.of());

        assertTrue(projectService.getProjects().isEmpty());
    }

    private static Project proyecto(int id, String nombre) {
        Project proyecto = new Project();
        proyecto.setProyecto_id(id);
        proyecto.setNameProject(nombre);
        return proyecto;
    }
}
