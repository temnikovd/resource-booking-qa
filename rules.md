
---

## Rule 1 — Restricted Creation of Admin Users

Creating a user with the role `ADMIN` is restricted and requires a special header to be provided in the request.

### Intent

Admin-level users represent privileged actors within the system and must not be created unintentionally or by regular clients without proper authorization.

### Rule Description

When performing the following operations:

* `POST /api/users`
* `PUT /api/users/{id}` (when changing role to `ADMIN`)

the request must contain a valid header:

```
X-Admin-Secret: <secret-value>
```

The server compares the value of this header against the configured application secret:

```
qa-test.admin-creation-secret
```

If the header is missing or contains an invalid value, the server returns:

```
403 Forbidden
```

with a business-relevant error message.

### Allowed Scenarios

| Scenario                                                    | Expected Result |
| ----------------------------------------------------------- | --------------- |
| Create a regular user (role `USER`) without header          | `201 Created`   |
| Create an admin user with correct secret                    | `201 Created`   |
| Update an existing user to role `ADMIN` with correct secret | `200 OK`        |

### Forbidden Scenarios

| Scenario                                              | Expected Result |
| ----------------------------------------------------- | --------------- |
| Create admin user without header                      | `403 Forbidden` |
| Create admin user with incorrect header value         | `403 Forbidden` |
| Modify user from role `USER` → `ADMIN` without header | `403 Forbidden` |

### Notes

* Deleting admin users is **not restricted** by this rule (may be restricted later if needed).
* Retrieving admin users is **not restricted**.
* This rule does **not** apply to role `USER`.

---

## Rule 2 — Password Requirement on User Creation

Creation of any user (regardless of role) requires a non-empty password to be provided in the request payload.

### Intent

A user without a password would create inconsistent authentication semantics and would make automated testing of authentication scenarios ambiguous. This rule ensures that every newly created user carries valid credentials from the moment of creation.

### Rule Description

When performing:

* `POST /api/users`

the request payload must include a field:

```
password: <non-empty-string>
```

If the field is missing or blank, the server returns:

```
400 Bad Request
```

with a business-relevant error message.

### Allowed Scenarios

| Scenario                                                            | Expected Result |
| ------------------------------------------------------------------- | --------------- |
| Create user with role `USER` and non-empty password                 | `201 Created`   |
| Create user with role `ADMIN` and non-empty password + valid secret | `201 Created`   |

### Forbidden Scenarios

| Scenario                                             | Expected Result   |
| ---------------------------------------------------- | ----------------- |
| Create user without `password` field                 | `400 Bad Request` |
| Create user with empty password (`""` or whitespace) | `400 Bad Request` |

### Notes

* Password changes during user updates are allowed but not required.
* Password value is **not returned** in any API response, even when changed.
* This rule is independent from role-based rules (e.g., Rule 1 for admin creation).


---

## Rule 3 — Only Admin Users Can Manage Resources

Creation, modification, and deletion of resources is restricted to users with the `ADMIN` role.

### Intent

Resources represent bookable entities (e.g., rooms, courts, trainers). Their lifecycle affects all bookings. Only privileged users should be allowed to create, modify, or delete resources.

### Rule Description

The following operations:

* `POST /api/resources`
* `PUT /api/resources/{id}`
* `DELETE /api/resources/{id}`

are allowed **only** for authenticated users with role `ADMIN`.

Reading resources:

* `GET /api/resources`
* `GET /api/resources/{id}`

is allowed for any authenticated user (both `USER` and `ADMIN`).

The rule is enforced using Spring Security roles:

* `User.role = ADMIN` → `ROLE_ADMIN`
* `User.role = USER` → `ROLE_USER`

Authorization checks:

* Write operations on `/api/resources/**` → `hasRole('ADMIN')`
* Read operations on `/api/resources/**` → `isAuthenticated()`

### Allowed Scenarios

| Scenario                                                 | Expected Result |
| -------------------------------------------------------- | --------------- |
| Authenticated `ADMIN` creates/updates/deletes a resource | `2xx` (success) |
| Authenticated `USER` reads resources                     | `200 OK`        |

### Forbidden Scenarios

| Scenario                                                | Expected Result    |
| ------------------------------------------------------- | ------------------ |
| Unauthenticated client accesses any `/api/resources/**` | `401 Unauthorized` |
| Authenticated `USER` calls `POST /api/resources`        | `403 Forbidden`    |
| Authenticated `USER` calls `PUT /api/resources/{id}`    | `403 Forbidden`    |
| Authenticated `USER` calls `DELETE /api/resources/{id}` | `403 Forbidden`    |

### Notes

