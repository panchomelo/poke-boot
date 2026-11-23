package com.pokedurex.pokeboot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedurex.pokeboot.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FavoriteIntegrationTest {

    private static final String ADMIN_EMAIL = "admin-test@pokedurex.local";
    private static final String ADMIN_PASSWORD = "admin-password-123";

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0")
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-1234567890");
        registry.add("app.admin.email", () -> ADMIN_EMAIL);
        registry.add("app.admin.password", () -> ADMIN_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    void unUsuarioGestionaSusFavoritosYNoPuedeVerLaListaGlobal() throws Exception {
        String email = "ash-test@pokedurex.local";
        String token = registrarEIniciarSesion("Ash", email, "pikachu123");

        MvcResult creado = mockMvc.perform(post("/api/favorites")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pokemonId": 25}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pokemonId").value(25))
                .andReturn();

        long favoritoId = leerJson(creado).get("id").asLong();

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pokemonId").value(25));

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pokemonId": 25}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/favorites/{id}", favoritoId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alias": "Mi compañero"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("Mi compañero"));

        mockMvc.perform(get("/api/favorites/all")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/favorites/{id}", favoritoId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unAdminVeYeliminaLosFavoritosDeCualquierUsuario() throws Exception {
        String email = "misty-test@pokedurex.local";
        String token = registrarEIniciarSesion("Misty", email, "starmie123");

        MvcResult creado = mockMvc.perform(post("/api/favorites")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pokemonId": 121, "alias": "Estrella"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long favoritoId = leerJson(creado).get("id").asLong();
        String adminToken = iniciarSesion(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/favorites/all")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].user.email", hasItem(email)))
                .andExpect(jsonPath("$[*].alias", hasItem("Estrella")));

        mockMvc.perform(delete("/api/favorites/{id}", favoritoId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unUsuarioNoPuedeAccederNiBorrarFavoritosDeOtro() throws Exception {
        String tokenDueno = registrarEIniciarSesion(
                "Brock", "brock-test@pokedurex.local", "onix1234"
        );
        String tokenIntruso = registrarEIniciarSesion(
                "Gary", "gary-test@pokedurex.local", "eevee123"
        );

        MvcResult creado = mockMvc.perform(post("/api/favorites")
                        .header("Authorization", bearer(tokenDueno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pokemonId": 95}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        long favoritoId = leerJson(creado).get("id").asLong();

        mockMvc.perform(get("/api/favorites/{id}", favoritoId)
                        .header("Authorization", bearer(tokenIntruso)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/favorites/{id}", favoritoId)
                        .header("Authorization", bearer(tokenIntruso)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        favoriteRepository.deleteById(favoritoId);
    }

    @Test
    void unUsuarioNoPuedeSuperarSeisFavoritos() throws Exception {
        String token = registrarEIniciarSesion(
                "Max", "max-test@pokedurex.local", "max12345"
        );

        List<Long> ids = new ArrayList<>();

        for (int pokemonId = 1; pokemonId <= 6; pokemonId++) {
            MvcResult creado = mockMvc.perform(post("/api/favorites")
                            .header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"pokemonId\": " + pokemonId + "}"))
                    .andExpect(status().isCreated())
                    .andReturn();

            ids.add(leerJson(creado).get("id").asLong());
        }

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pokemonId": 7}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Solo puedes tener hasta 6 Pokémon favoritos"));

        for (Long id : ids) {
            mockMvc.perform(delete("/api/favorites/{id}", id)
                            .header("Authorization", bearer(token)))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void elRegistroPublicoSiempreCreaUnUsuarioNormal() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Intruso",
                                  "email": "intruso-test@pokedurex.local",
                                  "password": "intruso123",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    private String registrarEIniciarSesion(String nombre, String email, String password)
            throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Registro(nombre, email, password))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        return iniciarSesion(email, password);
    }

    private String iniciarSesion(String email, String password) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Credenciales(email, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        return leerJson(resultado).get("token").asText();
    }

    private JsonNode leerJson(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Registro(String name, String email, String password) {
    }

    private record Credenciales(String email, String password) {
    }
}
