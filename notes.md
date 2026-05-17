# Backend Concepts

## Three-Layer Architecture

| Layer | Role | Rule |
|---|---|---|
| Controller (API) | Parse HTTP request, return HTTP response | No business logic |
| Service (Facade) | Business logic, validation, rules, orchestration | No SQL, no HTTP |
| Repository (DAO) | DB access only | No business logic |

**Data flow:** `Controller → Service → Repository → DB → Repository → Service → Controller`

---

## HTTP Status Codes

| Code | Meaning | When |
|---|---|---|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Client sent invalid data |
| 404 | Not Found | Resource doesn't exist |

---

## SQL Syntax Reminders

```sql
SELECT * FROM notes
SELECT * FROM notes WHERE type = ?
INSERT INTO notes (type, title) VALUES (?, ?)
UPDATE notes SET type = ?, title = ? WHERE id = ?
DELETE FROM notes WHERE id = ?
```

- `?` = placeholder — JDBC replaces it with the actual value (prevents SQL injection)
- Never concatenate user input into SQL strings

---

## Optional

Wrapper that forces the caller to handle the "not found" case.

```java
Optional<Note> note = noteRepository.findById(id);
note.isPresent()   // true if found
note.isEmpty()     // true if not found
note.get()         // unwrap — only call after checking isPresent()
```

Use `Optional<T>` when a single lookup might return nothing (findById).
Use `List<T>` when the result can just be empty (findAll, findByType).

---

## JdbcTemplate

| Method | Returns | Use when |
|---|---|---|
| `jdbc.query(sql, rowMapper, args...)` | `List<T>` | 0 or more rows |
| `jdbc.queryForObject(sql, Type.class, args...)` | `T` | exactly 1 value (e.g. generated id) |
| `jdbc.update(sql, args...)` | `int` (rows affected) | INSERT, UPDATE, DELETE |

---

## Exception Handling (M3)

- `GlobalExceptionHandler` formats error responses — maps exceptions to JSON + status code
- `@ControllerAdvice` registers it with Spring as a global interceptor — never called directly
- Controller just `throw`s — Spring scans `@ControllerAdvice` classes and calls the matching `@ExceptionHandler`
- Custom exception extends `RuntimeException` (unchecked) — no need to declare `throws` in the call chain
- `super("message")` calls the parent constructor, which sets `detailMessage` variable in `Throwable`
- `e.getMessage()` reads that variable — returns whatever string was passed to `super()`

### Flow
```
Controller throws NoteNotFoundException(id)
    → Spring intercepts it
    → scans @ControllerAdvice classes only
    → finds @ExceptionHandler(NoteNotFoundException.class)
    → calls handleNotFound(e)
    → e.getMessage() → "Note not found: 99"
    → returns 404 { "error": "Note not found: 99" }
```

---

## Logging (M6 — SLF4J)

- 5 levels: `trace` → `debug` → `info` → `warn` → `error`
- `log.info()` — normal operations; `log.warn()` — unexpected but recoverable; `log.error()` — something failed
- Use `{}` placeholder instead of string concatenation — skips building the string if the level is filtered out
  ```java
  log.info("fetching notes with type: {}", type);  // ✓
  log.info("fetching notes with type: " + type);   // ❌
  ```
- Set `logging.level.com.japanesenote=DEBUG` to show debug logs for your code only — avoid `logging.level.root=DEBUG` (too noisy)
- Log in the service layer — business logic happens there; repository SQL is already available via Spring's built-in logging

---

## Database Migrations (M5 — Flyway)

- Flyway is like "Git for database schema" — tracks and replays schema changes in order
- Flyway runs SQL files automatically on startup in order, tracks them in `flyway_schema_history`
- Already-applied migrations are skipped — safe to restart the app repeatedly
- Naming convention: `V{number}__{description}.sql` (double underscore required)
- Flyway reads DB connection from `application.properties` automatically — no extra config needed
- Each `V{num}__xxx.sql` is like a git commit — never edit once applied, always add a new file instead
- Flyway checksums each file on first run — editing an applied migration causes a checksum mismatch error on next startup
- Use `BIGSERIAL` (64-bit) not `SERIAL` (32-bit) for `id` — matches Java's `Long` and avoids Hibernate validation errors
- Indexes speed up `WHERE` and `ORDER BY` queries by avoiding full table scans
- Indexes slow down `INSERT`/`UPDATE`/`DELETE` slightly — only index frequently queried columns

