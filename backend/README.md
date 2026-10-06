# Target Backend API — Spring Boot Module

## Overview

This is the **Target Backend API** — the downstream service that the API Security Gateway protects.

In this architecture:
- Clients do **not** communicate with this service directly.
- All client requests pass through the **API Security Gateway** running on port `8080`.
- The gateway inspects requests, queries the ML service for anomaly detection, and forwards legitimate requests to this backend on port `3000`.

This backend is intentionally kept lightweight and focused solely on providing realistic mock business endpoints (`/api/products`, `/api/users`) and a health check (`/api/backend/health`).

## Technology Stack

- Java 21 (OpenJDK)
- Spring Boot 3.3.5
- Maven
- Spring Web

## Port

`3000` (configured in `src/main/resources/application.properties`)

## Running

```bash
cd backend
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Endpoints

| Method | Path | Description | Status |
|---|---|---|---|
| GET | `/api/backend/health` | Backend liveness check | ✅ Phase 4 |
| GET | `/api/products` | Mock product catalogue | ✅ Phase 4 |
| GET | `/api/users` | Mock user records | ✅ Phase 4 |

## Sample Responses

### GET `/api/backend/health`
```json
{
  "status": "UP",
  "service": "Target Backend API"
}
```

### GET `/api/products`
```json
[
  "Laptop",
  "Keyboard",
  "Mouse",
  "Monitor",
  "Webcam"
]
```

### GET `/api/users`
```json
[
  { "id": 1, "name": "Alice", "role": "admin" },
  { "id": 2, "name": "Bob", "role": "user" },
  { "id": 3, "name": "Charlie", "role": "user" },
  { "id": 4, "name": "Diana", "role": "moderator" }
]
```

## Testing

Automated tests:
```bash
mvn test
```

Manual endpoint verification:
```bash
curl http://localhost:3000/api/backend/health
curl http://localhost:3000/api/products
curl http://localhost:3000/api/users
```
