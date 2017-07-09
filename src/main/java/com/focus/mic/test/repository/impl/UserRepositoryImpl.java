package com.focus.mic.test.repository.impl;

import com.focus.mic.test.entity.User;
import com.focus.mic.test.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by caiwen on 2017/6/11.
 */
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;


}
