package com.pokedurex.pokeboot.dto;

import jakarta.validation.constraints.Size;

public record FavoriteUpdateRequest(

        @Size(max = 100, message = "El alias no puede superar los 100 caracteres")
        String alias
) {
}
