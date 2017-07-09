package com.focus.mic.test.repository;

import com.focus.mic.test.entity.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

/**
 * Created by caiwen on 2017/6/11.
 */
public class UserSpecification implements Specification<User> {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        Predicate likeUsername = cb.like(root.get("username"), "%" + user.getUsername() + "%");
        cb.and(likeUsername);
        Predicate ageGe = cb.ge(root.get("age"), user.getAge());
        return cb.and(ageGe);

    }

//    Class UserQueryCondition {
//        private String  username;
//        private LocalDate fromDate;
//        private LocalDate toDate;
//        private Integer fromAge;
//        private Integer toAge;
//
//
//    }
}