---

## Input Validation (M4)

- `@NotBlank` — fails if field is null, empty, or whitespace
- `@Pattern(regexp = "...")` — fails if field doesn't match the regex
- `@Valid` on controller parameter — triggers Spring to validate the object before the method runs. Without it, all validation annotations are ignored.
- When validation fails, Spring throws `MethodArgumentNotValidException` before the controller method runs
- `e.getMessage()` — one giant unformatted string, not suitable for clients
- `e.getBindingResult().getFieldErrors()` — structured list, one entry per failed field with field name + message separately
- Use `HashMap` in the validation handler (multiple errors, built dynamically) vs `Map.of()` for single known errors

---

## Mutable vs Immutable

- **Immutable** — cannot be changed after creation. `Map.of()`, `List.of()` are immutable — calling `.put()` or `.add()` throws an exception.
- **Mutable** — can be changed after creation. `HashMap`, `ArrayList` let you add/remove freely.

```java
Map.of("error", "msg")       // immutable — fixed, use when content is known upfront
new HashMap<>()              // mutable — use when building content dynamically (e.g. in a loop)
```

---

## Authentication (M7 — JWT)

### JWT Basics
- JWT has 3 parts: `header.payload.signature`
  - **Payload** — carries user info (e.g. username). `extractUsername()` reads from here
  - **Signature** — `HMAC(header + payload, secretKey)` — proves nobody tampered with it
- If payload is tampered → recomputed signature won't match input signature → rejected
- **Stateless** — server stores nothing; every request proves identity via the token
- **Session (stateful)** — server stores session in memory + sends cookie; JWT doesn't need this

### Login Flow
```
POST /api/auth/login
  → AuthController (receives username + password, calls service)
  → UserService.login() (finds user by username, verifies password, generates token)
  → passwordEncoder.matches() (compares plain password with stored hash)
  → jwtUtil.generateToken() (creates JWT with username as subject)
  → returns { "token": "..." }
```

### Authenticated Request Flow (e.g. GET /api/notes)
```
Request with "Authorization: Bearer <token>"
  → JwtAuthFilter (Guard 1: reads token, validates, writes username to SecurityContextHolder)
  → UsernamePasswordAuthenticationFilter (Guard 2: for HTML form login — does nothing in our app)
  → Authorization filter (Guard 3: checks SecurityContextHolder — name written? allow : 403)
  → NoteController (reads username from SecurityContextHolder, passes to service)
  → NoteService (looks up userId from username, passes to repository)
  → NoteRepository (executes SQL with WHERE user_id = ?)
  → PostgreSQL
```

### SecurityContextHolder
- A shared whiteboard for the current request — stores who is authenticated
- `JwtAuthFilter` writes to it; the authorization filter reads it
- If nothing is written → request gets 403
- Any filter can write to it — it's not exclusive to custom filters

### JwtAuthFilter
- Extends `OncePerRequestFilter` — guarantees the filter runs exactly once per request (Spring may forward requests internally, causing filters to run multiple times)
- Must call `filterChain.doFilter(request, response)` on every path (including early returns) — without it, the request gets stuck and never reaches the next filter or controller
- `Bearer` prefix — defined in RFC 6750 (OAuth 2.0); tells the server which auth type is being used (could be `Basic`, `Bearer`, `Digest`, etc.)
    - Bear means whoever bears/carries it is granted access

### BCrypt
- **Non-deterministic** — `encode("1234")` produces a different hash every time because of a random **salt**
- `encode("1234")` → `"$2a$10$abc..."` first time, `"$2a$10$xyz..."` second time
- Never compare hashes with `.equals()` — always use `passwordEncoder.matches(plainPassword, storedHash)`
- `matches()` extracts the salt from the stored hash and uses it to verify correctly
- **Non-deterministic ≠ non-idempotent**: non-deterministic = same input → different output; non-idempotent = doing something multiple times produces different effects (e.g. POST creates duplicates)

