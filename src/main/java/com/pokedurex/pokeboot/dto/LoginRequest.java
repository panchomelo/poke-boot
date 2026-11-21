package com.pokedurex.pokeboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Debe ingresar su correo")
        @Email(message = "Ingrese un correo válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "Debe ingresar su contraseña")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {
}
