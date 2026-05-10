# Backend Learning Roadmap — 日本語ノート Side Project

> **Role: PM SPEC** — 10 progressive milestones to sharpen backend skills using this project as a vehicle.

---

## Current State Audit

| Area | Status |
|---|---|
| Routes | GET /api/notes, POST /api/notes only |
| Error handling | None (crashes on DB failure) |
| Input validation | None |
| Auth | None |
| Config / secrets | Hardcoded in server.js |
| Logging | console.log only |
| Tests | None |
| DB migrations | None (manual table creation) |
| Security | No headers, no rate limiting |

---

## Milestones

### M1 — Config & Environment Variables
**Skill: Secure configuration management**

**Tasks:**
- Install `dotenv`
- Create `.env` with DB credentials, PORT, NODE_ENV
- Add `.env` to `.gitignore`
- Replace all hardcoded values in `server.js` with `process.env.*`

**Why it matters:** Hardcoded secrets are a critical security mistake. Every real backend uses env vars.

**Acceptance criteria:**
- [ ] Server starts with config from `.env`
- [ ] Removing `.env` prints a clear startup error, not a cryptic crash

---

### M2 — Complete REST CRUD
**Skill: RESTful API design, HTTP methods & status codes**

**Tasks — add missing endpoints:**

| Method | Route | Action | Status Code |
|---|---|---|---|
| GET | /api/notes | List notes (by type) | 200 |
| POST | /api/notes | Create note | **201** |
| GET | /api/notes/:id | Get single note | 200 / 404 |
| PUT | /api/notes/:id | Replace note | 200 / 404 |
| DELETE | /api/notes/:id | Delete note | **204** / 404 |

**Why it matters:** REST is the lingua franca of web APIs. HTTP semantics (PUT vs PATCH, 201 vs 200, 204 for delete) are foundational knowledge.

**Acceptance criteria:**
- [ ] Each route returns the correct HTTP status code
- [ ] 404 returned when note ID doesn't exist
- [ ] DELETE returns empty body with 204

---

### M3 — Error Handling Middleware
**Skill: Centralized error handling, Express middleware pattern**

**Tasks:**
- Wrap all route handlers in `try/catch`
- Create a single `errorHandler(err, req, res, next)` middleware at the bottom of `server.js`
- Return structured JSON errors: `{ error: "message", code: "ERROR_CODE" }`

**Why it matters:** Without this, one DB timeout crashes the whole server. Error middleware is a core Express pattern used in every real app.

**Acceptance criteria:**
- [ ] Kill the DB → server returns 500 JSON, doesn't crash
- [ ] Invalid route → returns 404 JSON, not Express default HTML

---

### M4 — Input Validation
**Skill: Data validation, defensive programming**

**Tasks:**
- Install `zod` (or `joi`)
- Define a schema for note creation: `title` (required, max 200 chars), `type` (enum), `meaning`/`example` (optional)
- Return 400 with field-level error messages on invalid input
- Extract as a validation middleware that runs before the route handler

**Why it matters:** Never trust client input. Validation prevents bad data from reaching the DB.

**Acceptance criteria:**
- [ ] POST with empty title → `400 { "error": "title is required" }`
- [ ] POST with invalid type → `400 { "error": "type must be one of: vocab, grammar, other" }`
- [ ] Valid input passes through to DB as before

---

### M5 — Database Migrations
**Skill: Schema versioning, migration tools**

**Tasks:**
- Create a `/migrations` folder with numbered SQL files
- `001_create_notes.sql` — create notes table with proper constraints (NOT NULL, PRIMARY KEY, DEFAULT NOW())
- `002_add_indexes.sql` — add index on `type` and `created_at` for query performance
- Write a `migrate.js` script that runs pending migrations on startup

**Why it matters:** In real projects, the DB schema changes over time. Migrations let you track and replay those changes safely.

**Acceptance criteria:**
- [ ] Fresh DB + run migrations → table created automatically
- [ ] Running migrations twice is idempotent (no errors)

---

### M6 — Request Logging
**Skill: Observability, logging best practices**

**Tasks:**
- Install `morgan`
- Configure log format: `METHOD URL STATUS response_time`
- Log errors with stack traces to stderr, not stdout

**Why it matters:** You can't debug production issues without logs.

**Acceptance criteria:**
- [ ] Every request prints: `GET /api/notes?type=vocab 200 12ms`
- [ ] Errors print full stack trace

---

### M7 — Authentication with JWT
**Skill: Auth fundamentals, stateless authentication**

**Tasks:**
- Install `bcrypt` + `jsonwebtoken`
- Add `POST /api/auth/register` and `POST /api/auth/login`
- Create a `users` table (new migration)
- Add JWT auth middleware that protects all `/api/notes` routes
- Add `user_id` foreign key to notes table (migration + update all queries)

**Why it matters:** Auth is unavoidable in real backends. JWT is the industry-standard pattern for stateless APIs.

**Acceptance criteria:**
- [ ] Register + login returns a JWT token
- [ ] Calling `/api/notes` without token → 401
- [ ] Notes are scoped per user (user A can't see user B's notes)

