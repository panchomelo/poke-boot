package com.pokedurex.pokeboot.exception;

public class FavoriteLimitException extends RuntimeException {

    public FavoriteLimitException() {
        super("Solo puedes tener hasta 6 Pokémon favoritos");
    }
}
