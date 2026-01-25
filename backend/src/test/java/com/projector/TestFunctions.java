package com.projector;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.projector.core.model.UserCredentials;
import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;
import com.projector.role.model.Role;
import com.projector.roadmap.model.Roadmap;
import com.projector.task.model.Task;
import com.projector.user.model.User;

/**
 * Переиспользуемые функции для E2E тестов.
 * Использует Testcontainers для создания изолированной PostgreSQL БД.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class TestFunctions {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("projector_test")
            .withUsername("projector")
            .withPassword("projector")
            .withReuse(false);

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format(
                "r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getFirstMappedPort(),
                postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", () -> String.format(
                "jdbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getFirstMappedPort(),
                postgres.getDatabaseName()));
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6380);
    }

    protected void initWebTestClient() {
        webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    /**
     * Хеширует пароль используя SHA-256 (как в UserService).
     */
    protected String sha256Hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Выполняет логин и возвращает JWT токен из cookie.
     */
    protected String loginAndGetToken(String email, String password) {
        UserCredentials credentials = new UserCredentials(email, password);
        String cookieHeader = webTestClient
                .post()
                .uri("/api/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().exists("Set-Cookie")
                .returnResult(String.class)
                .getResponseHeaders()
                .getFirst("Set-Cookie");
        return extractTokenFromCookie(cookieHeader);
    }

    /**
     * Создает WebTestClient с JWT токеном в cookie.
     */
    protected WebTestClient webTestClientWithAuth(String token) {
        return WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultCookie("X-Auth", token)
                .build();
    }

    /**
     * Извлекает токен из Set-Cookie заголовка.
     */
    protected String extractTokenFromCookie(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        String[] parts = cookieHeader.split(";");
        if (parts.length > 0) {
            String[] keyValue = parts[0].split("=");
            if (keyValue.length == 2 && keyValue[0].trim().equals("X-Auth")) {
                return keyValue[1];
            }
        }
        return null;
    }

    // ========== Функции для создания тестовых данных ==========

    /**
     * Создает тестового пользователя.
     */
    protected User createTestUser(Long id, String email, String password) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
    }

    /**
     * Создает тестового пользователя с ролями.
     */
    protected User createTestUserWithRoles(Long id, String email, String password, List<Long> roleIds) {
        User user = createTestUser(id, email, password);
        user.setRoleIds(roleIds);
        return user;
    }

    /**
     * Создает тестовую роль.
     */
    protected Role createTestRole(Long id, String name, Set<String> authorities) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthorities(authorities);
        return role;
    }

    /**
     * Создает тестовый roadmap.
     */
    protected Roadmap createTestRoadmap(Long id, String projectName, Long authorId, String mission, String description) {
        return Roadmap.builder()
                .id(id)
                .projectName(projectName)
                .authorId(authorId)
                .mission(mission)
                .description(description)
                .createDate(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовую feature (с пустым списком FA - нужно добавить вручную для валидации).
     */
    protected Feature createTestFeature(Long id, Long year, Quarter quarter, Long authorId, String summary, String description) {
        return Feature.builder()
                .id(id)
                .year(year)
                .quarter(quarter)
                .authorId(authorId)
                .sprint(1L)
                .release("v1.0.0")
                .summary(summary)
                .description(description)
                .functionalAreaIds(List.of()) // Empty list - tests should set this
                .createDate(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовую feature с functional area IDs.
     */
    protected Feature createTestFeatureWithFa(Long id, Long year, Quarter quarter, Long authorId, String summary, String description, List<Long> faIds) {
        return Feature.builder()
                .id(id)
                .year(year)
                .quarter(quarter)
                .authorId(authorId)
                .sprint(1L)
                .release("v1.0.0")
                .summary(summary)
                .description(description)
                .functionalAreaIds(faIds)
                .createDate(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовую task.
     */
    protected Task createTestTask(Long id, Long featureId, Long authorId, String summary, String description) {
        return Task.builder()
                .id(id)
                .featureId(featureId)
                .authorId(authorId)
                .summary(summary)
                .description(description)
                .createDate(LocalDateTime.now())
                .build();
    }
}

