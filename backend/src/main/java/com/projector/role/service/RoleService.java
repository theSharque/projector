package com.projector.role.service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.role.model.Authority;
import com.projector.role.model.Role;
import com.projector.role.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private static final Pattern VALIDATION_PATTERN = Pattern.compile("^(?:\\p{L}|[_-]|\\d|\\s(?!\\s))+$");

    private final RoleRepository roleRepository;

    public Flux<Role> getAllRoles() {
        return roleRepository.findAll().map(this::loadAuthoritiesFromString);
    }

    public Mono<Role> getRoleById(Long id) {
        return roleRepository
                .findById(id)
                .map(this::loadAuthoritiesFromString)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Role not found")));
    }

    public Mono<Role> createRole(Role role) {
        return validateRole(role)
                .flatMap(valid -> roleRepository.existsByName(role.getName()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(
                                new ServerWebInputException(
                                        "Role with such name already exists"));
                    }
                    validateAuthorities(role.getAuthorities());
                    role.setId(null);
                    Role roleToSave = prepareRoleForSave(role);
                    return roleRepository
                            .save(roleToSave)
                            .map(this::loadAuthoritiesFromString);
                });
    }

    public Mono<Role> updateRole(Long id, Role role) {
        return validateRole(role)
                .flatMap(valid -> roleRepository.existsById(id))
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ServerWebInputException("Role not found"));
                    }
                    return roleRepository
                            .findByName(role.getName())
                            .flatMap(
                                    existingRole -> {
                                        if (!existingRole.getId().equals(id)) {
                                            return Mono.error(
                                                    new ServerWebInputException(
                                                            "Role with such name already exists"));
                                        }
                                        return Mono.just(true);
                                    })
                            .switchIfEmpty(Mono.just(true));
                })
                .flatMap(unused -> {
                    validateAuthorities(role.getAuthorities());
                    role.setId(id);
                    Role roleToSave = prepareRoleForSave(role);
                    return roleRepository
                            .save(roleToSave)
                            .map(this::loadAuthoritiesFromString);
                });
    }

    @Transactional
    public Mono<Void> deleteRole(Long id) {
        return roleRepository
                .existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ServerWebInputException("Role not found"));
                    }
                    return roleRepository.deleteCascadeById(id).then();
                });
    }

    public Mono<Role> updateAuthorities(Long id, Set<String> authorities) {
        return roleRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Role not found")))
                .flatMap(role -> {
                    validateAuthorities(authorities);
                    role.setAuthorities(authorities);
                    Role roleToSave = prepareRoleForSave(role);
                    return roleRepository
                            .save(roleToSave)
                            .map(this::loadAuthoritiesFromString);
                });
    }

    private Mono<Boolean> validateRole(Role role) {
        if (role.getName() == null || role.getName().isBlank()) {
            return Mono.error(new ServerWebInputException("Role name cannot be empty"));
        }

        if (role.getName().length() < 2) {
            return Mono.error(
                    new ServerWebInputException("Role name must be at least 2 characters"));
        }

        Matcher matcher = VALIDATION_PATTERN.matcher(role.getName());
        if (!matcher.matches()) {
            return Mono.error(new ServerWebInputException("Invalid role name format"));
        }

        return Mono.just(true);
    }

    private void validateAuthorities(Set<String> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return;
        }

        Set<String> validAuthorityNames = Set.of(
                Authority.USER_VIEW.getName(),
                Authority.USER_EDIT.getName(),
                Authority.ROLE_VIEW.getName(),
                Authority.ROLE_EDIT.getName());

        Set<String> invalidAuthorities = authorities.stream()
                .filter(auth -> !validAuthorityNames.contains(auth))
                .collect(Collectors.toSet());

        if (!invalidAuthorities.isEmpty()) {
            throw new ServerWebInputException(
                    "Invalid authorities: " + String.join(", ", invalidAuthorities));
        }
    }

    private Role prepareRoleForSave(Role role) {
        Role roleToSave = Role.builder().id(role.getId()).name(role.getName()).build();
        roleToSave.setAuthorities(
                role.getAuthorities() != null
                        ? new HashSet<>(role.getAuthorities())
                        : new HashSet<>());
        return roleToSave;
    }

    private Role loadAuthoritiesFromString(Role role) {
        if (role.getAuthoritiesString() != null) {
            role.getAuthorities();
        }

        return role;
    }
}
