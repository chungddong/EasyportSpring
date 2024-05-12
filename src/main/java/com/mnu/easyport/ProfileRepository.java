package com.mnu.easyport;

import org.springframework.data.jpa.repository.JpaRepository;



public interface ProfileRepository extends JpaRepository<Profile, Long>{
    Profile findByUserid(String userid);
    Profile findByViewkey(String viewkey);
}
