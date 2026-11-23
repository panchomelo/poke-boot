package com.pokedurex.pokeboot.dto;

import com.pokedurex.pokeboot.model.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long id,
        Integer pokemonId,
        LocalDateTime createdAt
) {

    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getPokemonId(),
                favorite.getCreatedAt()
        );
    }
}
