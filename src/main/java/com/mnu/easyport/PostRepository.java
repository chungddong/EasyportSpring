package com.mnu.easyport;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserid(String userid);
    List<Post> findAll();
}
