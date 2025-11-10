package com.muse_ai.logic.entity.rol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Transactional
@Rollback
public class RoleSeederTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private RoleSeeder roleSeeder;

    @BeforeEach
    void setup() {
        roleSeeder = new RoleSeeder(roleRepository);
    }

    @Test
    void shouldSeedDefaultRolesIfNotPresent() {

        roleRepository.deleteAll();

        roleSeeder.onApplicationEvent(new ContextRefreshedEvent(applicationContext));

        Optional<Role> userRole = roleRepository.findByName(RoleEnum.USER);
        Optional<Role> adminRole = roleRepository.findByName(RoleEnum.ADMIN);
        Optional<Role> superAdminRole = roleRepository.findByName(RoleEnum.SUPER_ADMIN);

        assertThat(userRole).isPresent();
        assertThat(adminRole).isPresent();
        assertThat(superAdminRole).isPresent();

        assertThat(userRole.get().getDescription()).isEqualTo("Default user role");
        assertThat(adminRole.get().getDescription()).isEqualTo("Administrator role");
        assertThat(superAdminRole.get().getDescription()).isEqualTo("Super Administrator role");
    }
}
