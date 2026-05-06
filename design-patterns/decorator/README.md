# Decorator Pattern

## What is it?
Attaches **additional behaviour** to an object dynamically by wrapping it. Decorators implement the same interface as the component they wrap, so they're interchangeable with the original — and stackable.

**Prefer over subclassing** when behaviour combinations would cause a class explosion (e.g., `WarrantyLaptop`, `ProtectedLaptop`, `WarrantyAndProtectedLaptop`…).

---

## Example — Laptop Purchase Add-ons

A `Macbook` is the base product. Each add-on (extended warranty, complete protection) is a decorator that wraps the laptop, augmenting its description and price without touching the original class.

```
«interface» Laptop
────────────────────────────
+ getDescription(): String
+ getPrice(): double
        ▲
        │ implements
   ┌────┴─────────────────────┐
   │                          │ (abstract)
 Macbook               LaptopDecorator
 (base: ₹69,000)       ─────────────────────────
                        - decoratedLaptop: Laptop  ◀── wraps any Laptop
                                ▲
                                │ extends
                  ┌─────────────┴──────────────────┐
                  │                                 │
       ExtendedWarrantyLaptop          CompleteProtectionLaptop
       (+₹1,499 | +3yr warranty)       (+₹999 | +1yr protection)
```

**Stacking decorators:**
```java
Laptop base       = new Macbook();                          // ₹69,000
Laptop warranted  = new ExtendedWarrantyLaptop(base);       // ₹70,499
Laptop protected  = new CompleteProtectionLaptop(warranted);// ₹71,498
```
Each wrapper calls `decoratedLaptop.getPrice()` and adds its own cost — the chain resolves recursively.

---

## Key Roles

| Role | Class |
|---|---|
| Component Interface | `Laptop` |
| Concrete Component | `Macbook` |
| Abstract Decorator | `LaptopDecorator` |
| Concrete Decorators | `ExtendedWarrantyLaptop`, `CompleteProtectionLaptop` |
| Client | `DecoratorDemo` |

---

## Design Decisions
- **`LaptopDecorator` holds a `Laptop` reference** — not a `Macbook`. It can wrap any `Laptop`, including other decorators, enabling arbitrary stacking.
- **Open/Closed** — new add-ons (e.g., `AccidentalDamageLaptop`) are new classes; `Macbook` and existing decorators are never touched.

---

## When to Use
- You need to add responsibilities to objects **at runtime**, not at compile time.
- Subclassing would produce too many combinations.
- Behaviours should be **composable** and **stackable**.

## Real-World Examples
- `BufferedReader(new FileReader(...))` — Java I/O streams
- HTTP middleware (logging → auth → rate-limit → handler)
- Pizza/burger configurators (base + toppings)
- `Collections.unmodifiableList()`, `Collections.synchronizedList()`
