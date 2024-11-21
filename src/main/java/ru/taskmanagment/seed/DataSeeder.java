package ru.taskmanagment.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.Role;
import ru.taskmanagment.repository.RoleRepository;


@Service
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExist("ROLE_USER");
        createRoleIfNotExist("ROLE_ADMIN");
    }

    private void createRoleIfNotExist(String roleName) {
        if (roleRepository.findByAuthority(roleName).isEmpty()) {
            Role role = new Role(roleName);
            roleRepository.save(role);
            System.out.println("Created role: " + roleName);
        }
    }
}
