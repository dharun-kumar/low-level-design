# Factory Method Pattern

## What is it?
Defines an interface for creating an object, but lets **subclasses decide which class to instantiate**. The creator defers object creation to its subclasses via a factory method.

**Difference from Abstract Factory** — Factory Method creates *one* product through inheritance. Abstract Factory creates *families* of products through composition.

---

## Example — Payment Processor

The client works with a `Creator` that produces a `Product` (payment processor). The concrete creator (`PayPalProcessor`, `StripeProcessor`) decides which processor to instantiate — the client never calls `new PayPal()` or `new Stripe()` directly.

```
«interface» Creator                   «interface» Product
────────────────────                  ──────────────────────
+ createProcessor(): Product          + pay(amount): Payment
        ▲                                     ▲
        │ implements                           │ implements
   ┌────┴──────────────┐              ┌────────┴──────────┐
   │                   │              │                   │
PayPalProcessor   StripeProcessor   PayPal            Stripe
   │                   │
   └── creates PayPal  └── creates Stripe
```

**Client code is provider-agnostic:**
```java
Creator processor = new PayPalProcessor();   // or StripeProcessor
Product gateway   = processor.createProcessor();
gateway.pay(417.99);
```
Switching providers = swap one line. Zero changes to the rest.

---

## Key Roles

| Role | Class |
|---|---|
| Creator Interface | `Creator` |
| Concrete Creators | `PayPalProcessor`, `StripeProcessor` |
| Product Interface | `Product` |
| Concrete Products | `PayPal`, `Stripe` |
| Value Object | `Payment` (transactionID, amount, status, provider) |
| Client | `FactoryMethodDemo` |

---

## Design Decisions
- **`Payment` is a value object** — immutable, UUID-identified. Each `pay()` call produces a new `Payment` record; history is kept inside the processor (`transactions` list).
- **`Creator` only declares `createProcessor()`** — it doesn't call `pay()`. The client fetches the product and drives it. This keeps the factory focused on creation only.

---

## When to Use
- The exact type of object to create isn't known until runtime.
- You want **subclasses to control** which product is instantiated.
- Adding a new product variant requires **only a new creator + product class**, zero changes to existing code.

## Real-World Examples
- `Calendar.getInstance()` — returns the right calendar for the locale
- JDBC `DriverManager.getConnection()` — returns DB-specific connection
- Spring `BeanFactory` — creates beans by type at runtime
- Logging frameworks (`LoggerFactory.getLogger(...)`)