* This rule applies only to the `Resource` entity.
* User and booking management follow their own rules (see Rule 1, Rule 2, etc.).
* For training purposes, authentication is done via HTTP Basic using `email` as username and `password` as password.

---

Понял — ты имеешь в виду, что GitHub/IDE «съедает» тройные бэктики и формат разваливается. Дам вариант **без тройных бэктиков вообще**, только с заголовками, таблицами и инлайновыми `code` — полностью безопасный для копипаста в `RULES.md`.

---

## Rule 4 — Slot Start Time Must Be in the Future

Slot `startTime` must be strictly in the future relative to server time.
Slots cannot be created or updated in the past or at the current moment.

**Applies to:**
POST /api/slots
PUT /api/slots/{id}

**Constraint:**
`startTime > now`

**Response on violation:**
`400 Bad Request`

### Allowed Scenarios

| Scenario                           | Expected Result |
| ---------------------------------- | --------------- |
| Create slot starting in the future | 201 Created     |
| Update slot to a future start time | 200 OK          |

### Forbidden Scenarios

| Scenario                              | Expected Result |
| ------------------------------------- | --------------- |
| Create slot in the past               | 400 Bad Request |
| Update slot placing it in the past    | 400 Bad Request |
| Use current server time as start time | 400 Bad Request |

### Notes

* Validation is evaluated using server time.
* Slots must represent future availability to make sense in booking flows.
* End time validation is handled separately.

---

## Rule 5 — Slot Must Not Overlap Existing Slots for the Same Resource

Two slots for the same resource must not overlap in time.
This ensures exclusive scheduling per resource.

**Applies to:**
POST /api/slots
PUT /api/slots/{id}

**Overlap condition:**
Two intervals A and B overlap when:
A.start < B.end AND A.end > B.start

**Response on violation:**
`409 Conflict`

### Allowed Scenarios

| Scenario                                  | Expected Result |
| ----------------------------------------- | --------------- |
| Slots for same resource without overlap   | 2xx Success     |
| Slots for different resources             | 2xx Success     |
| Updating slot without introducing overlap | 200 OK          |

### Forbidden Scenarios

| Scenario                       | Expected Result |
| ------------------------------ | --------------- |
| Create overlapping slot        | 409 Conflict    |
| Update slot to overlap another | 409 Conflict    |

### Notes

* Overlap check applies only within the same resource.
* Different resources may have identical schedules without conflict.
* Zero-length or inverted intervals are rejected by time-range rules.

---

## Rule 6 — Slot Must Be in the Future to Be Bookable

A booking can only be created for a slot whose start time is strictly in the future relative to server time.

**Applies to:**
POST /api/bookings

**Constraint:**
The associated slot must satisfy: `slot.startTime > now`

**Response on violation:**
`400 Bad Request`

### Allowed Scenarios

| Scenario                                         | Expected Result |
| ------------------------------------------------ | --------------- |
| Create booking for a slot starting in the future | 201 Created     |

### Forbidden Scenarios

| Scenario                                                          | Expected Result |
| ----------------------------------------------------------------- | --------------- |
| Create booking for a slot in the past                             | 400 Bad Request |
| Create booking for a slot starting exactly at current server time | 400 Bad Request |

### Notes

* Validation is evaluated using server time on the backend.
* Slot time validation on booking is independent from slot creation rules and is always re-checked when booking is created.

---

## Rule 7 — Slot Must Be in the Future to Cancel Booking

A booking can only be cancelled while its associated slot has not yet started.
If the slot start time is in the past or exactly at the current time, cancellation is not allowed.

**Applies to:**
PATCH /api/bookings/{id}/cancel

**Constraint:**
The associated slot must satisfy: `slot.startTime > now`

**Response on violation:**
`400 Bad Request`

### Allowed Scenarios

| Scenario                                             | Expected Result |
| ---------------------------------------------------- | --------------- |
| Cancel booking when slot start time is in the future | 200 OK          |

### Forbidden Scenarios

| Scenario                                                       | Expected Result |
| -------------------------------------------------------------- | --------------- |
| Cancel booking when slot start time is in the past             | 400 Bad Request |
| Cancel booking when slot start time equals current server time | 400 Bad Request |

### Notes

* This rule applies even if the caller is an admin.
* Once the slot has started (or is in the past), the booking becomes effectively immutable from a cancellation perspective.

---

## Rule 8 — Only Owning User or Admin May Create or Cancel Booking

Booking creation and cancellation are restricted to:

* the user who owns the booking (the “owning user”), or
* a user with role `ADMIN`.

No other authenticated user may create bookings on behalf of someone else or cancel someone else’s bookings.

**Applies to:**
POST /api/bookings
PATCH /api/bookings/{id}/cancel

