package com.muse_ai.logic.entity.rol;

import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Order(2)
@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository  userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {
        final String email = "super.admin@gmail.com";
        if (userRepository.findByEmail(email).isPresent()) return;

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);
        if (optionalRole.isEmpty()) return;

        User user = new User();
        user.setFirstName("Super");
        user.setLastName1("Admin");
        user.setLastName2(null);                 // opcional
        user.setEmail(email);
        user.setPhone(null);                     // opcional
        user.setBirthDate(LocalDate.of(1990, 1, 1)); // requerido (no null)
        user.setArtLevel(ArtLevel.ADVANCED);     // requerido (no null)
        user.setPassword(passwordEncoder.encode("superadmin123"));
        user.setRole(optionalRole.get());

        userRepository.save(user);
    }
}
