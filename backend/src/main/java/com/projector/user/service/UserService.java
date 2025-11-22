package com.projector.user.service;

import com.projector.user.model.User;
import com.projector.user.repository.UserRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")));
    }

    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")));
    }

    public Mono<User> createUser(User user) {
        return validateUser(user)
                .flatMap(valid -> userRepository.existsByEmail(user.getEmail()))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ServerWebInputException("User with such email already exists"));
                    }
                    user.setId(null);
                    // TODO: Хеширование пароля должно быть реализовано
                    return userRepository.save(user);
                });
    }

    public Mono<User> updateUser(Long id, User user) {
        return validateUser(user)
                .flatMap(valid -> userRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")))
                .flatMap(existingUser -> {
                    // Проверяем, не используется ли email другим пользователем
                    return userRepository.findByEmail(user.getEmail())
                            .flatMap(emailUser -> {
                                if (!emailUser.getId().equals(id)) {
                                    return Mono.error(new ServerWebInputException("User with such email already exists"));
                                }
                                return Mono.just(true);
                            })
                            .switchIfEmpty(Mono.just(true));
                })
                .flatMap(unused -> {
                    user.setId(id);
                    // Сохраняем существующий passHash, если новый не указан
                    return userRepository.findById(id)
                            .flatMap(existingUser -> {
                                if (user.getPassHash() == null) {
                                    user.setPassHash(existingUser.getPassHash());
                                }
                                return userRepository.save(user);
                            });
                });
    }

    @Transactional
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("User not found")))
                .flatMap(user -> userRepository.deleteById(id)
                        .then());
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
}

