# API Security Gateway — Spring Boot Module

## Overview

This is the **API Security Gateway** — the Java component that sits between
clients and the target backend API. It is responsible for:

1. Intercepting all incoming HTTP requests
2. Capturing raw request metadata (method, path, query, headers, IP, timestamp)
3. Sending that metadata to the Python ML service for anomaly scoring
4. Applying the security policy (ALLOW / BLOCK)
5. Forwarding allowed requests to the target backend
6. Logging security events

## Technology

- Java 21 (OpenJDK)
- Spring Boot 3.3.5
- Maven
- Spring Web
- Spring Boot Actuator

## Port

`8080`

## Running

```bash
cd gateway
mvn spring-boot:run
```

Or build a JAR first:

```bash
mvn clean package -DskipTests
java -jar target/gateway-0.0.1-SNAPSHOT.jar
```

## Endpoints

| Method | Path | Description | Status |
|---|---|---|---|
| GET | `/api/health` | Gateway liveness check | ✅ Phase 3 |
| GET | `/actuator/health` | Spring Boot Actuator health | ✅ Phase 3 |
| ALL | `/api/**` | Proxied to backend via ML check | ⏳ Phase 5/13 |

## Configuration

Edit `src/main/resources/application.properties`.

Key properties (future phases):

```properties
server.port=8080
backend.url=http://localhost:3000
ml.service.url=http://localhost:8000
security.ml.enabled=true
security.anomaly.threshold=0.5
```

## Package Structure

```
com.krishna.gateway/
├── GatewayApplication.java        Main entry point
├── controller/
│   └── HealthController.java      GET /api/health  (Phase 3)
├── filter/                        Request interceptor (Phase 6)
├── proxy/                         Backend proxy (Phase 5)
├── ml/                            ML service client (Phase 13)
├── security/                      Decision engine (Phase 14)
└── logging/                       Security event logger (Phase 15)
```

## Testing

```bash
mvn test
```

Manual:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/actuator/health
```
