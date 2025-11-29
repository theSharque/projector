# API Testing Guide

Этот документ описывает полный цикл тестирования API для проверки функциональности управления пользователями и ролями.

## Предварительные требования

1. Запущены Docker контейнеры с PostgreSQL и Redis
2. Запущен бэкенд приложения
3. База данных пустая (миграции выполняются автоматически при старте)

## Шаги тестирования

### 1. Удалить тестовую БД и создать заново

```bash
cd /qs/projector
docker compose down -v
```

### 2. Запустить пустую БД

```bash
docker compose up -d postgres redis
```

Проверить статус:
```bash
docker compose ps
```

### 3. Запустить бэкенд и убедиться, что миграция прошла успешно

```bash
cd backend
./gradlew bootRun
```

Проверить здоровье приложения:
```bash
curl -s http://localhost:8080/actuator/health
```

Ожидаемый ответ: `{"status":"UP"}`

### 4. Залогиниться используя API

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin","password":"admin"}' \
  -c /tmp/cookies.txt -D /tmp/headers.txt
```

Проверить наличие Set-Cookie заголовка с токеном X-Auth.

### 5. Создать новую роль и убедиться, что все хорошо

Сначала получить токен:
```bash
TOKEN=$(grep -i "set-cookie" /tmp/headers.txt | grep -o 'X-Auth=[^;]*' | cut -d= -f2)
```

Создать роль:
```bash
curl -s -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d '{"name":"TEST_ROLE","authorities":["USER_VIEW","ROLE_VIEW"]}' | jq .
```

Ожидаемый ответ: JSON с полями id, name, authorities.

Проверить список ролей:
```bash
curl -s -X GET http://localhost:8080/api/roles -b "X-Auth=$TOKEN" | jq .
```

### 6. Создать нового пользователя и убедиться, что все хорошо

Создать пользователя:
```bash
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d '{"email":"testuser@example.com","password":"testpass123"}' | jq .
```

Ожидаемый ответ: JSON с полями id, email (без password и passHash).

Проверить список пользователей:
```bash
curl -s -X GET http://localhost:8080/api/users -b "X-Auth=$TOKEN" | jq .
```

### 7. Назначить новую роль на нового пользователя и убедиться, что все хорошо

Получить ID созданной роли (например, 11) и ID пользователя (например, 12).

Назначить роль через обновление пользователя:
```bash
curl -s -X PUT http://localhost:8080/api/users/12 \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d '{"email":"testuser@example.com","roleIds":[11]}' | jq .
```

Альтернативно, можно назначить роль напрямую в БД:
```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "INSERT INTO user_roles (user_id, role_id) VALUES (12, 11) ON CONFLICT DO NOTHING;"
```

Проверить в БД:
```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT user_id, role_id FROM user_roles WHERE user_id = 12;"
```

### 8. Залогиниться новым пользователем и убедиться, что все хорошо

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@example.com","password":"testpass123"}' \
  -D /tmp/login_headers.txt
```

Проверить Set-Cookie заголовок:
```bash
grep -i "set-cookie" /tmp/login_headers.txt
```

Декодировать JWT токен и проверить, что в authorities присутствуют права из назначенной роли (USER_VIEW, ROLE_VIEW).

### 9. Удалить нового пользователя и роль и убедиться, что все хорошо

Удалить пользователя:
```bash
curl -s -X DELETE http://localhost:8080/api/users/12 \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Удалить роль:
```bash
curl -s -X DELETE http://localhost:8080/api/roles/11 \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Проверить удаление в БД:
```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, email FROM users WHERE id = 12;"
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, name FROM roles WHERE id = 11;"
```

Ожидаемый результат: пустые результаты (0 rows).

## Примечания

- Все запросы к защищенным эндпоинтам требуют аутентификации через cookie X-Auth
- Поле `password` используется только при создании/обновлении пользователя и никогда не возвращается в ответах
- Поле `passHash` является внутренним и никогда не возвращается клиенту
- При создании пользователя поле `password` обязательно
- При обновлении пользователя: если `password` не пустой - он хешируется и обновляется, иначе используется старый пароль из БД

