package com.muse_ai.rest.admin;

import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public User createAdministrator(@RequestBody User newAdminUser) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }

        User user = new User();
        // Usa los campos nuevos; si tu payload trae "name/lastname", mapea antes o usa DTO
        user.setFirstName(newAdminUser.getFirstName());
        user.setLastName1(newAdminUser.getLastName1());
        user.setLastName2(newAdminUser.getLastName2());
        user.setEmail(newAdminUser.getEmail());
        user.setPhone(newAdminUser.getPhone());
        user.setBirthDate(
                newAdminUser.getBirthDate() != null ? newAdminUser.getBirthDate() : LocalDate.of(1990,1,1)
        );
        user.setArtLevel(
                newAdminUser.getArtLevel() != null ? newAdminUser.getArtLevel() : ArtLevel.ADVANCED
        );
        user.setPassword(passwordEncoder.encode(newAdminUser.getPassword()));
        user.setRole(optionalRole.get());

        return userRepository.save(user);
    }
}
