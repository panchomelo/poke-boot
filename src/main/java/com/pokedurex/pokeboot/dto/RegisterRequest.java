package com.pokedurex.pokeboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Debe ingresar su nombre")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "Debe ingresar su correo")
        @Email(message = "Ingrese un correo válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "Debe ingresar su contraseña")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {
}
