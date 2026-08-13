package test.Implementacion;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import model.Admin;

public interface IAdminRepo extends PanacheRepositoryBase<Admin, Integer> {
    public void save(Admin admin);
}
