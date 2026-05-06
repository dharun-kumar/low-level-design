# Adapter Pattern

## What is it?
Converts the interface of an existing class into another interface that the client expects. Lets incompatible interfaces work together **without modifying the original class**.

---

## Example — REST / SOAP API Adapter

A new system expects a unified `API` interface returning JSON. A legacy service only speaks SOAP XML. The `SOAPAdapter` wraps the legacy service and translates its response.

```
«interface» API
──────────────────
+ getResponse(): String
        ▲
        │ implements
   ┌────┴──────────┐
   │               │
  REST          SOAPAdapter  ──── wraps ────▶  SOAP (legacy)
  (JSON)         (JSON)                        + getXMLResponse(): String
```

**Flow:**
```
Client → SOAPAdapter.getResponse()
              → SOAP.getXMLResponse()   (XML response)
              → transformResponse()     (XML → JSON)
         ← JSON (same as REST)
```

---

## Key Roles

| Role | Class |
|---|---|
| Target Interface | `API` |
| Adaptee | `SOAP` (legacy, incompatible) |
| Adapter | `SOAPAdapter` |
| Concrete Target | `REST` |
| Client | `AdapterDemo` |

---

## When to Use
- Integrating a **legacy system** whose interface can't be changed.
- Using a third-party library that doesn't match your interface.
- You want to reuse existing code without modifying it.

## Real-World Examples
- Payment gateway wrappers (normalise PayPal/Stripe/Razorpay to one interface)
- Log framework adapters (SLF4J wrapping Log4j/Logback)
- `Arrays.asList()` — adapts an array to a `List`
