package com.java.sportshub;

import com.java.sportshub.daos.RoleDAO;
import com.java.sportshub.models.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


import java.util.List;

@SpringBootApplication
public class SportshubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportshubApplication.class, args);
    }
    @Bean
    CommandLineRunner initRoles(RoleDAO roleDAO) { //
        return args -> {
            if (roleDAO.count() == 0) {
                System.out.println("No roles found. Creating ADMIN and USER roles...");
                Role admin = new Role();
                admin.setRoleName("ADMIN");
                Role user = new Role();
                user.setRoleName("USER");
                roleDAO.save(admin);
                roleDAO.save(user);
                System.out.println("Roles created successfully.");
            } else {
                System.out.println("Roles already exist in database. Skipping creation.");
            }
        };
    }
}
