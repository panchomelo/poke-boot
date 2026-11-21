package com.pokedurex.pokeboot.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }
}
