package com.focus.mic.test.repository;

import com.focus.mic.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by caiwen on 2017/5/9.
 */
public interface UserRepository extends JpaRepository<User, Integer>{


}
