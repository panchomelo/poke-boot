package com.pokedurex.pokeboot.config;

import com.pokedurex.pokeboot.model.Role;
import com.pokedurex.pokeboot.model.User;
import com.pokedurex.pokeboot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Configuration
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    @Bean
    public CommandLineRunner crearAdminInicial(
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
                return;
            }

            if (userRepository.countByRole(Role.ADMIN) > 0) {
                return;
            }

            if (password.length() < 8) {
                log.warn("APP_ADMIN_PASSWORD debe tener al menos 8 caracteres; no se creó el admin");
                return;
            }

            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

            if (userRepository.existsByEmail(normalizedEmail)) {
                log.warn(
                        "Ya existe una cuenta con el correo de admin {}. Promuévela manualmente con SQL",
                        normalizedEmail
                );
                return;
            }

            User admin = new User(
                    "Administrador",
                    normalizedEmail,
                    passwordEncoder.encode(password),
                    Role.ADMIN
            );

            userRepository.save(admin);
            log.info("Administrador inicial creado con el correo {}", normalizedEmail);
        };
    }
}
