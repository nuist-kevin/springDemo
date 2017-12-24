package com.focus.mic.test.service.impl;

import com.focus.mic.test.entity.User;
import com.focus.mic.test.repository.UserJpaRepository;
import com.focus.mic.test.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserJpaRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
