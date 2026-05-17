# Japanese Note

A full-stack Japanese language learning note app. Create, organize, and review vocabulary, grammar, and other notes as you study.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React, Material UI |
| Backend | Java 21, Spring Boot |
| Database | PostgreSQL |
| Auth | JWT (JSON Web Token) |
| Containerization | Docker, Docker Compose |
| Deployment | Render (backend as Docker Web Service, frontend as Static Site) |

## Features

- User registration and login (JWT authentication)
- Create, edit, delete notes (vocab, grammar, other)
- Search notes by keyword
- Pagination
- Per-user note scoping (users only see their own notes)
- Input validation with field-level error messages
- ~~Rate limiting and security headers~~ (not implemented)

## Project Structure

```
japanese_note/
├── backend-java/       # Spring Boot REST API
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/           # React app
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml  # Local dev: runs backend + frontend + PostgreSQL
└── notes.md            # Learning notes from each milestone
```

## Getting Started

### Prerequisites

- Java 21
- Maven
- Node.js 20
- PostgreSQL 16

### Run Locally (without Docker)

1. Start PostgreSQL and create a database named `japanese_note`

2. Start the backend:
   ```bash
   cd backend-java
   mvn spring-boot:run
   ```

3. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm start
   ```

4. Open http://localhost:3000

### Run with Docker Compose

```bash
docker compose up --build
```

This starts the backend, frontend, and PostgreSQL together. Open http://localhost:3000.

## API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/auth/register | Register a new user | No |
| POST | /api/auth/login | Login, returns JWT | No |
| GET | /api/notes | List notes (paginated, searchable) | Yes |
| POST | /api/notes | Create a note | Yes |
| GET | /api/notes/:id | Get a single note | Yes |
| PUT | /api/notes/:id | Update a note | Yes |
| DELETE | /api/notes/:id | Delete a note | Yes |

## Learning Roadmap

This project was built incrementally across 12 milestones as a backend learning vehicle:

| Milestone | Topic |
|---|---|
| M1 | Config & Environment Variables |
| M2 | Complete REST CRUD |
| M3 | Error Handling |
| M4 | Input Validation |
| M5 | Database Migrations (Flyway) |
| M6 | Logging (SLF4J / Logback) |
| M7 | Authentication (Spring Security + JWT) |
| M8 | Pagination & Search |
| M9 | Testing (JUnit 5 + MockMvc) |
| M10 | ~~Security Hardening (Headers + Rate Limiting)~~ — skipped |
| M11 | Docker & Docker Compose |
| M12 | Deployment (Render) |
