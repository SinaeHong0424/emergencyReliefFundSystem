package com.ny.safeny;

import com.ny.safeny.model.User;
import com.ny.safeny.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * NYS Emergency Relief Fund System
 * Main Application Entry Point
 */
@SpringBootApplication
public class SafeNyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SafeNyApplication.class, args);
        System.out.println("""
                
                ╔════════════════════════════════════════════════════════════╗
                ║   NYS Emergency Relief Fund System Started Successfully    ║
                ║   Port: 8080                                             ║
                ║   Swagger UI: http://localhost:8080/api/swagger-ui.html  ║
                ╚════════════════════════════════════════════════════════════╝
                """);
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. find the 'admin' user(Create an empty object if not found)
            User admin = userRepository.findByUsername("admin").orElse(new User());

            // 2. force update information (ensure the password is reset)
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@2025")); // fixed password
            admin.setFullName("System Administrator");
            admin.setEmail("admin@ny.gov");
            admin.setPhone("000-000-0000");
            admin.setRole(User.Role.ROLE_ADMIN);
            admin.setEnabled(true);

            // 3. save (updates if the account exists, inserts if it does not)
            userRepository.save(admin);
            
            System.out.println(" Admin account ensured: admin / Admin@2025");
        };
    }
}