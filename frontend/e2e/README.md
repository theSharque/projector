# E2E Tests

E2E тесты для frontend приложения с использованием Playwright.

## Требования

- **Backend должен быть запущен на `http://localhost:8080`**
  ```bash
  cd backend
  ./gradlew bootRun
  ```
- Frontend dev server будет запущен автоматически Playwright

## Запуск тестов

```bash
# Запустить все тесты
npm run test:e2e

# Запустить тесты в UI режиме
npm run test:e2e:ui

# Запустить тесты в headed режиме (с видимым браузером)
npm run test:e2e:headed
```

## Тесты

### Login тесты
- ✅ Отображение страницы логина
- ✅ Валидация формы
- ✅ Ошибка при неверных credentials
- ⚠️ Успешный логин (требует запущенный backend)
- ✅ Редирект на login для защищенных маршрутов
- ⚠️ Сохранение сессии после перезагрузки (требует запущенный backend)

## Настройка

Конфигурация находится в `playwright.config.ts`.

**Важно**: Убедитесь, что backend запущен перед запуском тестов, иначе тесты логина будут падать с `ERR_CONNECTION_REFUSED`.

