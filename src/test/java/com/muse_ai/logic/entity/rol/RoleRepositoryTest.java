package com.muse_ai.logic.entity.rol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@Rollback
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldFindUserRoleByName() {
        // === Act ===
        Optional<Role> role = roleRepository.findByName(RoleEnum.USER);

        // === Assert ===
        assertThat(role).isPresent();
        assertThat(role.get().getName()).isEqualTo(RoleEnum.USER);
    }

    @Test
    void shouldFindAdminRoleByName() {
        // === Act ===
        Optional<Role> role = roleRepository.findByName(RoleEnum.ADMIN);

        // === Assert ===
        assertThat(role).isPresent();
        assertThat(role.get().getName()).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    void shouldThrowExceptionWhenInvalidEnumValueIsUsed() {
        assertThatThrownBy(() -> RoleEnum.valueOf("FAKE_ROLE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
    }


}
