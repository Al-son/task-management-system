package ru.taskmanagment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.taskmanagment.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByAuthority(String authority);
}
