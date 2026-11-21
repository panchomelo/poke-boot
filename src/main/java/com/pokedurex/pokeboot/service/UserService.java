package com.pokedurex.pokeboot.service;

import com.pokedurex.pokeboot.dto.AuthResponse;
import com.pokedurex.pokeboot.dto.LoginRequest;
import com.pokedurex.pokeboot.dto.RegisterRequest;
import com.pokedurex.pokeboot.dto.UserResponse;
import com.pokedurex.pokeboot.exception.DuplicateEmailException;
import com.pokedurex.pokeboot.exception.InvalidCredentialsException;
import com.pokedurex.pokeboot.model.Role;
import com.pokedurex.pokeboot.model.User;
import com.pokedurex.pokeboot.repository.UserRepository;
import com.pokedurex.pokeboot.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String name = request.name().trim();
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = new User(name, email, passwordEncoder.encode(request.password()), Role.USER);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }

        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
