package com.muse_ai.rest.auth.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class RegisterRequest {

    @NotBlank @Size(max = 60)
    private String firstName;

    @NotBlank @Size(max = 60)
    private String lastName1;

    @Size(max = 60)
    private String lastName2;

    @NotNull
    private LocalDate birthDate;

    @NotBlank @Email @Size(max = 100)
    private String email;

    // Permite números, +, -, espacios. Largo 7–20
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "Formato de teléfono inválido")
    private String phone;

    // Validación de contraseña: mínimo 8, 1 mayúscula, 1 minúscula y 1 número
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Contraseña insegura (8+ con mayúscula, minúscula y número)")
    private String password;

    @NotBlank               // valores: beginner | intermediate | advanced
    private String artLevel;

    // getters/setters…


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName1() {
        return lastName1;
    }

    public void setLastName1(String lastName1) {
        this.lastName1 = lastName1;
    }

    public String getLastName2() {
        return lastName2;
    }

    public void setLastName2(String lastName2) {
        this.lastName2 = lastName2;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getArtLevel() {
        return artLevel;
    }

    public void setArtLevel(String artLevel) {
        this.artLevel = artLevel;
    }
}