---

### M8 — Pagination, Search & Filtering
**Skill: Query design, API ergonomics**

**Tasks:**
- Add `?page=1&limit=20` pagination to `GET /api/notes`
- Add `?search=keyword` full-text search (PostgreSQL `ILIKE`)
- Return metadata: `{ data: [...], total: 42, page: 1, pages: 3 }`

**Why it matters:** Returning all rows forever doesn't scale. Pagination + search are standard in any production API.

**Acceptance criteria:**
- [ ] Default limit is 20; max limit enforced at 100
- [ ] `?search=食べる` returns notes containing that term in title or example

---

### M9 — Testing
**Skill: Integration testing**

**Tasks:**
- Install `jest` + `supertest`
- Write integration tests covering: all CRUD routes, validation errors, auth middleware
- Use a separate test database (`japanese_notes_test`)
- Add `npm test` script to `package.json`

**Why it matters:** Tests are how professionals verify correctness without manual clicking.

**Acceptance criteria:**
- [ ] `npm test` runs and passes
- [ ] A broken route causes at least one test to fail

---

### M10 — Security Hardening
**Skill: API security, production readiness**

**Tasks:**
- Install `helmet` (security headers)
- Install `express-rate-limit` — 100 req/15min per IP on auth routes
- Tighten CORS to only allow `localhost:3000`, not `*`

**Why it matters:** Security isn't optional. These are the minimum layers every production Express app should have.

**Acceptance criteria:**
- [ ] Response headers include `Content-Security-Policy`, `X-Frame-Options`, etc.
- [ ] 101st request in 15 min to `/api/auth/login` → `429 Too Many Requests`

---

### M11 — Docker
**Skill: Containerization, reproducible environments**

**Tasks:**
- Write a `Dockerfile` for the Spring Boot backend
- Write a `docker-compose.yml` that runs both the backend and PostgreSQL
- Configure environment variables via Docker Compose instead of local `.env`

**Why it matters:** "Works on my machine" is the #1 deployment problem. Containers guarantee the same environment everywhere.

**Acceptance criteria:**
- [ ] `docker compose up` starts both the backend and PostgreSQL from scratch
- [ ] API works identically to running locally

---

### M12 — Deployment
**Skill: Cloud deployment, CI/CD basics**

**Tasks:**
- Choose a cloud platform (e.g. Railway, Fly.io, Render, or AWS)
- Deploy the Dockerized app with a managed PostgreSQL instance
- Set up environment variables on the platform
- Verify the API is accessible via a public URL

**Why it matters:** A project that only runs on localhost is a demo. Deployment is what makes it real.

**Acceptance criteria:**
- [ ] App is live at a public URL (e.g. `https://your-app.fly.dev`)
- [ ] All API endpoints work the same as local

---

### M13 — Custom Domain & HTTPS
**Skill: DNS, TLS/SSL, production networking**

**Tasks:**
- Buy a domain (e.g. via Namecheap, Cloudflare, Google Domains)
- Point the domain to your deployed app via DNS records
- Set up HTTPS (most platforms provide free SSL via Let's Encrypt)

**Why it matters:** A custom domain with HTTPS is the difference between a side project and a real product.

**Acceptance criteria:**
- [ ] App accessible at `https://yourdomain.com`
- [ ] HTTP requests redirect to HTTPS
- [ ] Browser shows the lock icon (valid SSL certificate)

---

## Priority Order

| Priority | Milestone | Rationale |
|---|---|---|
| P0 | M1 Config | Fix the security hole first |
| P0 | M2 CRUD | Fundamental REST completeness |
| P0 | M3 Error Handling | Without this, nothing is stable |
| P1 | M4 Validation | Protect the DB from bad data |
| P1 | M5 Migrations | Required before adding users table in M7 |
| P1 | M6 Logging | Observability needed before debugging M7+ |
| P2 | M7 Auth | Big milestone — plan extra time |
| P2 | M8 Pagination | Product feature + query skill |
| P2 | M9 Testing | Best done alongside M7–M8 |
| P3 | M10 Security | Polish and production readiness |
| P3 | M11 Docker | Required before deploying |
| P3 | M12 Deployment | Make it live |
| P3 | M13 Domain + HTTPS | Final polish — real URL with SSL |

---

## How to Test Each Milestone

Use `curl` in your terminal or a GUI tool like **Bruno** / **Postman**:

```bash
# Test a GET
curl http://localhost:5001/api/notes?type=vocab

# Test a POST (M2)
curl -X POST http://localhost:5001/api/notes \
  -H "Content-Type: application/json" \
  -d '{"type":"vocab","title":"食べる","meaning":"to eat","example":"私は食べる"}'

# Test validation error (M4)
curl -X POST http://localhost:5001/api/notes \
  -H "Content-Type: application/json" \
  -d '{"type":"invalid"}'
# Expected: 400 { "error": "title is required" }

# Test DELETE (M2)
curl -X DELETE http://localhost:5001/api/notes/1
# Expected: 204 no body
```
