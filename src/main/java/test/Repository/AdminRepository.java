package test.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import model.Admin;
import test.Implementacion.IAdminRepo;

@ApplicationScoped
public class AdminRepository implements IAdminRepo {

    @Override
    @Transactional
    public void save(Admin admin) {
        persist(admin);
    }
}
