package com.muse_ai.logic.entity;

import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureRole(RoleEnum.USER, "Usuario estándar MuseAI");
        ensureRole(RoleEnum.ADMIN, "Administrador MuseAI");
        Role superAdminRole = ensureRole(RoleEnum.SUPER_ADMIN, "Super administrador MuseAI");

        seedSuperAdmin(superAdminRole);
    }

    private Role ensureRole(RoleEnum roleEnum, String description) {
        return roleRepository.findByName(roleEnum)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleEnum);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void seedSuperAdmin(Role superAdminRole) {
        String email = "emilio@gmail.com";
        userRepository.findByEmail(email)
                .map(existing -> {
                    boolean passwordMatches;
                    try {
                        passwordMatches = passwordEncoder.matches("superadmin123", existing.getPassword());
                    } catch (IllegalArgumentException ex) {
                        passwordMatches = false;
                    }

                    if (!passwordMatches) {
                        existing.setPassword(passwordEncoder.encode("superadmin123"));
                    }

                    if (existing.getRole() == null || !RoleEnum.SUPER_ADMIN.equals(existing.getRole().getName())) {
                        existing.setRole(superAdminRole);
                    }

                    if (existing.getArtLevel() == null) {
                        existing.setArtLevel(ArtLevel.ADVANCED);
                    }

                    if (existing.getBirthDate() == null) {
                        existing.setBirthDate(LocalDate.of(1990, 1, 1));
                    }

                    if (existing.getFirstName() == null || existing.getFirstName().isBlank()) {
                        existing.setFirstName("Emilio");
                    }

                    if (existing.getLastName1() == null || existing.getLastName1().isBlank()) {
                        existing.setLastName1("Superadmin");
                    }

                    if (existing.getPhone() == null || existing.getPhone().isBlank()) {
                        existing.setPhone("0000000000");
                    }

                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setFirstName("Emilio");
                    user.setLastName1("Superadmin");
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("superadmin123"));
                    user.setPhone("0000000000");
                    user.setArtLevel(ArtLevel.ADVANCED);
                    user.setBirthDate(LocalDate.of(1990, 1, 1));
                    user.setRole(superAdminRole);
                    return userRepository.save(user);
                });
    }
}
