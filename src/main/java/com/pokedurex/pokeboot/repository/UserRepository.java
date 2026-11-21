package com.pokedurex.pokeboot.repository;

import com.pokedurex.pokeboot.model.Role;
import com.pokedurex.pokeboot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);
}
