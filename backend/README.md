# Projector Backend

Backend часть системы управления проектами Projector.

## Технологический стек

- **Framework**: Spring Boot 3.2.0
- **Сборщик**: Gradle 8.5
- **База данных**: PostgreSQL (R2DBC)
- **Кэширование**: Redis (Reactive)
- **Авторизация**: JWT (RS256)
- **Java**: 17

### Используемые библиотеки

#### Spring Boot 3.2.0
- **Описание**: Framework для создания production-ready приложений на основе Spring
- **Документация**: https://docs.spring.io/spring-boot/index.html
- **Основные модули**:
  - `spring-boot-starter-webflux` - реактивный веб-фреймворк
  - `spring-boot-starter-security` - безопасность
  - `spring-boot-starter-data-r2dbc` - реактивный доступ к данным
  - `spring-boot-starter-validation` - валидация
  - `spring-boot-starter-actuator` - мониторинг и метрики

#### Spring Data R2DBC
- **Описание**: Реактивный доступ к реляционным базам данных через R2DBC
- **Документация**: https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/
- **Особенности**:
  - Неблокирующие операции с БД
  - Реактивные репозитории (`ReactiveCrudRepository`)
  - Поддержка транзакций через `ReactiveTransactionManager`
  - Автоматическая настройка через Spring Boot auto-configuration

#### Spring Security WebFlux
- **Описание**: Безопасность для реактивных Spring WebFlux приложений
- **Документация**: https://docs.spring.io/spring-security/reference/6.5/reactive/index.html
- **Возможности**:
  - Реактивная аутентификация и авторизация
  - Настройка через `SecurityWebFilterChain`
  - Интеграция с JWT токенами
  - Метод-безопасность (`@PreAuthorize`)

#### SpringDoc OpenAPI 2.3.0
- **Описание**: Автоматическая генерация документации API на основе OpenAPI 3.0
- **Документация**: https://springdoc.org/
- **Возможности**:
  - Swagger UI для интерактивной документации
  - Поддержка WebFlux
  - Автоматическая генерация из аннотаций
  - Кастомизация через конфигурацию

#### JWT (JJWT 0.12.3)
- **Описание**: Библиотека для работы с JWT токенами
- **Реализация**: RS256 с RSA ключами
- **Хранение**: Cookie с именем `X-Auth`

#### PostgreSQL R2DBC Driver
- **Описание**: Реактивный драйвер для PostgreSQL
- **URL формат**: `r2dbc:postgresql://host:port/database`

#### Redis Reactive
- **Описание**: Реактивный клиент для Redis
- **Использование**: Кэширование и сессии

