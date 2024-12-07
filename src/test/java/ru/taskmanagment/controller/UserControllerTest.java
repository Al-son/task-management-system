package ru.taskmanagment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.taskmanagment.payload.main.WebRs;
import ru.taskmanagment.payload.rq.LoginRq;
import ru.taskmanagment.payload.rq.RegisterRq;
import ru.taskmanagment.payload.rs.RegisterLoginRs;
import ru.taskmanagment.payload.rs.UserRs;
import ru.taskmanagment.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetAllUsers() throws Exception {
        UserRs userRs = new UserRs();
        userRs.setEmail("alson@gmail.com");
        userRs.setName("Alson");

        WebRs<List<UserRs>> webRs = new WebRs<>();
        webRs.setData(Collections.singletonList(userRs));

        when(userService.getAllUsers()).thenReturn(webRs);

        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data[0].email").value("alson@gmail.com"))
                .andExpect((ResultMatcher) jsonPath("$.data[0].name").value("Alson"));
    }

    @Test
    void testProfile() throws Exception {
        String email = "alson@gmail.com";
        UserRs userRs = new UserRs();
        userRs.setEmail(email);
        userRs.setName("Alson");

        WebRs<UserRs> webRs = new WebRs<>();
        webRs.setData(userRs);

        when(userService.profile(email)).thenReturn(webRs);

        mockMvc.perform(get("/auth/profile")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect((ResultMatcher) jsonPath("$.data.email").value(email))
                .andExpect((ResultMatcher) jsonPath("$.data.name").value("Alson"));
    }

}