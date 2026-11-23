package com.pokedurex.pokeboot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(

        @NotNull(message = "Debe indicar el número del Pokémon")
        @Min(value = 1, message = "El Pokémon debe estar entre 1 y 151")
        @Max(value = 151, message = "El Pokémon debe estar entre 1 y 151")
        Integer pokemonId
) {
}