**Creation behavior:**

* If `userId` is omitted in the request body, the booking is created for the currently authenticated user.
* If `userId` is provided, the caller must either:

    * be the same user (`userId == currentUser.id`), or
    * have role `ADMIN`.

Otherwise, the operation is forbidden.

**Cancellation behavior:**

* Only the owning user (booking.userId) or an `ADMIN` may cancel the booking.

**Response on violation:**
`403 Forbidden`

### Allowed Scenarios

| Scenario                                                                       | Expected Result |
| ------------------------------------------------------------------------------ | --------------- |
| Authenticated user creates booking for themselves (no userId provided)         | 201 Created     |
| Authenticated user creates booking for themselves (userId equals current user) | 201 Created     |
| Admin creates booking for any user                                             | 201 Created     |
| Owning user cancels their own booking (slot start time in the future)          | 200 OK          |
| Admin cancels any booking (slot start time in the future)                      | 200 OK          |

### Forbidden Scenarios

| Scenario                                              | Expected Result |
| ----------------------------------------------------- | --------------- |
| Non-admin user creates booking for a different userId | 403 Forbidden   |
| Non-admin user cancels booking owned by another user  | 403 Forbidden   |

### Notes

* Role checks are based on the authenticated principal resolved by the security layer.
* Ownership is determined by matching `booking.user.id` against the current user’s id.
* These rules are enforced in addition to slot-time rules (Rule 6 and Rule 7).

## Rule 9 — Only Admin Users May Create, Update, or Delete Slots

Slots represent availability for booking and must be managed only by privileged users.
Creation, modification and deletion of slots is restricted to users with role `ADMIN`.

**Applies to:**
POST /api/slots
PUT /api/slots/{id}
DELETE /api/slots/{id}

**Authorization constraints:**

* Caller must be authenticated.
* Caller must have role `ADMIN` for any write operation on `/api/slots/**`.
* Read operations (`GET /api/slots`, `GET /api/slots/{id}`) are allowed for any authenticated user (both `USER` and `ADMIN`).

**Response on violation:**
`401 Unauthorized` — when no authentication is provided.
`403 Forbidden` — when authenticated user does not have `ADMIN` role.

### Allowed Scenarios

| Scenario               | Expected Result |
| ---------------------- | --------------- |
| ADMIN creates a slot   | 201 Created     |
| ADMIN updates a slot   | 200 OK          |
| ADMIN deletes a slot   | 204 No Content  |
| USER reads slots (GET) | 200 OK          |

### Forbidden Scenarios

| Scenario                                                  | Expected Result  |
| --------------------------------------------------------- | ---------------- |
| Unauthenticated client calls any `/api/slots/**` endpoint | 401 Unauthorized |
| Non-admin USER attempts to create a slot                  | 403 Forbidden    |
| Non-admin USER attempts to update a slot                  | 403 Forbidden    |
| Non-admin USER attempts to delete a slot                  | 403 Forbidden    |

### Notes

* Role mapping is handled by the security layer (`User.role = ADMIN` → `ROLE_ADMIN`).
* This rule applies only to slots; other entities (users, bookings, resources) have their own specific access rules.

---

## Rule 10 — Slots May Be Created and Updated Only for Future Time Ranges

Slot time ranges must be in the future at the moment of creation or update.
A slot whose `startTime` is in the past or at the current server time is not allowed to be created or updated.

**Applies to:**
POST /api/slots
PUT /api/slots/{id}

**Time constraints:**

* `startTime` must be strictly greater than current server time (`startTime > now`).
* `endTime` must be strictly after `startTime` (`endTime > startTime`).

**Response on violation:**
`400 Bad Request`

### Allowed Scenarios

| Scenario                                                                        | Expected Result |
| ------------------------------------------------------------------------------- | --------------- |
| Create slot with future `startTime` and `endTime`                               | 201 Created     |
| Update slot so that `startTime` remains in the future and `endTime > startTime` | 200 OK          |

### Forbidden Scenarios

| Scenario                                                                          | Expected Result |
| --------------------------------------------------------------------------------- | --------------- |
| Create slot where `startTime` is in the past                                      | 400 Bad Request |
| Create slot where `startTime` equals current server time                          | 400 Bad Request |
| Update slot so that `startTime` moves into the past or equals current server time | 400 Bad Request |
| Create or update slot where `endTime` is equal to or before `startTime`           | 400 Bad Request |

### Notes

* Validation is performed at the moment of the API call, using server time.
* This rule applies both to new slots and to updates of existing slots.
* Additional rules may also apply (e.g., non-overlap with other slots and admin-only access as defined in Rule 5 and Rule 9).

