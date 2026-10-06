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
| ALL | `/api/**` | Proxied to target backend (:3000) | ✅ Phase 5 (ML hook in Phase 13) |

## Configuration

Edit `src/main/resources/application.properties`.

Active properties:

```properties
server.port=8080
backend.url=http://localhost:3000
```

Future configuration placeholders:

```properties
# ml.service.url=http://localhost:8000
# security.ml.enabled=true
# security.anomaly.threshold=0.5
```

## Package Structure

```
com.krishna.gateway/
├── GatewayApplication.java        Main entry point
├── controller/
│   ├── HealthController.java      GET /api/health (Phase 3)
│   └── ProxyController.java       ALL /api/** catch-all proxy & pipeline trigger (Phase 5/6)
├── filter/
│   └── RequestInterceptor.java    Request interception orchestrator (Phase 6)
├── model/
│   ├── RequestMetadata.java       Raw request metadata DTO (Phase 6)
│   └── SecurityDecision.java      Allow/block decision model (Phase 6)
├── security/
│   ├── MetadataExtractor.java     Safe metadata extraction & IP/header sanitization (Phase 6)
│   └── SecurityPipeline.java      Security analysis contract & baseline ALLOW service (Phase 6)
├── proxy/
│   └── ProxyService.java          HTTP forwarding & error handling (Phase 5)
├── ml/                            ML service client (Phase 13)
├── security/                      Decision engine & rules (Phase 14)
└── logging/                       Security event logger (Phase 15)
```

## Testing

Automated tests:

```bash
mvn test
```

Manual end-to-end testing (with both gateway :8080 and backend :3000 running):

```bash
# Gateway local health
curl http://localhost:8080/api/health

# Actuator health
curl http://localhost:8080/actuator/health

# Proxied backend endpoints
curl http://localhost:8080/api/products
curl http://localhost:8080/api/users
curl http://localhost:8080/api/backend/health
```

