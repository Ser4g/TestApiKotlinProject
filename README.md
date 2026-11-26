# TestApiKotlinProject

Автотесты API для сервиса пользователей (`/user/create`, `/user/get`) на Kotlin.

Чек-лист проверок: [CheckList.md](CheckList.md)

## Требования

| Компонент | Версия | Проверка установки |
|---|---|---|
| JDK | 21 | `java -version` |
| Maven | 3.8+ | `mvn -version` |


## Стек

- Kotlin 2.0.21, Maven
- Java 21
- RestAssured 5.5.0 — HTTP-клиент
- JUnit 5 — тестовый фреймворк
- AssertJ — ассерты
- Jackson (jackson-module-kotlin) — десериализация JSON-ответов в data-классы

## Запуск тестов

```bash
mvn test
```

По умолчанию тесты идут на `http://localhost:3333`. Адрес переопределяется без правки кода:

```bash
mvn test -DbaseUri=http://<хост> -Dport=<порт>
```

Запуск одного класса или теста:

```bash
mvn test -Dtest=UserCreateTest
mvn test -Dtest=UserCreateNegativeTest#missingUsername
```

## Структура проекта

```
src/test/kotlin/org/example/
├── client/UserApiClient.kt          — обёртка над RestAssured (createUser, getUsers)
├── model/
│   ├── CreateUserResponse.kt        — DTO ответа /user/create
│   ├── StringOrListDeserializer.kt  — десериализация message (строка или массив)
│   └── UserDto.kt                   — DTO элемента массива /user/get
├── utils/TestHelper.kt              — вспомогательные методы: генерация уникальных username/email
└── tests/
    ├── UserCreateTest.kt            — чек-лист №1-2 (позитивные create)
    ├── UserCreateNegativeTest.kt    — чек-лист №3-8 (нет полей, пустые значения, дубликаты)
    ├── UserGetTest.kt               — чек-лист №9-11 (массив, структура, хеш)
    ├── UserFlowTest.kt              — чек-лист №12-14 (сквозные create → get)
    └── UserCreateSecurityTest.kt    — чек-лист №1 из пункта 2.1 (SQL-инъекция)
```

Каждый тест в `@DisplayName` содержит номер проверки из чек-листа.

## Результаты тестов

1. **Консоль** — сводка Maven по каждому классу и итоговая статистика.
2. **`target/surefire-reports/`** — отчёты surefire:
   - `*.txt` — текстовый отчёт по классу (пройдено/упало, время);
   - `*.xml` — машиночитаемый формат для CI (GitLab CI, Jenkins).
   Каталог пересоздаётся при каждом прогоне.

В падениях тестов видно: номер проверки из чек-листа (в названии теста) и описание несоответствия (в сообщении ассерта, включая фактический статус-код и тело ответа).

## Особенности

- Тесты создают пользователей с уникальными `username`/`email` — повторный прогон не конфликтует с предыдущим. Эндпоинта удаления пользователей нет, данные в БД накапливаются.
