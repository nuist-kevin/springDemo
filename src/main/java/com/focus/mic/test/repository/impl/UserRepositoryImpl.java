package com.focus.mic.test.repository.impl;

import com.focus.mic.test.entity.User;
import com.focus.mic.test.repository.UserRepository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> findAllUsersByUsername(String username) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(root);

        criteriaQuery.where(criteriaBuilder.
                like(criteriaBuilder.lower(root.get("username")),
                        "%" + StringUtils.trimWhitespace(username) + "%"));

        List<User> userList = entityManager.createQuery(criteriaQuery).getResultList();

        return userList;
    }



}
