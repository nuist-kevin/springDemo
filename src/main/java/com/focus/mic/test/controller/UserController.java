package com.focus.mic.test.controller;

import com.focus.mic.test.entity.User;
import com.focus.mic.test.repository.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;


@Controller
@RequestMapping("/users")
public class UserController {

    private UserJpaRepository userRepository;

    @Autowired
    public UserController(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String users(Model model) {
        model.addAttribute("userList",userRepository.findAll());
        return "/users";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public String user(@PathVariable("id") int userId, Model model) {
        model.addAttribute("user", userRepository.findOne(userId));
        return "/user";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String register(@Valid User user, Errors errors) {
        if (errors.hasErrors()) {
            return "registerForm";
        }
        User savedUser = userRepository.save(user);
        return "redirect:/users/" + savedUser.getId();
    }
}
