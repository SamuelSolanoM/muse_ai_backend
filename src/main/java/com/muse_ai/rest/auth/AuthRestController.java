package com.muse_ai.rest.auth;

import com.muse_ai.logic.entity.auth.AuthenticationService;
import com.muse_ai.logic.entity.auth.JwtService;
import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.LoginResponse;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import com.muse_ai.rest.auth.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.muse_ai.logic.entity.user.ArtLevel;
import com.muse_ai.logic.entity.http.HttpResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthRestController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;



    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody User user) {
        User authenticatedUser = authenticationService.authenticate(user);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        Optional<User> foundedUser = userRepository.findByEmail(user.getEmail());

        foundedUser.ifPresent(loginResponse::setAuthUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }
        user.setRole(optionalRole.get());
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new HttpResponse<>("El correo ya está en uso"));
        }

        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);
        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new HttpResponse<>("Role USER no configurado"));
        }

        User u = new User();
        u.setFirstName(req.getFirstName());
        u.setLastName1(req.getLastName1());
        u.setLastName2(req.getLastName2());
        u.setBirthDate(req.getBirthDate());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setArtLevel(ArtLevel.fromString(req.getArtLevel()));
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(optionalRole.get());

        User saved = userRepository.save(u);

        // Respuesta de éxito según tu criterio de aceptación
        HttpResponse<User> response = new HttpResponse<>(
                "Cuenta creada correctamente. Bienvenido a MuseAI", saved);
        return ResponseEntity.ok(response);
    }

}