package com.pokedurex.pokeboot.dto;

import com.pokedurex.pokeboot.model.User;

public record FavoriteUserResponse(Long id, String name, String email) {

    public static FavoriteUserResponse from(User user) {
        return new FavoriteUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
