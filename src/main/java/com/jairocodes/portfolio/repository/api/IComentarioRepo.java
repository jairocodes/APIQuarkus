package com.jairocodes.portfolio.repository.api;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import com.jairocodes.portfolio.model.Comentario;

import java.util.List;

public interface IComentarioRepo extends PanacheRepositoryBase<Comentario, Integer> {
    public void saveComment(Comentario com);
    public Comentario getComentario(Integer id);
    List<Comentario> getComentarios();
}