### SecurityConfig
- `@Configuration` — tells Spring this class contains setup/config
- `@Bean` — registers the method's return value in Spring's warehouse for injection elsewhere
- `@Bean` vs `@Component`/`@Service`/`@Repository`:
  - `@Component`/`@Service`/`@Repository` → put on a class **you own** — Spring creates it automatically
  - `@Bean` → put on a method in `@Configuration` — for classes **you don't own** (e.g. `BCryptPasswordEncoder` from a library)
- `.csrf(csrf -> csrf.disable())` — CSRF attacks exploit cookies (browser sends them automatically); JWT uses `Authorization` header (must be added manually by code) → no CSRF risk
- `.sessionManagement(STATELESS)` — tells Spring not to create HTTP sessions; without it, Spring could create a session after first auth, letting users skip the token on subsequent requests
- `.requestMatchers("/api/auth/**").permitAll()` — `**` = wildcard for anything after; register/login don't require a token
- `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` — run our filter before Spring's built-in one, so SecurityContextHolder is populated in time

### CSRF (Cross-Site Request Forgery)
- Malicious website tricks your browser into sending a request to a site you're logged into
- Works because browsers send cookies automatically — the target server thinks it's you
- Only exploits auth that the browser sends automatically: session cookies, Basic auth
- Does NOT work against JWT in `Authorization` header — browsers never send custom headers automatically on cross-site requests

### Foreign Key & User Scoping
- foreign key: a column points to rows in another table -> (more precise) points to primary key in another table
  - since primary key id is unique, it's equal to pointing to rows in another table
- `user_id BIGINT REFERENCES users(id)` — points to a row in the `users` table
- Prevents **orphan data** — a note pointing to a user that doesn't exist
- All note queries must include `WHERE user_id = ?` — users can only see/modify their own notes
- `user_id` must come from the JWT token (server-side), never from the client request body — client can fake it

### Singleton & Race Condition
- Spring beans are **singletons** — one instance shared by all users
- Never store per-user state (like `userId`) as a class field in a service — if User A sets it, User B overwrites it
- Pass per-user data as method parameters instead

### BIGSERIAL vs BIGINT
- `BIGSERIAL` = auto-increment — for primary keys (`users.id`), PostgreSQL generates 1, 2, 3...
- `BIGINT` = just a number — for foreign keys (`notes.user_id`), you set the value yourself
- `BIGSERIAL` skips ids on failed inserts — by design, not a bug

---

## ResponseEntity

```java
ResponseEntity.ok(body)               // 200 + body
ResponseEntity.status(201).body(obj)  // 201 + body
ResponseEntity.noContent().build()    // 204 no body
ResponseEntity.notFound().build()     // 404 no body
ResponseEntity.badRequest().body(msg) // 400 + body
```

---

## Pagination, Search & Filtering (M8)

### Dynamic SQL
- Build SQL conditionally at runtime: `if (type != null) sql += " AND type = ?"`
- One method handles all filter combinations — avoids writing separate methods for every permutation
- Use `List<Object> params` to collect parameters dynamically, then pass `params.toArray()` to JDBC
- `jdbc.query()` accepts `Object...` (varargs = `Object[]` under the hood), not `List<Object>` — hence `.toArray()`

### Pagination
- `LIMIT` = how many rows to return; `OFFSET` = how many rows to skip
- `offset = (curPage - 1) * pageSize` — page 1 skips 0, page 2 skips `pageSize`, etc.
- Enforce defaults (`curPage=1`, `pageSize=20`) in the controller via `@RequestParam(defaultValue = "...")`
- Enforce max limit (`pageSize > 100 → 100`) in the service — business rules belong in the service layer
- `@RequestParam(defaultValue = "1")` — Spring uses this value when the client doesn't send the param; `@RequestParam(required = false)` — value is `null` when missing

