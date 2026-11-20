package com.jairocodes.portfolio.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.jairocodes.portfolio.model.Admin;
import com.jairocodes.portfolio.repository.api.IAdminRepo;

@ApplicationScoped
public class AdminRepository implements IAdminRepo {

    @Override
    @Transactional
    public void save(Admin admin) {
        persist(admin);
    }
}
