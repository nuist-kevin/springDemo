package com.focus.mic.test.repository;

import com.focus.mic.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor, UserRepository{

    User findByUsername(String username);


}
