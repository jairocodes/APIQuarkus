package com.jairocodes.portfolio.repository.api;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import com.jairocodes.portfolio.model.Admin;

public interface IAdminRepo extends PanacheRepositoryBase<Admin, Integer> {
    public void save(Admin admin);
}