### Count Query
- `findAll` with `LIMIT`/`OFFSET` only returns one page — it doesn't know the total
- A separate `SELECT COUNT(*)` query returns the total matching rows across all pages
- `numOfPages = (int) Math.ceil((double) total / pageSize)` — cast to `double` first, otherwise integer division truncates (e.g. `43/20 = 2` instead of `3`)

### Search with ILIKE
- `ILIKE` = case-insensitive pattern match (PostgreSQL-specific); `LIKE` = case-sensitive
- `title ILIKE ?` with param `"%keyword%"` — `%` matches any characters before/after the keyword

### PagedResponse
- Wrapper class returning `{ data, total, curPage, numOfPages }` — gives the client everything needed to render pagination
- Immutable: constructor only, no setters — set all values once at creation

---

## Testing (M9 — JUnit 5 + Mockito + MockMvc)

### Test in Isolation
- Test one layer at a time — mock the layer below, never go deeper
- `ControllerTest`: real Controller + fake Service
- `ServiceTest`: real Service + fake Repository
- If a service test fails → bug is in the service, not the repository

### Arrange → Act → Assert
- **Arrange** — create fake data, tell mocks what to return (`when().thenReturn()`)
- **Act** — call the real method under test
- **Assert** — check the result (`assertEquals`) or check internal calls (`verify`)
- `when().thenReturn()` prepares answers for methods the code will call internally — without it, mocks return null and the code crashes

### @Mock vs @MockBean
- `@Mock` — plain Mockito fake, no Spring. Used with `@ExtendWith(MockitoExtension.class)`. Fast.
- `@MockBean` — fake injected into Spring context, replacing the real bean. Used with `@WebMvcTest`.
- `@InjectMocks` — creates the real class under test, injects `@Mock` fakes into it (Mockito wiring)
- In `@WebMvcTest`, Spring creates the real controller and injects `@MockBean` fakes — no `@InjectMocks` needed

| Test | Who creates the real object? | Who injects fakes? |
|---|---|---|
| ServiceTest | `@InjectMocks` (Mockito) | `@Mock` (Mockito) |
| ControllerTest | `@WebMvcTest` (Spring) | `@MockBean` (Spring) |

### assertEquals vs verify
- `assertEquals(expected, actual)` — checks the **output** (return value)
- `verify(mock).method(args)` — checks **internal behavior** (was this method called with these args?)

### MockMvc & @WebMvcTest
- `@WebMvcTest(XController.class)` — loads only the controller layer (no DB, no services). Fast.
- `@SpringBootTest` — loads the entire app including DB. Slow.
- `MockMvc` — sends fake HTTP requests without starting a real server
- `mockMvc.perform()` declares `throws Exception` — Java's checked exception rule forces callers to propagate it
- `jsonPath("$.data[0].title")` — `$` = JSON root, navigate into fields/arrays to check values

### Mockito Matchers
- `any(Note.class)` — match any Note argument (needed when Spring creates a new object from JSON, different from your mockNote)
- `eq("gary")` — matcher version of a plain value
- **Rule**: once you use any matcher (`any()`) for one argument, ALL arguments must use matchers — can't mix `any()` with plain `"gary"`, must use `eq("gary")`

### Security in Tests
- `@WithMockUser(username = "gary")` — fakes a logged-in user so requests pass Spring Security
- `.with(csrf())` — adds a fake CSRF token to POST/PUT/DELETE requests; Spring's test framework re-enables CSRF by default
- CSRF protects **state-changing** requests (POST/DELETE), not requests that "carry data" — GET with query params is safe because it changes nothing
- `@WebMvcTest` doesn't load `@Configuration` classes — use `@Import(SecurityConfig.class)` when tests need custom security rules (e.g. `permitAll` for auth endpoints)

---

## Docker (M11)

### Core Concepts

