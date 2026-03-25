package com.finanzas.auth.aplicacion.dto.peticion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * DTO para el endpoint que guarda la descripcion del cliente.
 * Recibe las credenciales para autenticar + la descripcion a guardar.
 */
@Data
public class PeticionDescripcion {

    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    private String contrasena;

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;
}
