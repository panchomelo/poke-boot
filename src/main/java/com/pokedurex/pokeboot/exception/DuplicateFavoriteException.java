package com.pokedurex.pokeboot.exception;

public class DuplicateFavoriteException extends RuntimeException {

    public DuplicateFavoriteException() {
        super("Ese Pokémon ya está en tus favoritos");
    }
}
