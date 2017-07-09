package com.focus.mic.test.controller;

import com.focus.mic.test.entity.User;
import com.focus.mic.test.repository.UserJpaRepository;
import com.focus.mic.test.repository.UserRepository;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceView;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UserControllerTest {

    @Test
    public void shouldShowRecentUsers() throws Exception {

        List<User> expectedUsers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            expectedUsers.add(new User("caiwen" + i, "6886377" + "-" + i, i));
        }

        UserJpaRepository mockUserRepository = mock(UserJpaRepository.class);
        when(mockUserRepository.findAll()).thenReturn(expectedUsers);

        UserController controller = new UserController(mockUserRepository);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setSingleView(new InternalResourceView("/WEB-INF/jsp/users.jsp"))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.view().name("users"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("userList"))
                .andExpect(MockMvcResultMatchers.model().attribute("userList", hasItems(expectedUsers.toArray())));
    }


    @Test
    public void shouldShowSpecificUser() throws Exception {
        User user = new User("caiwen", "12345", 32);
        UserJpaRepository mockUserRepository = mock(UserJpaRepository.class);
        when(mockUserRepository.findOne(12345)).thenReturn(user);

        UserController controller = new UserController(mockUserRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(MockMvcRequestBuilders.get("/users/12345"))
                .andExpect(MockMvcResultMatchers.view().name("user"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("user"))
                .andExpect(MockMvcResultMatchers.model().attribute("user", user));
    }

    @Test
    public void shouldProcessRegistration() throws Exception {

        User user = new User("caiwen", "123456", 32);

        User savedUser = new User("caiwen", "123456", 32);
        savedUser.setId(123456);

        UserJpaRepository mockUserRepository = mock(UserJpaRepository.class);
        when(mockUserRepository.save(user))
                .thenReturn(user);

        UserController controller = new UserController(mockUserRepository);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/users/register")
                .param("username", "caiwen")
                .param("password", "123456")
                .param("age", "32")

        ).andExpect(MockMvcResultMatchers.redirectedUrl("/users/12345"));

    }
}
