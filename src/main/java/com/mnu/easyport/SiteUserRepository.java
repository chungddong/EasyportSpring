package com.mnu.easyport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteUserRepository extends JpaRepository<SiteUser, Long> {
    SiteUser findByUserid(String userid);
}
