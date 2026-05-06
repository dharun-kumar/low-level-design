# Chain of Responsibility Pattern

## What is it?
Passes a request along a **chain of handlers**. Each handler decides to process the request or forward it to the next. The sender doesn't know which handler will ultimately handle it.

---

## Example — HTTP Request Pipeline

An incoming request is validated through three sequential gates: Authentication → Authorization → Application. Each gate either terminates (on failure) or forwards (on success).

```
Request
   │
   ▼
AuthenticationHandler ──(valid user)──▶ AuthorizationHandler ──(admin)──▶ ApplicationHandler
       │                                        │
  (null user)                             (non-admin)
  terminate                               terminate
```

**Chain setup:**
```java
authentication.setNext(authorization);
authorization.setNext(application);
authentication.handle(request);
```

| Handler | Check | On Pass | On Fail |
|---|---|---|---|
| `AuthenticationHandler` | user != null | forward | terminate |
| `AuthorizationHandler` | role == "admin" | forward | terminate |
| `ApplicationHandler` | — | process + forward | — |

---

## Key Roles

| Role | Class |
|---|---|
| Handler (abstract) | `BaseRequestHandler` |
| Concrete Handlers | `AuthenticationHandler`, `AuthorizationHandler`, `ApplicationHandler` |
| Request | `Request` |
| Client | `CRDemo` |

---

## Design Decisions
- **`forward()`** is in the base class — concrete handlers call `forward(request)` instead of `nextHandler.handle(request)` directly, so the null check is centralised.
- **Chain termination** — `ApplicationHandler` has no next handler; `forward()` silently does nothing when `nextHandler == null`.
- **Each handler has one responsibility** — auth, authz, and processing are fully decoupled.

---

## When to Use
- Multiple objects may handle a request but the handler isn't known upfront.
- You want to issue a request to one of several handlers **without hardcoding** the chain.
- Handlers should be assembled dynamically in different orders.

## Real-World Examples
- Servlet filters / middleware pipelines (Spring `FilterChain`)
- Logging levels (DEBUG → INFO → WARN → ERROR)
- UI event propagation (child → parent → window)
- Exception handling chains (try-catch-finally)
