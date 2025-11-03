package com.muse_ai.rest.auth;

import com.muse_ai.logic.entity.auth.AuthenticationService;
import com.muse_ai.logic.entity.auth.JwtService;
import com.muse_ai.logic.entity.auth.PasswordRecoveryService;
import com.muse_ai.logic.entity.rol.Role;
import com.muse_ai.logic.entity.rol.RoleEnum;
import com.muse_ai.logic.entity.rol.RoleRepository;
import com.muse_ai.logic.entity.user.LoginResponse;
import com.muse_ai.logic.entity.user.User;
import com.muse_ai.logic.entity.user.UserRepository;
import com.muse_ai.rest.auth.dto.PasswordRecoveryRequest;
import com.muse_ai.rest.auth.dto.PasswordResetRequest;
import com.muse_ai.rest.auth.dto.RegisterRequest;
import com.muse_ai.rest.auth.dto.LoginRequest;
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
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService,
                              PasswordRecoveryService passwordRecoveryService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginRequest req) {
        User authenticatedUser = authenticationService.authenticate(req.email(), req.password());

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        loginResponse.setAuthUser(authenticatedUser);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/password/recovery")
    public ResponseEntity<HttpResponse<Void>> requestPasswordRecovery(@Valid @RequestBody PasswordRecoveryRequest req) {
        passwordRecoveryService.requestReset(req.getEmail());
        HttpResponse<Void> response = new HttpResponse<>("Si el correo existe, se enviará un código de verificación");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<HttpResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest req) {
        passwordRecoveryService.resetPassword(req.getEmail(), req.getCode(), req.getNewPassword());
        HttpResponse<Void> response = new HttpResponse<>("Contraseña actualizada correctamente");
        return ResponseEntity.ok(response);
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
