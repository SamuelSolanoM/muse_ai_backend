package com.muse_ai.rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import com.muse_ai.rest.auth.dto.RegisterRequest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String testEmail = "userlogin@test.com";
    private final String rawPassword = "Password123";

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        Role role = roleRepository.findByName(RoleEnum.USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleEnum.USER);
                    newRole.setDescription("Basic user role for authentication tests");
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



    @Test
    void shouldAuthenticateUserAndReturnJwtToken() throws Exception {
        String loginRequest = objectMapper.writeValueAsString(new LoginRequest(testEmail, rawPassword));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    @WithMockUser
    @Test
    void shouldRequestPasswordRecoverySuccessfully() throws Exception {
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);

        mockMvc.perform(post("/auth/password/recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si el correo existe, se enviará un código de verificación"));
    }


    static class PasswordRecoveryRequest {
        public String email;

        public PasswordRecoveryRequest(String email) {
            this.email = email;
        }
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Juan");
        registerRequest.setLastName1("Perez");
        registerRequest.setLastName2("Test");
        registerRequest.setBirthDate(LocalDate.of(1995, 3, 12));
        registerRequest.setEmail("juan@test.com");
        registerRequest.setPhone("88889999");
        registerRequest.setArtLevel("BEGINNER");
        registerRequest.setPassword(rawPassword);


        String jsonRequest = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cuenta creada correctamente. Bienvenido a MuseAI"))
                .andExpect(jsonPath("$.data.email").value("juan@test.com"));
    }

}
