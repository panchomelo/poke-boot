package com.pokedurex.pokeboot.dto;

import com.pokedurex.pokeboot.model.Favorite;

import java.time.LocalDateTime;

public record AdminFavoriteResponse(
        Long id,
        Integer pokemonId,
        String alias,
        LocalDateTime createdAt,
        FavoriteUserResponse user
) {

    public static AdminFavoriteResponse from(Favorite favorite) {
        return new AdminFavoriteResponse(
                favorite.getId(),
                favorite.getPokemonId(),
                favorite.getAlias(),
                favorite.getCreatedAt(),
                FavoriteUserResponse.from(favorite.getUser())
        );
    }
}
