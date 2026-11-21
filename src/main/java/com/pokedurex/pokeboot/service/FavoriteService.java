package com.pokedurex.pokeboot.service;

import com.pokedurex.pokeboot.dto.AdminFavoriteResponse;
import com.pokedurex.pokeboot.dto.FavoriteRequest;
import com.pokedurex.pokeboot.dto.FavoriteResponse;
import com.pokedurex.pokeboot.dto.FavoriteUpdateRequest;
import com.pokedurex.pokeboot.exception.DuplicateFavoriteException;
import com.pokedurex.pokeboot.exception.FavoriteNotFoundException;
import com.pokedurex.pokeboot.model.Favorite;
import com.pokedurex.pokeboot.model.Role;
import com.pokedurex.pokeboot.model.User;
import com.pokedurex.pokeboot.repository.FavoriteRepository;
import com.pokedurex.pokeboot.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(User user) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteResponse get(User user, Long id) {
        return FavoriteResponse.from(findAccessible(user, id));
    }

    @Transactional
    public FavoriteResponse add(User user, FavoriteRequest request) {
        if (favoriteRepository.existsByUserIdAndPokemonId(user.getId(), request.pokemonId())) {
            throw new DuplicateFavoriteException();
        }

        User owner = userRepository.getReferenceById(user.getId());
        Favorite favorite = new Favorite(owner, request.pokemonId(), normalizeAlias(request.alias()));

        try {
            favoriteRepository.saveAndFlush(favorite);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateFavoriteException();
        }

        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public FavoriteResponse update(User user, Long id, FavoriteUpdateRequest request) {
        Favorite favorite = findAccessible(user, id);
        favorite.setAlias(normalizeAlias(request.alias()));

        return FavoriteResponse.from(favorite);
    }

    @Transactional
    public void delete(User user, Long id) {
        favoriteRepository.delete(findAccessible(user, id));
    }

    @Transactional(readOnly = true)
    public List<AdminFavoriteResponse> listAll() {
        return favoriteRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AdminFavoriteResponse::from)
                .toList();
    }

    private Favorite findAccessible(User user, Long id) {
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(FavoriteNotFoundException::new);

        boolean isOwner = favorite.getUser().getId().equals(user.getId());

        if (!isOwner && user.getRole() != Role.ADMIN) {
            throw new FavoriteNotFoundException();
        }

        return favorite;
    }

    private String normalizeAlias(String alias) {
        if (alias == null) {
            return null;
        }

        String trimmed = alias.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
