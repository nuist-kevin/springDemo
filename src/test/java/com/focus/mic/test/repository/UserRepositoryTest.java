package com.focus.mic.test.repository;

import com.focus.mic.test.config.JpaConfiguration;
import com.focus.mic.test.entity.User;
import com.focus.mic.test.util.MyScriptUtils;
import java.sql.DriverManager;
import jdk.nashorn.api.scripting.ScriptUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ContextConfiguration(classes = {JpaConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class UserRepositoryTest {

    @Resource
    private UserJpaRepository userRepository;

    @Test
    public void test() {
        User user = userRepository.findOne(1);
        System.out.println(user.getBirthday());
    }

    @Test
    @Transactional
    public void add() {
        User user = new User("caiwen", "123456", 32);
        user.setBirthday(LocalDate.of(1985, 7, 9));
                userRepository.save(user);
        System.out.println(userRepository.findOne(user.getId()).getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    @Transactional
    public void testQueryByExample() {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("username", match -> match.contains())
                .withMatcher("birthday", matcher -> matcher.contains());
    }

    @Test
    @Transactional
    public void testQueryBySpec() {
        LocalDate fromDate = LocalDate.of(1980, 1, 1);
        LocalDate toDate = LocalDate.of(1989, 12, 31);
        List<User> users =userRepository.findAll((root, query, cb) -> {
            Predicate condition = cb.between(root.get("birthday"), fromDate, toDate);
            Predicate con = cb.like(root.get("username"), "%wen");
            /*
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(condition);
            predicateList.add(con);
            Predicate[] predicates = new Predicate[predicateList.size()];
            return cb.and(predicateList.toArray(predicates));
              */
            query.where(cb.and(con, condition));

            return query.getRestriction();
        });
        System.out.println(users.get(0).getUsername());

    }



}
