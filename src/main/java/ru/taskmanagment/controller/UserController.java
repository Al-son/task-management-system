package ru.taskmanagment.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.taskmanagment.payload.main.WebRs;
import ru.taskmanagment.payload.rq.*;
import ru.taskmanagment.payload.rs.RegisterLoginRs;
import ru.taskmanagment.payload.rs.UserRs;
import ru.taskmanagment.service.UserService;

import java.util.List;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public WebRs<List<UserRs>> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public WebRs<List<RegisterLoginRs>> register(@RequestBody RegisterRq registerRq) {
        return userService.register(registerRq);
    }

    @PostMapping("/login")
    public WebRs<List<RegisterLoginRs>> login(@RequestBody LoginRq loginRq) {
        return userService.login(loginRq);
    }

    @GetMapping("/profile")
    public WebRs<UserRs> profile(@RequestParam String email) {
        return userService.profile(email);
    }

    @PostMapping("/grant-admin")
    public WebRs<Void> grantAsAdmin(@RequestBody GrantedRq grantedRq) {
        return userService.grantAsAdmin(grantedRq);
    }

    @PostMapping("/revoke-admin")
    public WebRs<Void> unGrantAsAdmin(@RequestBody GrantedRq grantedRq) {
        return userService.unGrantAsAdmin(grantedRq);
    }

    @PostMapping("/forgot-password")
    public WebRs<String> forgetPassword(@RequestBody ResetPassRq resetPassRq, HttpServletRequest request) {
        return userService.forgetPassword(resetPassRq, request);
    }

    @PostMapping("/reset-password")
    public WebRs<String> resetPassword(@RequestParam String resetToken, @RequestBody PassRe passRe) {
        return userService.resetPassword(resetToken, passRe);
    }
}
