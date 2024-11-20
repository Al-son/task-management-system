package ru.taskmanagment.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.taskmanagment.config.jwt.JwtTokenUtil;
import ru.taskmanagment.entity.Role;
import ru.taskmanagment.entity.User;
import ru.taskmanagment.exception.CustomerRoleNotFoundException;
import ru.taskmanagment.exception.UserNotFoundException;
import ru.taskmanagment.payload.main.WebRs;
import ru.taskmanagment.payload.rq.*;
import ru.taskmanagment.payload.rs.RegisterLoginRs;
import ru.taskmanagment.payload.rs.UserAuth;
import ru.taskmanagment.payload.rs.UserRs;
import ru.taskmanagment.repository.RoleRepository;
import ru.taskmanagment.repository.UserRepository;
import ru.taskmanagment.util.Constant;
import ru.taskmanagment.validation.ValidationUtil;


import javax.management.relation.RoleNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final ValidationUtil validationUtil;
    private final RoleRepository roleRepository;

    public WebRs<List<UserRs>> getAllUsers() {
        List<UserRs> userResponses = userRepository.findAll().stream()
                .map(user -> new UserRs(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());

        return new WebRs<>(HttpStatus.OK.value(), HttpStatus.OK.name(), userResponses);
    }

    public WebRs<List<RegisterLoginRs>> register(RegisterRq registerRq) throws CustomerRoleNotFoundException {
        validationUtil.validate(registerRq);
        if (userRepository.findByEmail(registerRq.getEmail()).isPresent()) {
            throw new UserNotFoundException("Email is already registered");
        }
        String encodedPassword = passwordEncoder.encode(registerRq.getPassword());
        RegisterRq encodePassword = new RegisterRq(
                registerRq.getName(),
                registerRq.getEmail(),
                encodedPassword
        );
        User user = encodePassword.toUsers();
//        Role userRole = roleRepository.findByName(Constant.ROLE_USER)
//                .orElseThrow(() -> new CustomerRoleNotFoundException("Role not found"));
        Optional<Role> userRole = roleRepository.findByName(Constant.ROLE_USER);
        if (userRole.isEmpty()) {
            System.out.println("Role not found in the database");
            throw new CustomerRoleNotFoundException("Role not found");
        } else {
            System.out.println("Role found: " + userRole.get().getName());
            user.getRoles().add(userRole.get());
        }
        userRepository.save(user);
        return generateAndAttempToken(registerRq.getEmail(), registerRq.getPassword());
    }

    public WebRs<List<RegisterLoginRs>> login(LoginRq loginRq) throws UserNotFoundException {
        if (!userRepository.findByEmail(loginRq.getEmail()).isPresent()) {
            throw new UserNotFoundException("Email address not registered");
        }
        return generateAndAttempToken(loginRq.getEmail(), loginRq.getPassword());
    }

    private WebRs<List<RegisterLoginRs>> generateAndAttempToken(String email, String password) throws UserNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Email address not registered"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("Email and password are incorrect");
        }
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            UserAuth userAuth = (UserAuth) authentication.getPrincipal();
            String accessToken = jwtTokenUtil.generateAccessToken(userAuth);
            return new WebRs<>(new RegisterLoginRs(accessToken));
        } catch (BadCredentialsException e) {
            throw new UserNotFoundException("Email and password are incorrect");
        }
    }

    public WebRs<UserRs> profile(String email) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("Access token is not valid");
        }
        return new WebRs<>(optionalUser.get().toUserRs());
    }

    public WebRs<Void> grantAsAdmin(GrantedRq grantedRq) {
        validationUtil.validate(grantedRq);
        findExistingUserById(grantedRq.getUserId());
        User user = userRepository.findById(grantedRq.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Role roleAdmin = roleRepository.findByName(Constant.ROLE_ADMIN)
                .orElseThrow(() -> new UserNotFoundException("Role not found"));

        if (user.getRoles().contains(roleAdmin)) {
            throw new UserNotFoundException("User is already an administrator");
        }

        user.getRoles().remove(roleRepository.findByName(Constant.ROLE_USER)
                .orElseThrow(() -> new UserNotFoundException("Role not found")));
        user.getRoles().add(roleAdmin);
        userRepository.save(user);

        return new WebRs<>();
    }

    public WebRs<Void> unGrantAsAdmin(GrantedRq grantedRq) {
        validationUtil.validate(grantedRq);
        findExistingUserById(grantedRq.getUserId());
        User user = userRepository.findById(grantedRq.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        Role roleAdmin = roleRepository.findByName(Constant.ROLE_ADMIN).orElseThrow(() -> new UserNotFoundException("Role not found"));

        if (!user.getRoles().contains(roleAdmin)) {
            throw new UserNotFoundException("User is not an administrator");
        }
        user.getRoles().remove(roleRepository.findByName(Constant.ROLE_USER)
                .orElseThrow(() -> new UserNotFoundException("Role not found")));
        user.getRoles().add(roleAdmin);
        userRepository.save(user);

        return new WebRs<>();
    }

    private void findExistingUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User id not found");
        }
    }

    public WebRs<String> forgetPassword(ResetPassRq resetPassRq, HttpServletRequest httpServletRequest) {
        validationUtil.validate(resetPassRq);
        User user = userRepository.findByEmail(resetPassRq.getEmail()).orElseThrow(() -> new UserNotFoundException("Email address is not registered!"));
        user.setTimeCreationToken(LocalDateTime.now());
        user.setResetToken(generateToken());
        userRepository.save(user);
        return new WebRs<>("Link has been sent to your email! Please check it.");
    }

    public WebRs<String> resetPassword(String resetToken, PassRe pass) {
        validationUtil.validate(pass);
        User user = userRepository.findByResetToken(resetToken).orElseThrow(() -> new UserNotFoundException("Invalid reset token"));
        LocalDateTime tokenCreation = user.getTimeCreationToken();
        if (isTokenExpired(tokenCreation)) {
            throw new UserNotFoundException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(pass.getNewPassword()));
        user.setResetToken(null);
        userRepository.save(user);

        return new WebRs<>("Password updated");
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isTokenExpired(LocalDateTime tokenTime) {
        Duration expiredTime = Duration.between(tokenTime, LocalDateTime.now());
        Duration resetTokenTime = Duration.ofMinutes(10);
        return expiredTime.toMinutes() > resetTokenTime.toMinutes();
    }
}
