package com.finanzas.auth.aplicacion.dto.peticion;

import jakarta.validation.constraints.*;
import lombok.Data;

// DTO para verificar el correo con el codigo de 6 digitos
@Data
public class PeticionVerificacion {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo invalido")
    private String correo;

    @NotBlank(message = "El codigo es obligatorio")
    @Size(min = 6, max = 6, message = "El codigo debe tener exactamente 6 digitos")
    private String codigo;
}
