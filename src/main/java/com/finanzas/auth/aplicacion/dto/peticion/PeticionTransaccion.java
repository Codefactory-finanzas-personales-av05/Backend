package com.finanzas.auth.aplicacion.dto.peticion;

import com.finanzas.auth.dominio.modelo.Transaccion;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * DTO de entrada para registrar un ingreso o gasto (HU-03 / HU-04).
 * El correoUsuario NO viene aqui — viene del token JWT en el controlador.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PeticionTransaccion {

    @NotBlank(message = "El nombre de la transaccion es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 13, fraction = 2, message = "El monto no puede tener mas de 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "La fecha de la transaccion es obligatoria")
    private LocalDateTime movimientoEn;

    @NotNull(message = "El tipo de transaccion es obligatorio (INGRESO o GASTO)")
    private Transaccion.TipoTransaccion tipo;

    @NotNull(message = "La categoria es obligatoria")
    private Long idCategoria;

    // Descripcion opcional segun HU-03
    private String descripcion;
}
