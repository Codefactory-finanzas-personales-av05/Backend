package com.finanzas.auth.aplicacion.dto.peticion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO que recibe el endpoint de login
@Data
public class PeticionLogin {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo invalido")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    private String contrasena;
}
