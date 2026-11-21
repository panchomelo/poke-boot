package com.pokedurex.pokeboot.exception;

public class FavoriteNotFoundException extends RuntimeException {

    public FavoriteNotFoundException() {
        super("Favorito no encontrado");
    }
}
