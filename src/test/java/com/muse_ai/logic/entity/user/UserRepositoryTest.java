package com.muse_ai.logic.entity.user;

import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindUserByEmail() {
        // === Arrange ===

        Role role = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new IllegalStateException("El rol USER no se encontró, verifica el RoleSeeder."));

        User user = new User();
        user.setFirstName("Daniel");
        user.setLastName1("Cruz");
        user.setLastName2("Test");
        String email = "daniel" + System.currentTimeMillis() + "@test.com";
        user.setEmail(email);
        user.setPassword("Password123");
        user.setPhone("88888888");
        user.setBirthDate(LocalDate.of(1970, 5, 10));
        user.setArtLevel(ArtLevel.BEGINNER);
        user.setRole(role);

        userRepository.save(user);

        // === Act ===
        Optional<User> foundUser = userRepository.findByEmail(email);

        // === Assert ===
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getFirstName()).isEqualTo("Daniel");
        assertThat(foundUser.get().getRole().getName()).isEqualTo(RoleEnum.USER);
    }
}