## Структура проекта

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/projector/
│   │   │   ├── core/           # Основной модуль (JWT, Security)
│   │   │   │   ├── config/     # Конфигурации
│   │   │   │   ├── component/  # Spring компоненты
│   │   │   │   ├── service/    # Бизнес-логика
│   │   │   │   ├── model/      # Модели данных
│   │   │   │   └── exception/  # Исключения
│   │   │   └── ProjectorApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
└── build.gradle
```

## Архитектура и основные концепции

### Реактивная архитектура

Приложение построено на основе **Spring WebFlux** и **R2DBC** для обеспечения неблокирующей работы:

- **WebFlux** - реактивный веб-фреймворк на основе Project Reactor
- **R2DBC** - реактивный доступ к реляционным БД (без блокировок)
- **Reactive Streams** - использование `Mono` и `Flux` для асинхронных операций

### JWT Авторизация

JWT токены генерируются на основе RSA ключей (RS256) и хранятся в cookie клиента с именем `X-Auth`.

#### Реализация

- `JwtSigner` - сервис для генерации и валидации JWT токенов с RSA ключами
- `JwtAuthenticationManager` - менеджер аутентификации для Spring Security (реактивный)
- `JwtServerAuthenticationConverter` - конвертер для извлечения JWT из cookie
- `SecurityConfig` - конфигурация Spring Security для WebFlux

#### Особенности

- Токены подписываются с использованием RS256 алгоритма
- Ключи генерируются при старте приложения (в production должны храниться в конфигурации)
- Cookie с токеном устанавливается через `ResponseCookie`
- Валидация токенов происходит на каждом запросе через Security фильтр

### Spring Data R2DBC

Реактивные репозитории для работы с PostgreSQL:

```java
@Repository
public interface RoleRepository extends R2dbcRepository<Role, Long> {
    Mono<Boolean> existsByName(String name);
    Mono<Role> findByName(String name);
}
```

**Основные типы возврата**:
- `Mono<T>` - для одной сущности или пустого результата
- `Flux<T>` - для коллекций сущностей
- `Mono<Void>` - для операций без возвращаемого значения

**Транзакции**:
- Использование `@Transactional` для реактивных операций
- Автоматическое управление через `R2dbcTransactionManager`

## Запуск

### Требования

- Java 17+
- PostgreSQL 12+
- Redis 6+
- Gradle 8.5+

### Локальный запуск

1. Настройте базу данных PostgreSQL и Redis
2. Обновите `application.yml` с вашими настройками подключения
3. Запустите приложение:

```bash
./gradlew bootRun
```

Или используйте Gradle wrapper:

```bash
./gradlew wrapper
./gradlew bootRun
```

### Переменные окружения

- `DB_USERNAME` - пользователь PostgreSQL (по умолчанию: `projector`)
- `DB_PASSWORD` - пароль PostgreSQL (по умолчанию: `projector`)
- `REDIS_HOST` - хост Redis (по умолчанию: `localhost`)
- `REDIS_PORT` - порт Redis (по умолчанию: `6379`)
- `SERVER_PORT` - порт приложения (по умолчанию: `8080`)
- `TOKEN_MAX_AGE` - время жизни JWT токена в секундах (по умолчанию: `3600`)

## API Документация

После запуска приложения API документация доступна по адресу:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### Доступ к Swagger UI

Swagger UI доступен без аутентификации для удобства разработки. Все endpoints API документированы с использованием аннотаций OpenAPI 3.0.

### Настройка Swagger

Конфигурация находится в:
- `application.yml` - настройки springdoc
- `OpenApiConfig.java` - Java конфигурация с информацией об API

## Модули

### Core Module (Основной модуль)

Содержит базовую функциональность:
- JWT авторизация
- Security конфигурация
- Базовые модели

## Разработка

### Сборка

```bash
./gradlew build
```

### Тестирование

```bash
./gradlew test
```

### Создание JAR

```bash
./gradlew bootJar
```

Готовый JAR будет находиться в `build/libs/projector-0.0.1-SNAPSHOT.jar`

## Полезные ссылки

### Официальная документация

- [Spring Boot 3.2.0 Reference](https://docs.spring.io/spring-boot/docs/3.2.0/reference/html/)
- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/)
- [Spring Security WebFlux Reference](https://docs.spring.io/spring-security/reference/6.5/reactive/index.html)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [Project Reactor Reference Guide](https://projectreactor.io/docs/core/release/reference/)

### Дополнительные ресурсы

- [R2DBC Specification](https://r2dbc.io/)
- [PostgreSQL R2DBC Driver](https://github.com/pgjdbc/r2dbc-postgresql)
- [JJWT Documentation](https://github.com/jwtk/jjwt)

## Best Practices

### Реактивное программирование

1. **Используйте правильные типы**:
   - `Mono<T>` для одного элемента или пустоты
   - `Flux<T>` для потоков данных

2. **Избегайте блокирующих операций**:
   - Не используйте `.block()` в production коде
   - Используйте цепочки операторов (`flatMap`, `map`, `filter`)

3. **Обработка ошибок**:
   - Используйте `.onErrorResume()`, `.onErrorReturn()` для обработки ошибок
   - Логируйте ошибки для отладки

### R2DBC

1. **Настройка пула соединений**:
   ```yaml
   spring.r2dbc.pool:
     initial-size: 5
     max-size: 10
     max-idle-time: 30m
   ```

2. **Транзакции**:
   - Используйте `@Transactional` для группировки операций
   - Все операции в транзакции должны быть реактивными

3. **Кастомные запросы**:
   - Используйте `@Query` для SQL запросов
   - Используйте `@Modifying` для операций изменения данных

