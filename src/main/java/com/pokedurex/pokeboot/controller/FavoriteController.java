package com.pokedurex.pokeboot.controller;

import com.pokedurex.pokeboot.dto.AdminFavoriteResponse;
import com.pokedurex.pokeboot.dto.ErrorResponse;
import com.pokedurex.pokeboot.dto.FavoriteRequest;
import com.pokedurex.pokeboot.dto.FavoriteResponse;
import com.pokedurex.pokeboot.model.User;
import com.pokedurex.pokeboot.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favoritos", description = "CRUD de Pokémon favoritos por usuario")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "Listar los favoritos del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de favoritos"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public List<FavoriteResponse> list(@AuthenticationPrincipal User user) {
        return favoriteService.list(user);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar los favoritos de todos los usuarios (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista global de favoritos"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene rol ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public List<AdminFavoriteResponse> listAll() {
        return favoriteService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un favorito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito encontrado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Favorito inexistente o de otro usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public FavoriteResponse get(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return favoriteService.get(user, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar un Pokémon a favoritos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Favorito creado"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El Pokémon ya está en favoritos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public FavoriteResponse add(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FavoriteRequest request
    ) {
        return favoriteService.add(user, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un favorito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito eliminado"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Favorito inexistente o de otro usuario",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public void delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        favoriteService.delete(user, id);
    }
}
