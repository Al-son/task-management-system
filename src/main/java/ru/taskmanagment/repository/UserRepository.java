package ru.taskmanagment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.taskmanagment.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.name = :email")
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
}
