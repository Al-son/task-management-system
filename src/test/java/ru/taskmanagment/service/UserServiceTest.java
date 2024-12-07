package ru.taskmanagment.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.taskmanagment.entity.User;
import ru.taskmanagment.payload.main.WebRs;
import ru.taskmanagment.payload.rs.UserRs;
import ru.taskmanagment.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void testFindUser() {
        MockitoAnnotations.openMocks(this);
        User mockUser = new User(1L, "John Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        WebRs<UserRs> result = userService.findById(1L);
        assertEquals("John Doe", result.getName());
        verify(userRepository).findById(1L);
    }
}
