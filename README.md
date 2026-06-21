# Jumble — Scramble Word Game

A word-reconstruction game inspired by [Scrabble™](https://en.wikipedia.org/wiki/Scrabble). Given a random 6-letter word with its letters scrambled, the player constructs as many valid sub-words as possible (minimum 3 letters) using only the available letters. The game ends when all possible sub-words have been found.

---

## Repository Structure

```
fourtitude-interviewq-jumble/   ← Spring Boot back-end (Tasks A, B, C)
jumble-client/                  ← Angular 17 front-end (Task D)
```

---

## Task Completion Summary

| Task | Description | Status |
|------|-------------|--------|
| **A** | Core game engine (`JumbleEngine.java`) — all 7 methods | ✅ Complete |
| **B** | Web interface (`RootController`, `GameWebController`, Thymeleaf pages) | ✅ Complete |
| **C** | REST API (`GameApiController`) + full unit tests | ✅ Complete |
| **D** | Angular 17 + Bootstrap 5 standalone web client (`jumble-client/`) | ✅ Complete |

**Total test count:** 63 Java (JUnit 5) + 22 TypeScript (Jasmine/Karma) = **85 tests, all passing**

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 8 or above (tested on JDK 21) |
| Maven | 3.6+ (or use included `mvnw`) |
| Node.js | 18+ (tested on 20.9.0) |
| npm | 9+ |

---

## Task A, B, C — Spring Boot Back-end

### Build & Run

```bash
cd fourtitude-interviewq-jumble

# Run the Spring Boot application
./mvnw spring-boot:run
```

The application starts on **http://localhost:8080**.

### Web Interface (Task B)

| URL | Description |
|-----|-------------|
| http://localhost:8080/ | Home / index page |
| http://localhost:8080/scramble | Scramble a word |
| http://localhost:8080/palindrome | List palindrome words |
| http://localhost:8080/exists | Check if a word exists |
| http://localhost:8080/prefix | Find words by prefix |
| http://localhost:8080/search | Search words by criteria |
| http://localhost:8080/subWords | Generate sub-words |
| http://localhost:8080/game/new | Start a new guessing game |
| http://localhost:8080/game/play | Play the current game |

### REST API (Task C)

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/api/game/new` | Create a new game, returns game state with `id` |
| `POST` | `/api/game/guess` | Submit a guess word (`id` + `word` in JSON body) |

Interactive Swagger UI: **http://localhost:8080/swagger-ui/index.html**

#### Example — New Game

```bash
curl http://localhost:8080/api/game/new
```

```json
{
  "result": "Created new game.",
  "id": "65e0d7a4-59bf-4065-beb1-3c2220d87e1e",
  "original_word": "titans",
  "scramble_word": "nisatt",
  "total_words": 29,
  "remaining_words": 29,
  "guessed_words": []
}
```

#### Example — Submit a Guess

```bash
curl -X POST http://localhost:8080/api/game/guess \
  -H "Content-Type: application/json" \
  -d '{"id": "65e0d7a4-59bf-4065-beb1-3c2220d87e1e", "word": "tins"}'
```

```json
{
  "result": "Guessed correctly.",
  "id": "65e0d7a4-59bf-4065-beb1-3c2220d87e1e",
  "original_word": "titans",
  "scramble_word": "snitat",
  "guess_word": "tins",
  "total_words": 29,
  "remaining_words": 28,
  "guessed_words": ["tins"]
}
```

### Run Unit Tests

```bash
cd fourtitude-interviewq-jumble
./mvnw test
```

Expected output:
```
Tests run: 63, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests | Covers |
|------------|-------|--------|
| `JumbleEngineTest` | 19 | All 7 engine methods |
| `RootControllerTest` | 30 | All 5 web form handlers |
| `GameWebControllerTest` | 6 | Web game flow |
| `GameApiControllerTest` | 7 | REST API endpoints |
| `JumbleApplicationTests` | 1 | Application context loads |

---

## Task D — Angular 17 Web Client

### Build & Run

```bash
cd jumble-client

# Install dependencies
npm install

# Start the dev server (proxies /api to http://localhost:8080)
npm start
```

The Angular app starts on **http://localhost:4200**.

> **Note:** The Spring Boot back-end must be running on port 8080 for the Angular client to function. The dev server is pre-configured with a proxy (`proxy.conf.json`) so no CORS configuration is required during development.

### Production Build

```bash
cd jumble-client
npm run build
# Output: dist/jumble-client/
```

### Run Unit Tests

```bash
cd jumble-client
npm test
```

Expected output: **22 specs, 0 failures**

| Test File | Tests | Covers |
|-----------|-------|--------|
| `game.service.spec.ts` | 4 | `GameService` HTTP calls (new game, correct/incorrect/all guessed) |
| `game-board.component.spec.ts` | 15 | Component state, game logic, form validation, error handling |
| `app.component.spec.ts` | 3 | App shell renders navbar and game-board |

### Project Structure

```
jumble-client/src/app/
├── game.model.ts                        # TypeScript interfaces (GameGuessInput, GameGuessOutput)
├── game.service.ts                      # HTTP service wrapping the REST API
├── game.service.spec.ts                 # Service unit tests
├── game-board/
│   ├── game-board.component.ts          # Game logic and state management
│   ├── game-board.component.html        # Bootstrap 5 template
│   ├── game-board.component.scss        # Component styles
│   └── game-board.component.spec.ts     # Component unit tests
├── app.component.ts                     # Root standalone component
├── app.component.html                   # Navbar + game-board outlet
├── app.component.spec.ts                # App-level tests
└── app.config.ts                        # provideHttpClient() provider
```

---

## Implementation Notes

### Task A — JumbleEngine

- Words are loaded once at startup into a `HashSet<String>` (O(1) lookup for `exists`) and a `Map<Integer, List<String>>` keyed by word length (O(1) random word by length).
- `generateSubWords` uses a character frequency map comparison, running in O(W × L) where W = dictionary size, L = word length — no recursive permutation generation.
- `scramble` shuffles characters and retries up to 1000 times to guarantee the output differs from the input.

### Task B — Web Controllers

- Bean Validation (`@Valid`, `@NotBlank`, `@Size`) guards all form inputs.
- The search form uses custom `bindingResult.rejectValue()` calls when all three criteria are empty, since `@Size(min=0, max=1)` alone permits empty strings.
- `GameWebController` uses `@SessionAttributes("board")` to persist game state across requests.

### Task C — REST API

- Game states are stored in a `ConcurrentHashMap<String, GameGuessModel>` keyed by `UUID`.
- The `playGame` endpoint validates the `id` field format before lookup — null or non-UUID strings return **404 "Invalid Game ID."**, a valid UUID absent from the map returns **404 "Game board/state not found."**, and a null `word` is treated as an incorrect guess (HTTP 200) rather than a validation error.
- `@CrossOrigin` is applied to the controller to allow Angular dev-server requests.

### Task D — Angular Client

- Fully standalone components — no `NgModule`.
- `GameService` accepts a `GameGuessInput` object so both `id` and `word` fields travel in a single typed call.
- Client-side validation on the guess input: required, min 3 characters, max 30 characters, letters only (`[a-zA-Z]+`).
- The dev-server proxy (`proxy.conf.json`) forwards all `/api/*` requests to `http://localhost:8080`, eliminating CORS friction during development.

---

## Console Application (Task A reference)

A console-based interface is also available for manual testing of the game engine:

```bash
cd fourtitude-interviewq-jumble
./mvnw exec:java
```

---

## Technology Stack

| Layer | Technology |
|-------|------------|
| Language | Java 8 (source/target), runs on JDK 8+ |
| Framework | Spring Boot 2.7.18 |
| Templating | Thymeleaf 3 with Layout Dialect |
| Validation | javax.validation (Hibernate Validator) |
| API Docs | Springdoc OpenAPI 1.7 (Swagger UI) |
| Testing (Java) | JUnit 5, Spring MockMvc |
| Front-end | Angular 17 (standalone components) |
| CSS | Bootstrap 5.3 |
| Testing (TS) | Jasmine + Karma |
| Build (Java) | Maven 3 |
| Build (TS) | Angular CLI 17 |
