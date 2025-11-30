# API Testing Guide

Этот документ описывает полный цикл тестирования API для проверки функциональности управления пользователями, ролями, roadmap, feature и task.

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

### 7. Создать roadmap и убедиться, что все хорошо

Получить ID текущего пользователя (admin):

```bash
ADMIN_ID=$(curl -s -X GET http://localhost:8080/api/users -b "X-Auth=$TOKEN" | jq '.[] | select(.email=="admin") | .id')
```

Создать roadmap:

```bash
curl -s -X POST http://localhost:8080/api/roadmaps \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d "{\"projectName\":\"Test Project\",\"authorId\":$ADMIN_ID,\"mission\":\"Build amazing software\",\"description\":\"Test roadmap description\",\"participantIds\":[$ADMIN_ID]}" | jq .
```

Ожидаемый ответ: JSON с полями id, projectName, authorId, mission, description, participantIds.

Проверить список roadmaps:

```bash
curl -s -X GET http://localhost:8080/api/roadmaps -b "X-Auth=$TOKEN" | jq .
```

Сохранить ID созданного roadmap:

```bash
ROADMAP_ID=$(curl -s -X GET http://localhost:8080/api/roadmaps -b "X-Auth=$TOKEN" | jq '.[] | select(.projectName=="Test Project") | .id')
```

### 8. Создать feature и убедиться, что все хорошо

Создать feature:

```bash
curl -s -X POST http://localhost:8080/api/features \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d "{\"year\":2024,\"quarter\":\"Q1\",\"authorId\":$ADMIN_ID,\"sprint\":1,\"release\":\"v1.0.0\",\"summary\":\"User authentication feature\",\"description\":\"Implement user login and registration\"}" | jq .
```

Ожидаемый ответ: JSON с полями id, year, quarter, authorId, sprint, release, summary, description.

Проверить список features:

```bash
curl -s -X GET http://localhost:8080/api/features -b "X-Auth=$TOKEN" | jq .
```

Сохранить ID созданной feature:

```bash
FEATURE_ID=$(curl -s -X GET http://localhost:8080/api/features -b "X-Auth=$TOKEN" | jq '.[] | select(.summary=="User authentication feature") | .id')
```

### 9. Создать task и убедиться, что все хорошо

Создать task:

```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d "{\"featureId\":$FEATURE_ID,\"authorId\":$ADMIN_ID,\"summary\":\"Implement login endpoint\",\"description\":\"Create REST API endpoint for user login\"}" | jq .
```

Ожидаемый ответ: JSON с полями id, featureId, authorId, summary, description.

Проверить список tasks:

```bash
curl -s -X GET http://localhost:8080/api/tasks -b "X-Auth=$TOKEN" | jq .
```

Сохранить ID созданной task:

```bash
TASK_ID=$(curl -s -X GET http://localhost:8080/api/tasks -b "X-Auth=$TOKEN" | jq '.[] | select(.summary=="Implement login endpoint") | .id')
```

### 10. Назначить новую роль на нового пользователя и убедиться, что все хорошо

Получить ID созданной роли и ID пользователя:

```bash
USER_ID=$(curl -s -X GET http://localhost:8080/api/users -b "X-Auth=$TOKEN" | jq '.[] | select(.email=="testuser@example.com") | .id')
ROLE_ID=$(curl -s -X GET http://localhost:8080/api/roles -b "X-Auth=$TOKEN" | jq '.[] | select(.name=="TEST_ROLE") | .id')
```

Назначить роль через обновление пользователя:

```bash
curl -s -X PUT http://localhost:8080/api/users/$USER_ID \
  -H "Content-Type: application/json" \
  -b "X-Auth=$TOKEN" \
  -d "{\"email\":\"testuser@example.com\",\"roleIds\":[$ROLE_ID]}" | jq .
```

Альтернативно, можно назначить роль напрямую в БД:

```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "INSERT INTO user_roles (user_id, role_id) VALUES ($USER_ID, $ROLE_ID) ON CONFLICT DO NOTHING;"
```

Проверить в БД:

```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT user_id, role_id FROM user_roles WHERE user_id = $USER_ID;"
```

### 11. Залогиниться новым пользователем и убедиться, что все хорошо

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

### 12. Удалить созданные объекты и убедиться, что все хорошо

Удалить task:

```bash
curl -s -X DELETE http://localhost:8080/api/tasks/$TASK_ID \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Удалить feature:

```bash
curl -s -X DELETE http://localhost:8080/api/features/$FEATURE_ID \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Удалить roadmap:

```bash
curl -s -X DELETE http://localhost:8080/api/roadmaps/$ROADMAP_ID \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Получить ID созданного пользователя (например, 12) и роли (например, 11):

```bash
USER_ID=$(curl -s -X GET http://localhost:8080/api/users -b "X-Auth=$TOKEN" | jq '.[] | select(.email=="testuser@example.com") | .id')
ROLE_ID=$(curl -s -X GET http://localhost:8080/api/roles -b "X-Auth=$TOKEN" | jq '.[] | select(.name=="TEST_ROLE") | .id')
```

Удалить пользователя:

```bash
curl -s -X DELETE http://localhost:8080/api/users/$USER_ID \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Удалить роль:

```bash
curl -s -X DELETE http://localhost:8080/api/roles/$ROLE_ID \
  -b "X-Auth=$TOKEN" -w "\nHTTP:%{http_code}\n"
```

Ожидаемый код: 204 No Content

Проверить удаление в БД:

```bash
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, email FROM users WHERE id = $USER_ID;"
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, name FROM roles WHERE id = $ROLE_ID;"
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, project_name FROM roadmaps WHERE id = $ROADMAP_ID;"
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, summary FROM features WHERE id = $FEATURE_ID;"
PGPASSWORD=projector psql -h localhost -p 5433 -U projector -d projector \
  -c "SELECT id, summary FROM tasks WHERE id = $TASK_ID;"
```

Ожидаемый результат: пустые результаты (0 rows).

## Примечания

- Все запросы к защищенным эндпоинтам требуют аутентификации через cookie X-Auth
- Поле `password` используется только при создании/обновлении пользователя и никогда не возвращается в ответах
- Поле `passHash` является внутренним и никогда не возвращается клиенту
- При создании пользователя поле `password` обязательно
- При обновлении пользователя: если `password` не пустой - он хешируется и обновляется, иначе используется старый пароль из БД
- При создании roadmap обязательны поля: `projectName`, `authorId`
- При создании feature обязательны поля: `year` (2000-2500), `quarter` (Q1/Q2/Q3/Q4), `authorId`
- При создании task обязательны поля: `featureId`, `authorId`
- Поля `createDate` и `updateDate` устанавливаются автоматически при создании/обновлении объектов
