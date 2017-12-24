package com.focus.mic.test.controller;

import com.focus.mic.test.domain.UserCommand;
import com.focus.mic.test.entity.User;
import com.focus.mic.test.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;

@Controller
@RequestMapping("/")
public class HomeController {

    @Resource
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String home() {
        return "login";
    }

    @RequestMapping(path = "login", method = RequestMethod.POST)
    public ModelAndView login(ModelAndView modelAndView, UserCommand userCommand) {
        User user = userService.getUserByUsername(userCommand.getUsername());
        modelAndView.setViewName("redirect:index");

        if (user != null) {
            if (user.getPassword().equals(userCommand.getPassword())) {
                modelAndView.setViewName("redirect:main");
            }
        }
        return modelAndView;
    }

    @RequestMapping(path = "main", method = RequestMethod.GET)
    public String main() {
        return "main";
    }

    public static void main(String[] args) {
        File[] hiddenFiles = new File(".").listFiles(File::isHidden);
    }
}
