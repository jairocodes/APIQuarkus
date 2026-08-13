package test.Implementacion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import model.Comentario;

import java.util.List;

public interface IComentarioRepo extends PanacheRepositoryBase<Comentario, Integer> {
    public void saveComment(Comentario com);
    public Comentario getComentario(Integer id);
    List<Comentario> getComentarios();
}
