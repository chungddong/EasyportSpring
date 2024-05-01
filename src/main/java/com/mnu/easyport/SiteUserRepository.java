package com.mnu.easyport;

import org.springframework.data.repository.CrudRepository;

public interface SiteUserRepository extends CrudRepository<SiteUser,String> {
    SiteUser findByEmail(String email);
}
