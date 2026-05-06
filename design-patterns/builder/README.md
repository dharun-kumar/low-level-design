# Builder Pattern

## What is it?
Constructs a complex object **step by step**. Separates the construction logic from the final object, allowing the same construction process to create different configurations.

Solves the **telescoping constructor problem** — instead of overloaded constructors for every combination of optional fields, you chain only the fields you need.

---

## Example — HTTP Request Builder

`HttpRequest` has multiple optional fields (method, headers, parameters, timeout). The builder lets you set only what's needed and produces an immutable object via `build()`.

```
HttpRequestBuilder
──────────────────────────
+ url: String  (required)
+ method: String
+ headers: Map<String, String>
+ parameters: Map<String, String>
+ timeout: int
──────────────────────────
+ method(String): HttpRequestBuilder      ◀── returns this (fluent)
+ header(String, String): HttpRequestBuilder
+ parameter(String, String): HttpRequestBuilder
+ timeout(int): HttpRequestBuilder
+ build(): HttpRequest                    ◀── produces immutable object
```

**Usage:**
```java
HttpRequest request = new HttpRequestBuilder("https://github/api/repo")
    .method("GET")
    .header("Content-Type", "application/json")
    .parameter("user_name", "dharun-kumar")
    .timeout(5000)
    .build();
```

---

## Key Roles

| Role | Class |
|---|---|
| Product | `HttpRequest` (immutable, constructed via builder) |
| Builder | `HttpRequestBuilder` |
| Client | `BuilderDemo` |

---

## Design Decisions
- **`url` is required** — passed to the constructor, not a setter. Mandatory fields go in the builder's constructor.
- **Fields are `public` in builder** — `HttpRequest` constructor reads them directly, avoiding getter boilerplate. Acceptable when builder is an inner class or closely coupled.
- **`build()` is the only way** to create `HttpRequest` — no public constructor on the product.

---

## When to Use
- Object has **many optional parameters** and you want to avoid overloaded constructors.
- Construction must happen **step by step** and the order matters.
- You need the final object to be **immutable**.

## Real-World Examples
- `StringBuilder`, `UriBuilder`, `OkHttp.Request.Builder`
- `AlertDialog.Builder` in Android
- `ProcessBuilder`, `HttpClient.Builder` in Java standard library
