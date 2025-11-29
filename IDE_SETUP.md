# Настройка автоматического форматирования при сохранении

## VS Code

Настройки уже добавлены в `.vscode/settings.json`. 

**Рекомендуемые расширения:**
- `vscjava.vscode-java-pack` - Java Extension Pack
- `redhat.java` - Language Support for Java by Red Hat
- `vscjava.vscode-gradle` - Gradle for Java
- `editorconfig.editorconfig` - EditorConfig для VS Code

**Что настроено:**
- ✅ Автоматическое форматирование при сохранении
- ✅ Автоматическое упорядочивание импортов
- ✅ Удаление неиспользуемых импортов
- ✅ 4 пробела для отступов
- ✅ Удаление trailing whitespace
- ✅ Одна пустая строка в конце файла

**Использование:**
1. Установите рекомендованные расширения
2. Откройте проект в VS Code
3. При сохранении файла (`Ctrl+S` / `Cmd+S`) форматирование применится автоматически

## IntelliJ IDEA

### Вариант 1: Использование Spotless через External Tool

1. Откройте **Settings** → **Tools** → **External Tools**
2. Нажмите **+** для добавления нового инструмента:
   - **Name:** `Spotless Apply`
   - **Program:** `$PROJECT_DIR$/backend/gradlew`
   - **Arguments:** `spotlessApply -q`
   - **Working directory:** `$PROJECT_DIR$/backend`

3. Откройте **Settings** → **Tools** → **Actions on Save**
4. Включите:
   - ✅ **Reformat code**
   - ✅ **Run external tool** → выберите `Spotless Apply`
   - ✅ **Optimize imports**

### Вариант 2: Использование встроенного форматтера Google Java Format

1. Установите плагин **Google Java Format** (если еще не установлен):
   - **Settings** → **Plugins** → поиск "Google Java Format"

2. Настройте форматтер:
   - **Settings** → **Editor** → **Code Style** → **Java**
   - **Scheme:** выберите или создайте схему на основе "GoogleStyle"
   - **Tab size:** `4`
   - **Indent:** `4`
   - **Use tab character:** ❌ (не использовать табы)

3. Настройте Actions on Save:
   - **Settings** → **Tools** → **Actions on Save**
   - ✅ **Reformat code**
   - ✅ **Optimize imports**
   - ✅ **Remove unused imports**

### Рекомендуемые настройки Code Style

**Settings** → **Editor** → **Code Style** → **Java**:
- **Tab size:** `4`
- **Indent:** `4`
- **Continuation indent:** `8`
- **Use tab character:** ❌

**Wrapping and Braces:**
- **Keep line breaks:** ✅
- **Keep line breaks in code:** ✅

**Imports:**
- **Class count to use import with '*':** `999` (не использовать wildcard импорты)
- **Names count to use static import with '*':** `999` (не использовать wildcard статические импорты)
- **Import Layout:**
  - `<same package>`
  - `java`
  - `javax`
  - `org`
  - `com`
  - `--- blank line ---`
  - `static <same package>`
  - `static java`
  - `static javax`
  - `static org`
  - `static com`

**Blank Lines:**
- **Keep maximum blank lines:** `1`
- **Before 'package':** `0`
- **After 'package':** `1`
- **After imports:** `1`
- **After class header:** `1`

## Проверка форматирования

Чтобы проверить форматирование всех файлов:

```bash
cd backend
./gradlew spotlessCheck
```

Чтобы автоматически исправить форматирование:

```bash
cd backend
./gradlew spotlessApply
```

Эти команды также выполняются автоматически при сборке проекта (`./gradlew build`).

