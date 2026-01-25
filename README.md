# QA Test Gym Booking API

This project is a backend application built for educational and QA testing purposes.
It models a simplified gym-style booking domain with users, courses, sessions, and bookings.
Full business rules are documented in `RULES.md`.

## Features

* User authentication and authorization (USER, TRAINER, ADMIN)
* Course management 
* Session scheduling 
* Booking management 
* Dual authentication: HTTP Basic & JWT Bearer
* Swagger / OpenAPI documentation
* Docker deployment support

## Prerequisites

* Java 17
* Maven 3.9+
* Docker (optional)

## Running Locally

```
mvn spring-boot:run
```

Server starts at:

```
http://localhost:8080
```

## Swagger / OpenAPI Documentation

Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

Security schemes in Swagger:

* `BasicAuth` — HTTP Basic (email + password)
* `BearerAuth` — JWT token

Use the **Authorize** button to attach credentials for testing.

## Authentication

Two authentication mechanisms are supported:

1. **HTTP Basic Auth**
2. **JWT Bearer Token**

Both share the same user store and authorization rules.

### Obtaining a JWT token

```
POST /api/auth/login
Content-Type: application/json
```

Example:
```
{
  "email": "admin@example.com",
  "password": "admin123"
}
```

Response:
```
{ "token": "..." }
```

Use the token as:
```
Authorization: Bearer <token>
```

## Configuration

YAML configuration:

```
qa-test:
  admin-creation-secret: "change-me-in-prod"

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

## Docker Deployment

Build:
```
docker build -t qa-test-app .
```

Run:
```
docker run -p 8080:8080 
-e QA_TEST_ADMIN_CREATION_SECRET="my-admin-secret" 
-e JWT_SECRET="x9G4kqV2Lw7cR8tZ1fM3pQ6sU9vB2nH5"
-e JWT_EXPIRATION_SECONDS=3600
qa-test-app
```

API will be available at:
```
http://localhost:8080
```

## Domain Overview

Domain objects:

* **Users** — actors with authentication + business roles
    - roles: USER, TRAINER, ADMIN

* **Courses** — activity types (e.g., Yoga, Boxing) associated with a TRAINER

* **Sessions** — scheduled occurrences of a course (e.g., Monday 18:00–19:00)
    - must be in the future
    - must not overlap per course
    - (future) may define capacity

* **Bookings** — Reservations of sessions by users

For detailed business rules see `RULES.md`.

## Intended Usage

The project is designed for:

* automated API testing practice
* QA onboarding & training
* test case design (positive + negative)
* authentication & authorization testing
* interview preparation

It is **not** intended for production use.

## License

MIT — free to use, modify, and extend.
