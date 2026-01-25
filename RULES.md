# Business Rules (Updated Domain Model)

## Domain Overview

The system models a simplified course booking workflow intended for QA testing and validation scenarios.

Primary domain entities:
- Users — authenticated actors (roles: USER, ADMIN, TRAINER)
- Courses — bookable offerings (e.g., “Yoga for Beginners”)
- Sessions — scheduled time slots for specific courses
- Bookings — reservations of sessions by users

The system enforces a set of business rules aimed at validating role-based access, time-based booking constraints, and ownership behaviors.


## Rule 1 — Restricted Creation of Admin Users

Creating a user with role ADMIN requires a privileged header to prevent unauthorized elevation of privileges.

**Applies to:**
POST /api/users  
PUT /api/users/{id} _(when changing role to ADMIN)_

**Requirement:**
Header: `X-Admin-Secret: <secret>`  
Value must match configured application secret.

**Violation:**
403 Forbidden

### Allowed Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create USER without secret                                          | 201 Created     |
| Create ADMIN with correct secret                                    | 201 Created     |
| Update USER → ADMIN with correct secret                             | 200 OK          |

### Forbidden Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create ADMIN without secret                                         | 403 Forbidden   |
| Create ADMIN with incorrect secret                                  | 403 Forbidden   |
| Update USER → ADMIN without secret                                  | 403 Forbidden   |


## Rule 2 — Password Required for User Creation

All newly created users must provide a non-empty password.

**Applies to:**
POST /api/users

**Requirement:**
`password` must be non-empty

**Violation:**
400 Bad Request

### Allowed Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create USER with password                                           | 201 Created     |
| Create ADMIN with password + valid secret                           | 201 Created     |

### Forbidden Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create USER without password                                        | 400 Bad Request |
| Create USER with empty or whitespace-only password                  | 400 Bad Request |


## Rule 3 — Only Admin Users May Manage Courses

Course lifecycle operations are restricted to ADMIN users.

**Applies to:**
POST /api/courses  
PUT /api/courses/{id}  
DELETE /api/courses/{id}

Reads:
GET /api/courses  
GET /api/courses/{id}

Read operations require authentication; write operations require ADMIN.

### Allowed Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| ADMIN creates/updates/deletes course                                | 2xx Success     |
| USER reads courses                                                  | 200 OK          |

### Forbidden Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| USER attempts create/update/delete                                  | 403 Forbidden   |
| Unauthenticated client accesses any course endpoint                 | 401 Unauthorized |


## Rule 4 — Session Start Must Be in the Future

Sessions may only be scheduled for future time ranges.

**Applies to:**
POST /api/sessions  
PUT /api/sessions/{id}

**Constraint:**
startTime > now

**Violation:**
400 Bad Request

### Allowed Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create/update session with future startTime                         | 2xx Success     |

### Forbidden Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create/update session with startTime in the past                    | 400 Bad Request |
| Create/update session with startTime equal to current time          | 400 Bad Request |


## Rule 5 — Sessions Must Not Overlap for the Same Course

Two sessions for the same course must not overlap.

**Overlap Condition:**
A.start < B.end AND A.end > B.start

**Applies to:**
POST /api/sessions  
PUT /api/sessions/{id}

**Violation:**
409 Conflict

### Allowed Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Two sessions without overlap for the same course                    | 2xx Success     |
| Identical time ranges across different courses                      | 2xx Success     |

### Forbidden Scenarios

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Create/update overlapping sessions for the same course              | 409 Conflict    |


# Booking Rules (Combined Block)

Booking behaviors include time constraints and ownership/role constraints.

## Rule B1 — Session Must Be in the Future to Be Bookable

Users may only book sessions whose startTime > now.

**Applies to:**
POST /api/bookings

**Violation:**
400 Bad Request

### Allowed

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| User books future session                                           | 201 Created     |

### Forbidden

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| User books session in the past                                      | 400 Bad Request |
| User books session at current time                                  | 400 Bad Request |


## Rule B2 — Session Must Be in the Future to Allow Cancellation

Bookings may only be cancelled while the associated session has not started.

**Applies to:**
PATCH /api/bookings/{id}/cancel

**Violation:**
400 Bad Request

### Allowed

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Cancel booking before session start                                 | 200 OK          |

### Forbidden

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Cancel booking after session start                                  | 400 Bad Request |
| Cancel booking at exact session start time                          | 400 Bad Request |


## Rule B3 — Only Owning User or Admin May Create or Cancel

Ownership and role restrictions apply to creation and cancellation.

**Creation Behavior:**
- If userId omitted → booking assigned to current user
- If userId provided → caller must be same user OR ADMIN

**Cancellation Behavior:**
- Owning user OR ADMIN may cancel (subject to Rule B2)

**Violation:**
403 Forbidden

### Allowed

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| User creates booking for self                                       | 201 Created     |
| Admin creates booking for any user                                  | 201 Created     |
| Owning user cancels booking (future session)                        | 200 OK          |
| Admin cancels booking                                               | 200 OK          |

### Forbidden

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| User creates booking for another user (not admin)                   | 403 Forbidden   |
| User cancels booking for another user (not admin)                   | 403 Forbidden   |


## Rule 9 — Only Admin Users May Manage Sessions

Write operations on sessions require ADMIN.

**Applies to:**
POST /api/sessions  
PUT /api/sessions/{id}  
DELETE /api/sessions/{id}

Reads require authentication.

### Allowed

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| ADMIN creates/updates/deletes session                               | 2xx Success     |
| USER reads sessions                                                 | 200 OK          |

### Forbidden

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| USER attempts to modify session                                     | 403 Forbidden   |
| Unauthenticated access to session endpoints                         | 401 Unauthorized |


## Rule 10 — Session Time Range Must Be Valid

Constraint:
- startTime > now
- endTime > startTime

**Violation:**
400 Bad Request

### Allowed

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| Valid future time range                                             | 2xx Success     |

### Forbidden

| Scenario                                                            | Expected Result |
|---------------------------------------------------------------------|-----------------|
| endTime <= startTime                                                | 400 Bad Request |
| startTime <= now                                                    | 400 Bad Request |


---
End of Rules.