- **Image** — a read-only blueprint (like a class)(recipe). Built from a Dockerfile. Portable — share it and anyone can run your app.
- **Container** — a running instance of an image (like an object)(food made from recipe). Has its own isolated filesystem, network, and processes.
- **One service in docker-compose.yml = one image = one container.** 3 services → 3 images built/pulled → 3 containers running.
- **Dockerfile** — a text file with step-by-step instructions to build an image (`FROM`, `COPY`, `RUN`, etc.). Each instruction creates a **layer** — Docker caches layers, so if you only change your code but not dependencies, it skips the slow `RUN npm ci` / `RUN mvn package` step on rebuild.
- **docker-compose** — tool to run multiple containers together (e.g. app + database) with one command.
- **`localhost`** = "myself, this machine." Your Mac's localhost is your Mac. Inside a container, `localhost` means that container itself — not your Mac, not other containers.

| Concept | What it is | Analogy |
|---|---|---|
| Image | A snapshot of your app + its environment | A recipe |
| Container | A running instance of an image | A dish made from the recipe |
| Dockerfile | Instructions to build the image | The recipe steps |
| docker-compose | Run multiple containers together | A menu that says "make all 3 dishes" |

```
┌─────────────┐  ┌──────────────┐  ┌─────────────┐
│   Frontend  │  │   Backend    │  │  PostgreSQL │
│   (React)   │  │ (Spring Boot)│  │  (Database) │
│   port 3000 │  │  port 5001   │  │  port 5432  │
└─────────────┘  └──────────────┘  └─────────────┘
  └──── docker-compose.yml manages all 3 ────┘
```

### Dockerfile Instructions

| Instruction | Purpose | Example |
|---|---|---|
| `FROM` | Base image to start from | `FROM maven:3.9-eclipse-temurin-21` |
| `WORKDIR` | Set working directory inside the container | `WORKDIR /app` |
| `COPY` | Copy files from host into the image | `COPY . .` |
| `RUN` | Execute a command during build (install deps, compile) | `RUN mvn package -DskipTests` |
| `CMD` | Default command when the container starts | `CMD ["java", "-jar", "app.jar"]` |

### Multi-Stage Build
- Stage 1 (build): uses a big image with build tools (JDK + Maven) to compile the code
- Stage 2 (run): uses a small image with just the runtime (JRE) to run the .jar
- Only the final stage becomes the image — build tools are discarded, making the image much smaller

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build   # Stage 1: compile
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre                   # Stage 2: run
COPY --from=build /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

### docker-compose.yml Keys

| Key | Purpose | Example |
|---|---|---|
| `services` | Define each container | `db:`, `backend:`, `frontend:` |
| `image` | Use a pre-built image | `image: postgres:16` |
| `build` | Build image from a Dockerfile | `build: ./backend-java` |
| `ports` | Door from your Mac into the container. Left = Mac port, right = container port. Only needed for **you** (browser, Postman) to reach in — containers talk internally without it | `"3000:80"` |
| `environment` | Set env variables inside the container | `POSTGRES_USER: gary` |
| `volumes` | Persist data outside the container | `pgdata:/var/lib/postgresql/data` |
| `depends_on` | Start this service after another | `depends_on: [db]` |

### Key Terms
- **JDK** (Java Development Kit) — compiler + tools + JRE. Needed to *build* Java code.
- **JRE** (Java Runtime Environment) — just the runtime. Needed to *run* .jar files.
- **.jar** (Java ARchive) — a zip of compiled `.class` files. is your entire app packed into one file. `java -jar app.jar` runs it.
- **Volume** — persistent storage outside the container. Without it, data is lost when the container is removed. `down` keeps volumes, `down -v` deletes them.

### Common Commands

| Command | What it does |
|---|---|
| `docker compose up --build` | Build images + start all services |
| `docker compose down` | Stop + remove all containers |
| `docker compose down -v` | Stop + remove containers + delete volumes (data!) |
| `docker compose logs backend` | View logs for one service |
| `docker ps` | List running containers |
| `docker images` | List built images |

### Important Notes
- Docker Desktop must be running when you use `docker` commands
- `docker compose down` removes containers but keeps volumes (data safe)
- `docker compose down -v` also removes volumes — use only when you want to reset the DB
- Other engineers just need Docker + your code → `docker compose up --build` → everything works
- Images are portable — same image runs identically on any machine with Docker

