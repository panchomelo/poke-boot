package com.pokedurex.pokeboot.dto;

import com.pokedurex.pokeboot.model.Role;
import com.pokedurex.pokeboot.model.User;

public record UserResponse(Long id, String name, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
