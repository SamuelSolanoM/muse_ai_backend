package com.muse_ai.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testEmail = "user@example.com";
    private final String rawPassword = "Test1234";

    @BeforeEach
    void setup() {
        Role role = roleRepository.findByName(RoleEnum.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleEnum.USER);
                    newRole.setDescription("Basic user role for testing");
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setFirstName("Daniel");
        user.setLastName1("Cruz");
        user.setLastName2("Test");
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone("88888888");
        user.setBirthDate(LocalDate.of(1990, 5, 10));
        user.setArtLevel(ArtLevel.BEGINNER);
        user.setRole(role);

        userRepository.save(user);
    }

    @WithMockUser
    @Test
    void shouldRetrieveAllUsersSuccessfully() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "1")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.pageNumber").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(5));
    }

    @WithMockUser
    @Test
    void shouldDeleteUserSuccessfully() throws Exception {

        Role role = roleRepository.findByName(RoleEnum.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleEnum.USER);
                    newRole.setDescription("Basic user role for testing delete");
                    return roleRepository.save(newRole);
                });

        User userToDelete = new User();
        userToDelete.setFirstName("Code");
        userToDelete.setLastName1("Horizon");
        userToDelete.setLastName2("DeleteTest");
        userToDelete.setEmail("delete@test.com");
        userToDelete.setPassword(passwordEncoder.encode("Delete1234"));
        userToDelete.setPhone("89998888");
        userToDelete.setBirthDate(LocalDate.of(1992, 8, 20));
        userToDelete.setArtLevel(ArtLevel.INTERMEDIATE);
        userToDelete.setRole(role);

        userRepository.saveAndFlush(userToDelete);

        mockMvc.perform(delete("/users/{userId}", userToDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"))
                .andExpect(jsonPath("$.data.email").value("delete@test.com"));
    }
    }
