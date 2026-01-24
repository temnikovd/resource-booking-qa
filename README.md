
---

# QA Test Slot Booking API

This project is a backend application built for educational purposes to practice API testing and validation.
It models a simplified slot booking domain with users, resources, slots, and bookings.
Business rules are documented separately in `RULES.md`.

---

## Features

* User management (USER and ADMIN)
* Resource management (admin-only writes)
* Slot management (admin-only writes, time and overlap validation)
* Booking management (ownership and time-based constraints)
* HTTP Basic authentication
* JWT Bearer token authentication
* Swagger / OpenAPI documentation
* Docker deployment support

---

## Prerequisites

* Java 17
* Maven 3.9+
* Docker (optional)

---

## Running Locally

Build and run using Maven:

```
mvn spring-boot:run
```

Application starts at:

```
http://localhost:8080
```

---

## Swagger / OpenAPI Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```
http://localhost:8080/v3/api-docs
```

Security schemes available in Swagger:

* `BasicAuth` (HTTP Basic)
* `BearerAuth` (JWT tokens)

Use the **Authorize** button in Swagger to authenticate.

---

## Authentication

The API supports **two parallel authentication mechanisms**:

1. HTTP Basic Auth
2. JWT Bearer Tokens

Both mechanisms share the same user store and authorization rules.

### Basic Authentication

Login using email and password.

Example credentials:

* username: `admin@example.com`
* password: `admin123`

### JWT Authentication

To obtain a JWT token:

```
POST /api/auth/login
Content-Type: application/json
```

Example request:

```
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

Example response:

```
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```

Use the token in requests:

```
Authorization: Bearer <token>
```

Both Basic and Bearer methods are accepted interchangeably across secured endpoints.

---

## Configuration

Main configuration:

```
qa-test:
  admin-creation-secret: "change-me-in-prod"
```

JWT configuration, secret should be at least 32 characters long:

```
jwt:
  secret: "x9G4kqV2Lw7cR8tZ1fM3pQ6sU9vB2nH5"
  expiration-seconds: 3600
```

Environment overrides:

```
QA_TEST_ADMIN_CREATION_SECRET
JWT_SECRET
JWT_EXPIRATION_SECONDS
```

---

## Docker Deployment

Build image:

```
docker build -t qa-test-app .
```

Run container:

```
docker run \
  -p 8080:8080 \
  -e QA_TEST_ADMIN_CREATION_SECRET="my-admin-secret" \
  -e JWT_SECRET="x9G4kqV2Lw7cR8tZ1fM3pQ6sU9vB2nH5" \
  -e JWT_EXPIRATION_SECONDS=3600 \
  qa-test-app
```

The API will be available at:

```
http://localhost:8080
```

---

## Domain Overview

Main domain objects:

* **Users** — represent authenticated actors
* **Resources** — bookable entities
* **Slots** — time intervals for resources
* **Bookings** — reservations made by users

For full business rules, refer to:

`RULES.md`

---

## Intended Usage

This project is intended for:

* practicing automated API testing
* QA onboarding and training
* negative scenario design
* security and authentication testing
* interview preparation

It is **not** designed as a production-ready booking system.

---
## License
MIT — feel free to use, modify and extend.

---

