package com.projector.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.user.model.User;
import com.projector.user.model.UserRole;
import com.projector.user.repository.UserRepository;
import com.projector.user.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Long id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")));
    }

    public Mono<User> getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")));
    }

    @Transactional
    public Mono<User> createUser(User user) {
        return validateUser(user)
                .flatMap(valid -> userRepository.existsByEmail(user.getEmail()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ServerWebInputException(
                                "User with such email already exists"));
                    }
                    user.setId(null);
                    return userRepository.save(user);
                })
                .flatMap(savedUser -> {
                    if (user.getRoleIds() != null && !user.getRoleIds().isEmpty()) {
                        return assignRolesToUser(savedUser.getId(), user.getRoleIds())
                                .thenReturn(savedUser);
                    }
                    return Mono.just(savedUser);
                });
    }

    @Transactional
    public Mono<User> updateUser(Long id, User user) {
        return validateUser(user)
                .flatMap(valid -> userRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")))
                .flatMap(existingUser -> {
                    return userRepository
                            .findByEmail(user.getEmail())
                            .flatMap(
                                    emailUser -> {
                                        if (!emailUser.getId().equals(id)) {
                                            return Mono.error(
                                                    new ServerWebInputException(
                                                            "User with such email already exists"));
                                        }

                                        return Mono.just(true);
                                    })
                            .switchIfEmpty(Mono.just(true));
                })
                .flatMap(unused -> {
                    user.setId(id);
                    return userRepository
                            .findById(id)
                            .flatMap(
                                    existingUser -> {
                                        if (user.getPassHash() == null) {
                                            user.setPassHash(existingUser.getPassHash());
                                        }

                                        return userRepository.save(user);
                                    })
                            .flatMap(updatedUser -> {
                                if (user.getRoleIds() != null) {
                                    return deleteUserRoles(id)
                                            .then(assignRolesToUser(id, user.getRoleIds()))
                                            .thenReturn(updatedUser);
                                }
                                return Mono.just(updatedUser);
                            });
                });
    }

    @Transactional
    public Mono<Void> deleteUser(Long id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")))
                .flatMap(user -> userRepository.deleteById(id).then());
    }

    public Mono<User> getUser(String email, String password) {
        String login = email.toLowerCase();
        return userRepository
                .findByEmail(login)
                .flatMap(user -> {
                    String passwordHash = sha256Hash(password);
                    if (user.getPassHash().equals(passwordHash)) {
                        return Mono.just(user);
                    } else {
                        return Mono.error(new UsernameNotFoundException(
                                "Invalid username or password"));
                    }
                })
                .switchIfEmpty(
                        Mono.error(new UsernameNotFoundException("Invalid username or password")));
    }

    private String sha256Hash(String password) {
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

    private Mono<Boolean> validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return Mono.error(new ServerWebInputException("User email cannot be empty"));
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            return Mono.error(new ServerWebInputException("Invalid email format"));
        }

        return Mono.just(true);
    }

    private Mono<Void> assignRolesToUser(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(roleIds)
                .map(roleId -> UserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build())
                .flatMap(userRoleRepository::save)
                .then();
    }

    private Mono<Void> deleteUserRoles(Long userId) {
        return userRoleRepository.deleteByUserId(userId).then();
    }
}
