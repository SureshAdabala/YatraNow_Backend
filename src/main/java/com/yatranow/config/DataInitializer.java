package com.yatranow.config;

import com.yatranow.entity.Admin;
import com.yatranow.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking Admin account configuration...");

        String defaultEmail = "suresh@gmail.com";
        String defaultPassword = "Suresh@55";
        String defaultName = "Suresh";

        // Check if the default admin exists
        Optional<Admin> existingAdmin = adminRepository.findByEmail(defaultEmail);

        if (existingAdmin.isPresent()) {
            log.info("Default admin ({}) exists. Updating password to ensure sync.", defaultEmail);
            Admin admin = existingAdmin.get();
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            adminRepository.save(admin);
        } else {
            log.info("Default admin ({}) not found. Creating new admin.", defaultEmail);
            Admin newAdmin = new Admin();
            newAdmin.setName(defaultName);
            newAdmin.setEmail(defaultEmail);
            newAdmin.setPassword(passwordEncoder.encode(defaultPassword));
            newAdmin.setRole("ADMIN");
            adminRepository.save(newAdmin);
        }

        // Enforce "Only One Admin" rule
        List<Admin> allAdmins = adminRepository.findAll();
        if (allAdmins.size() > 1) {
            log.warn("Multiple admins found ({}). Removing others to enforce single-admin policy.", allAdmins.size());
            for (Admin admin : allAdmins) {
                if (!admin.getEmail().equals(defaultEmail)) {
                    log.info("Deleting extra admin: {}", admin.getEmail());
                    adminRepository.delete(admin);
                }
            }
        }

        log.info("Admin configuration completed. Default admin is ready.");
    }
}
