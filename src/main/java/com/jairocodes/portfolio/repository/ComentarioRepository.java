package com.jairocodes.portfolio.repository;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import com.jairocodes.portfolio.model.Comentario;
import com.jairocodes.portfolio.repository.api.IComentarioRepo;

import java.util.List;

@ApplicationScoped
public class ComentarioRepository implements IComentarioRepo {

    @Inject
    EntityManager em;

    @Override
    @Transactional
    //hacer que se guarde en formato txt el comentario
    public void saveComment(Comentario com) {
        persist(com);
    }



    @Override
    public Comentario getComentario(Integer id){
        return findById(id);
    }

    @Override
    public List<Comentario> getComentarios() {
        return listAll();
    }


}
