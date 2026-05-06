# State Pattern

## What is it?
Allows an object to **alter its behaviour when its internal state changes**. The object appears to change its class. Each state is encapsulated in its own class with its own behaviour — no large if-else chains.

---

## Example — Monitor Settings

A monitor has a menu button that cycles through settings (Brightness → Contrast → Sharpness → Brightness…) and an up button that adjusts the current setting. The active `Setting` state handles both buttons.

```
Monitor
────────────────────────────
- setting: Setting          ◀── holds current state (changes on menuButton())
+ menuButton()  → setting.next(this)     // transitions to next state
+ upButton()    → setting.increase()     // delegates adjustment to current state
+ setSetting(Setting)

«interface» Setting
───────────────────────
+ next(Monitor)        // transition to next state
+ increase()           // adjust current setting value

        ▲ implements
   ┌────┼──────────────┐
   │    │              │
Brightness  Contrast  Sharpness
(Singleton) (Singleton)(Singleton)
next() →    next() →   next() →
Contrast    Sharpness  Brightness
```

**Transition cycle:**
```
menuButton() → Contrast   (was Brightness)
menuButton() → Sharpness  (was Contrast)
menuButton() → Brightness (was Sharpness)
upButton()   → Sharpness increased to 51
```

---

## Key Roles

| Role | Class |
|---|---|
| Context | `Monitor` |
| State Interface | `Setting` |
| Concrete States | `Brightness`, `Contrast`, `Sharpness` |
| Client | `StateDemo` |

---

## Design Decisions
- **States are Singletons** — `Brightness.getInstance()` etc. Each state holds its own `value` field, so a single instance per state is sufficient and avoids repeated allocation.
- **State transitions are owned by each state** — `Brightness.next()` knows it transitions to `Contrast`. The `Monitor` (context) doesn't contain any transition logic; it just calls `setting.next(this)`.
- **No if-else in Monitor** — adding a new setting (e.g., `ColourTemp`) only requires a new `Setting` class and updating the adjacent states' `next()` methods.

---

## When to Use
- An object's behaviour **depends heavily on its state** and must change at runtime.
- You have **large conditional blocks** branching on state enum/field.
- State-specific behaviour and transitions should be **localised** — not spread across the context.

## Real-World Examples
- Elevator states (Idle / MovingUp / MovingDown)
- Order lifecycle (Placed → Confirmed → Shipped → Delivered → Cancelled)
- Traffic light controller (Red → Green → Yellow → Red)
- TCP connection states (Listen / SynReceived / Established / Closed)
