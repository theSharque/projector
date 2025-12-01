package com.projector.core.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.projector.feature.model.Feature;
import com.projector.feature.model.Quarter;
import com.projector.role.model.Role;
import com.projector.roadmap.model.Roadmap;
import com.projector.task.model.Task;
import com.projector.user.model.User;

/**
 * Helper class for E2E API tests.
 * Provides reusable methods for creating test data and common assertions.
 */
public class ApiTestHelper {

    // Test data constants
    public static final Long TEST_ADMIN_ID = 1L;
    public static final Long TEST_USER_ID = 2L;
    public static final Long TEST_ROLE_ID = 1L;
    public static final Long TEST_ROADMAP_ID = 1L;
    public static final Long TEST_FEATURE_ID = 1L;
    public static final Long TEST_TASK_ID = 1L;

    public static final String TEST_ADMIN_EMAIL = "admin";
    public static final String TEST_ADMIN_PASSWORD = "admin";
    public static final String TEST_USER_EMAIL = "testuser@example.com";
    public static final String TEST_USER_PASSWORD = "testpass123";
    public static final String TEST_ROLE_NAME = "TEST_ROLE";

    // Role test data
    public static Role createTestRole() {
        Role role = new Role();
        role.setId(TEST_ROLE_ID);
        role.setName(TEST_ROLE_NAME);
        role.setAuthorities(Set.of("USER_VIEW", "ROLE_VIEW"));
        return role;
    }

    public static Role createTestRole(Long id, String name, Set<String> authorities) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setAuthorities(authorities);
        return role;
    }

    // User test data
    public static User createTestAdmin() {
        return User.builder()
                .id(TEST_ADMIN_ID)
                .email(TEST_ADMIN_EMAIL)
                .password(TEST_ADMIN_PASSWORD)
                .build();
    }

    public static User createTestUser() {
        return User.builder()
                .id(TEST_USER_ID)
                .email(TEST_USER_EMAIL)
                .password(TEST_USER_PASSWORD)
                .build();
    }

    public static User createTestUser(Long id, String email, String password) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
    }

    public static User createTestUserWithRoles(Long id, String email, String password, List<Long> roleIds) {
        User user = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
        user.setRoleIds(roleIds);
        return user;
    }

    // Roadmap test data
    public static Roadmap createTestRoadmap() {
        return Roadmap.builder()
                .id(TEST_ROADMAP_ID)
                .projectName("Test Project")
                .authorId(TEST_ADMIN_ID)
                .mission("Build amazing software")
                .description("Test roadmap description")
                .participantIds(List.of(TEST_ADMIN_ID))
                .build();
    }

    public static Roadmap createTestRoadmap(Long id, String projectName, Long authorId) {
        return Roadmap.builder()
                .id(id)
                .projectName(projectName)
                .authorId(authorId)
                .mission("Build amazing software")
                .description("Test roadmap description")
                .build();
    }

    // Feature test data
    public static Feature createTestFeature() {
        return Feature.builder()
                .id(TEST_FEATURE_ID)
                .year(2024L)
                .quarter(Quarter.Q1)
                .authorId(TEST_ADMIN_ID)
                .sprint(1L)
                .release("v1.0.0")
                .summary("User authentication feature")
                .description("Implement user login and registration")
                .build();
    }

    public static Feature createTestFeature(Long id, Long year, Quarter quarter, Long authorId) {
        return Feature.builder()
                .id(id)
                .year(year)
                .quarter(quarter)
                .authorId(authorId)
                .sprint(1L)
                .release("v1.0.0")
                .summary("User authentication feature")
                .description("Implement user login and registration")
                .build();
    }

    // Task test data
    public static Task createTestTask() {
        return Task.builder()
                .id(TEST_TASK_ID)
                .featureId(TEST_FEATURE_ID)
                .authorId(TEST_ADMIN_ID)
                .summary("Implement login endpoint")
                .description("Create REST API endpoint for user login")
                .build();
    }

    public static Task createTestTask(Long id, Long featureId, Long authorId) {
        return Task.builder()
                .id(id)
                .featureId(featureId)
                .authorId(authorId)
                .summary("Implement login endpoint")
                .description("Create REST API endpoint for user login")
                .build();
    }
}

