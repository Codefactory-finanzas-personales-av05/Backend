package com.finanzas.auth.aplicacion.dto.peticion;

import jakarta.validation.constraints.*;
import lombok.Data;

// DTO que recibe el endpoint de registro
@Data
public class PeticionRegistro {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene formato valido")
    @Size(max = 150, message = "El correo no puede tener mas de 150 caracteres")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._\\-])[A-Za-z\\d@$!%*?&#._\\-]{8,}$",
        message = "La contrasena necesita: minimo 8 caracteres, una mayuscula, una minuscula, un numero y un especial"
    )
    private String contrasena;

    @NotBlank(message = "Debes confirmar la contrasena")
    private String confirmarContrasena;

    // Validacion personalizada: las dos contrasenas deben ser iguales
    @AssertTrue(message = "Las contrasenas no coinciden")
    public boolean isContrasenaCoincide() {
        return contrasena != null && contrasena.equals(confirmarContrasena);
    }
}
