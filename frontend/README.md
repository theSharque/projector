# Projector Frontend

Frontend приложение для системы управления проектами Projector.

## Технологический стек

- **React 19** - UI библиотека
- **TypeScript** - Типизация
- **Vite** - Сборщик
- **Ant Design 5** - UI компоненты
- **React Router** - Роутинг
- **React Query (TanStack Query)** - Управление серверным состоянием
- **Zustand** - Управление клиентским состоянием
- **Axios** - HTTP клиент
- **React Hot Toast** - Уведомления

## Установка

```bash
npm install
```

## Разработка

```bash
npm run dev
```

Приложение будет доступно по адресу `http://localhost:5173`

## Сборка

```bash
npm run build
```

## Структура проекта

```
src/
├── api/              # API клиенты
├── components/       # Переиспользуемые компоненты
│   ├── layout/      # Layout компоненты
│   ├── common/      # Общие компоненты
│   ├── forms/       # Формы
│   └── relations/   # Компоненты для работы со связями
├── features/        # Feature-based структура
│   ├── auth/       # Аутентификация
│   ├── user/       # User модуль
│   ├── role/       # Role модуль
│   ├── roadmap/    # Roadmap модуль
│   ├── feature/    # Feature модуль
│   └── task/       # Task модуль
├── hooks/          # Custom hooks
├── stores/         # Zustand stores
├── types/          # TypeScript типы
└── utils/          # Утилиты
```

## Environment Variables

Создайте файл `.env.local` для локальных настроек:

```
VITE_API_URL=http://localhost:8080
```

## Интеграция с Backend

Frontend настроен на работу с Spring Boot backend:
- API запросы проксируются через Vite proxy к `http://localhost:8080`
- JWT аутентификация через cookies (`X-Auth`)
- CORS настроен на backend для разрешения запросов с frontend

## Основные возможности

- Аутентификация через JWT
- CRUD операции для всех сущностей (User, Role, Roadmap, Feature, Task)
- Управление связями между сущностями:
  - Назначение ролей пользователям
  - Назначение участников roadmap
  - Связь задач с features
- Фильтрация и поиск
- Уведомления об операциях
