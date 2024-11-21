package ru.taskmanagment.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.Role;
import ru.taskmanagment.repository.RoleRepository;


@Service
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    //private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final RoleRepository roleRepository;

//    @Override
//    public void run(String... args) throws Exception {
//        try {
//            generateRoles();
//        } catch (Exception e) {
//            logger.error("Error occurred during data seeding: {}", e.getMessage(), e);
//        }
//    }

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

//    private void generateRoles() {
//        if (roleRepository.findAll().isEmpty()) {
//            Role adminRole = new Role(Constant.ROLE_ADMIN);
//            Role userRole = new Role(Constant.ROLE_USER);
//            roleRepository.saveAll(List.of(adminRole, userRole));
//            logger.info("Roles created: {} and {}", Constant.ROLE_USER, Constant.ROLE_ADMIN);
//        } else {
//            logger.info("Roles already exist in the database.");
//        }
//    }
}
