package com.pokedurex.pokeboot.repository;

import com.pokedurex.pokeboot.model.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "user")
    List<Favorite> findAllByOrderByCreatedAtDesc();

    boolean existsByUserIdAndPokemonId(Long userId, Integer pokemonId);
}
