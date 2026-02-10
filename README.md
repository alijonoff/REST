# User Management REST API (Spring Boot)

## What this application is about
A simple RESTful backend for managing users. The project demonstrates core backend features:
- CRUD operations with persistence
- Authentication/Authorization (Basic Auth)
- Global error handling with consistent JSON responses
- API versioning (URI-based)
- Pagination for list endpoints
- Monitoring via Spring Boot Actuator

---

## Requirements

### 1) Project Setup
- Create a Spring Boot project with these dependencies:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - H2 Database (in-memory for development) *(or MySQL/PostgreSQL for production-like setup)*
  - Spring Boot Actuator
- Configure application properties (`application.yml` or `application.properties`) for:
  - datasource (H2 or external DB)
  - JPA (DDL auto, SQL logging optional)
  - H2 console enabled for development
  - Actuator endpoints exposure (at least `health` and `info`)

---

### 2) Data Model
- Create a `User` entity/table to store user data.
- Minimum fields:
  - `id` (primary key)
  - `username` (unique, required)
  - `email` (unique, required)
  - `enabled` (boolean, default true) *(optional but recommended)*
  - `createdAt` timestamp *(optional)*
- Add validation rules:
  - username not blank, minimum length (e.g., 3–5 chars)
  - email must be valid format

---

### 3) CRUD Operations
- Implement a `UserService` to contain business logic.
- Implement a `UserRepository` (Spring Data JPA) for database access.
- Implement controller endpoints for:
  - `POST /api/v1/users` → create user
  - `GET /api/v1/users/{id}` → get user by id
  - `GET /api/v1/users` → list users
  - `PUT /api/v1/users/{id}` → update user
  - `DELETE /api/v1/users/{id}` → delete user
- Expected HTTP status codes:
  - 201 Created (create)
  - 200 OK (read/update)
  - 204 No Content (delete)
  - 400 Bad Request (validation/input errors)
  - 404 Not Found (missing user)
  - 500 Internal Server Error (unexpected/server errors)

---

### 4) Authentication & Authorization
- Configure Spring Security with Basic Authentication.
- Protect API endpoints under `/api/**`.
- Allow unauthenticated access to:
  - `/actuator/health`
  - `/h2-console/**` (development only)
- Use a safe password strategy in real apps (hashing, no plaintext storage).

---

### 5) Error Handling
- Implement global exception handling using `@ControllerAdvice`.
- Define a consistent `ErrorResponse` JSON structure, including:
  - timestamp
  - status
  - error
  - message
  - path
- Handle at minimum:
  - validation errors → 400
  - not found errors → 404
  - generic/unexpected errors → 500

---

### 6) API Versioning
- Use URI versioning:
  - `/api/v1/...`
  - `/api/v2/...`
- Provide at least two controller versions (v1 and v2).
- v2 may differ slightly (e.g., use Pageable directly, different response structure, etc.).

---

### 7) Pagination
- Add pagination support to user listing endpoint(s):
  - support `page` and `size`
  - support sorting (recommended)
- Example:
  - `GET /api/v1/users?page=0&size=10&sortBy=username&direction=asc`
  - `GET /api/v2/users?page=0&size=10&sort=username,desc`

---

## Notes
- Use layered architecture: Controller → Service → Repository.
- Do not hardcode secrets (DB credentials) in source code.
- H2 console should not be exposed in production.









# Task 2 — Code Review Comments (UserController JDBC/Servlet code)

## Critical issues (must fix)
1. **SQL Injection vulnerability**
   - Problem: `SELECT * FROM Users WHERE id = '" + userId + "'`
   - Risk: attackers can inject SQL via `userId`
   - Fix: use `PreparedStatement` with `?` parameters.

2. **Hardcoded database credentials**
   - Problem: DB URL, username, password are in code.
   - Risk: secrets leak, insecure deployments.
   - Fix: load from environment/config; never use `root` in app code.

3. **Deprecated MySQL driver**
   - Problem: `com.mysql.jdbc.Driver` is outdated.
   - Fix: use `com.mysql.cj.jdbc.Driver` (or rely on auto-loading).

4. **Resource leaks (no try-with-resources)**
   - Problem: manual `close()` calls won’t run if an exception happens earlier.
   - Fix: use try-with-resources for `Connection`, `Statement`, and `ResultSet`.

5. **Exception handling is unsafe/incomplete**
   - Problem: catches `Exception` and prints to console only.
   - Risk: client may receive no proper HTTP response; debugging and monitoring become poor.
   - Fix: return meaningful HTTP codes + structured error response; log properly.

6. **Code does not compile**
   - Problem: `this.DB_URL = "jdbc:mysql://localhost:3306/anotherdb";`
   - Reason: `DB_URL` is `static final` (constant) and cannot be reassigned.

7. **NullPointerException risk in doPost**
   - Problem: `username.length() < 5` before checking `username == null`.
   - Fix: validate null/blank first, then check length.

8. **Wrong HTTP status codes**
   - Problem: missing/empty parameters return `500`.
   - Correct: client input errors should return `400 Bad Request`.
   - Also: if user not found, return `404 Not Found`.

## Important issues (should fix)
9. **Response is not valid JSON**
   - Problem: `users.toString()` outputs something like `[Alice, Bob]` (not guaranteed JSON-safe).
   - Fix: use a JSON serializer (Jackson/Gson) or build correct JSON.

10. **Query by ID but returns a list**
   - Problem: `WHERE id = ...` suggests one row, but returns `List<String> users`.
   - Fix: return single user object or 404; if multiple expected, query should reflect that.

11. **No input validation / type validation**
   - `userId` should be validated (e.g., numeric if `id` is numeric).
   - Return `400` if invalid.

12. **Passwords handled insecurely**
   - Password is read but not hashed/stored safely (and should never be logged).
   - In real apps: hash with BCrypt, enforce strength rules, use HTTPS.

## Design / maintainability issues
13. **Mixing controller and database logic**
   - Problem: servlet/controller directly manages JDBC connection and SQL.
   - Fix: separate layers (Controller → Service → Repository/DAO).

14. **Debug / heavy printing inside request handler**
   - Problem: nested loops printing in `doPost` wastes time and spams output.
   - Fix: remove completely.

15. **Logging style**
   - Problem: `System.out.println(...)` and `printStackTrace()`.
   - Fix: use a logging framework (SLF4J + Logback) with log levels.

## Overall recommendation
- If this is a Spring Boot assignment, replace direct JDBC-in-controller with:
  - JPA Entity + Repository
  - Service for business logic
  - REST controller for HTTP mapping
  - Global exception handler for consistent errors
  - Spring Security for auth
- If you must use JDBC, still:
  - use PreparedStatement
  - use try-with-resources
  - validate inputs
  - return correct HTTP responses
  - serialize JSON properly