### Networking
- **Service names** (`db`, `backend`, `frontend`) = hostnames on Docker's internal network. Containers find each other by service name (e.g. `backend:5001`, `db:5432`).
- Every app needs a **listening port** — its "phone number" (nginx=80, Spring Boot=5001, Postgres=5432). `ports:` creates an external phone line from your Mac to that port.
- Docker's DB is completely separate from your local DB — different data, different users.

```
You (browser) → localhost:3000 → [3000:80] → nginx
                                                │  (internal, no ports: needed)
                                                ├── backend:5001
                                                └── db:5432
```
#### My own words
- we need 3000:80 because we do something outside docker, and usually from frontend, so we use external port 3000 and nginx give to docker internal port 80, and call backend or db internally using backend:5001 and db:5432.

---

## M12 — Deployment

### Cloud Platforms (PaaS)
- **PaaS** (Platform as a Service) = cloud platforms like Render, Railway, Fly.io, Heroku that handle infrastructure so you focus on code.
- You provide code + config → platform handles building, running, networking, and public URLs.
- Render supports native runtimes for Node.js, Python, Ruby, Go, Rust, Elixir. Java requires Docker or manually setting build/start commands.

### Web Service vs Static Site
- **Web Service** = a running process (server) that receives requests, executes logic, queries DB. Uses CPU/memory even when idle.
- **Static Site** = just files (HTML/CSS/JS) on a CDN. No process runs. Browser downloads files and runs the app client-side.
- Backend → Web Service (needs to run Java, handle requests, talk to DB).
- Frontend → Static Site (after `npm run build`, it's just files — no server needed).

### CDN (Content Delivery Network)
- Servers spread around the world that cache and serve static files.
- Users download from the nearest server → faster load times.
- Static sites on CDN never "spin down" — files are always ready to serve.

### CORS (Cross-Origin Resource Sharing)
- Browser security rule: by default, JavaScript on origin A **cannot read responses** from origin B.
- CORS is the mechanism that **opens** that door — the backend tells the browser "I allow this origin to read my responses."
- Configured on the backend via `CorsConfiguration`: allowed origins, methods, headers, credentials.
- `setAllowCredentials(true)` = allow the browser to send cookies and Authorization headers in cross-origin requests.
- Not needed when frontend and backend share the same origin (e.g., nginx proxy in Docker Compose).
- Needed when frontend and backend are on different domains (e.g., Render deployment).

### Nginx's Role (Docker Compose vs Render)
- **Nginx** = a web server. It acts as a middleman between the browser and the backend.
- In Docker Compose it had two jobs:
  1. Serve React build files to the browser.
  2. Reverse proxy — forward `/api/` requests to the backend (`http://backend:5001`).
- On Render, nginx is not needed:
  1. CDN serves the files.
  2. Browser calls the backend directly (CORS allows it).

### Environment Variables in Deployment
- `${PORT:5001}` — Render assigns a dynamic port via `PORT` env var. Default to 5001 locally.
- `REACT_APP_` prefix — CRA (Create React App) only exposes env vars with this prefix to frontend code. Safety measure so secrets like `DB_PASSWORD` don't leak into browser-readable JavaScript.
- `REACT_APP_*` vars are baked in at **build time**, not runtime. Changing them requires a rebuild.
- `CORS_ALLOWED_ORIGIN` — read by Spring Boot at startup to configure which frontend origin is allowed.

### CI/CD via Webhook
- When you connect a GitHub repo to Render, a **webhook** is set up.
- Every push to the watched branch (e.g., `main`) triggers an automatic deploy.
- This is a simple form of **CI/CD** (Continuous Integration / Continuous Deployment).

### Free Tier Behavior
- Backend (Web Service) spins down after 15 min of inactivity — the JVM process is stopped. First request after idle takes ~30-60s to cold-start.
- Frontend (Static Site) never spins down — files on CDN cost almost nothing to serve.
- Managed PostgreSQL on free tier is deleted after 90 days — the entire DB instance is removed, not just the data. To recover: create a new free PostgreSQL, update env vars, and Flyway will recreate the tables on first startup (but old data is lost).