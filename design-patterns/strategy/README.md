# Strategy Pattern

## What is it?
Defines a **family of algorithms**, encapsulates each one, and makes them **interchangeable at runtime**. The context delegates behaviour to a strategy object instead of implementing it directly.

**Difference from State** — Strategy is chosen by the *client* and represents *how* to do something. State transitions automatically and represents *what the object currently is*.

---

## Example — Navigation Routes

A `Navigator` finds directions between two locations. The routing algorithm (road, air) is pluggable — swap it at runtime without changing the navigator.

```
Navigator (Context)
──────────────────────────────
- strategy: Strategy          ◀── holds current strategy
+ Navigator(Strategy)
+ changeStrategy(Strategy)    // swap at runtime
+ fetchDirections(src, dest)  // delegates to strategy.route()

«interface» Strategy
──────────────────────────────
+ route(source, destination)
        ▲
        │ implements
   ┌────┴──────────────┐
   │                   │
RoadStrategy        AirStrategy
"350 Kms, 6h 30m    "350 Kms, 1h
 by Road"            by Air"
```

**Runtime swap:**
```java
Navigator maps = new Navigator(new RoadStrategy());
maps.fetchDirections("Chennai", "Bangalore"); // Road directions

maps.changeStrategy(new AirStrategy());
maps.fetchDirections("Chennai", "Bangalore"); // Air directions
```

---

## Key Roles

| Role | Class |
|---|---|
| Context | `Navigator` |
| Strategy Interface | `Strategy` |
| Concrete Strategies | `RoadStrategy`, `AirStrategy` |
| Client | `StrategyDemo` |

---

## Design Decisions
- **`changeStrategy()` at runtime** — the context is not locked to a strategy at construction time. Behaviour can change dynamically (e.g., user switches from road to flight mode).
- **Context owns no routing logic** — `fetchDirections` is purely a delegation. Zero changes to `Navigator` when adding a new strategy like `TrainStrategy`.
- **Open/Closed** — new algorithms extend the family via a new class; existing code is untouched.

---

## When to Use
- You have **multiple variants of an algorithm** and want to switch between them at runtime.
- You want to eliminate **large conditionals** selecting behaviour based on type.
- Algorithm implementations should be **isolated** from the code that uses them.

## Real-World Examples
- Sorting algorithms (`Comparator` passed to `Collections.sort()`)
- Payment methods (CreditCard / UPI / NetBanking strategy)
- Compression algorithms (ZIP / GZIP / LZ4)
- Elevator dispatch strategies (`NearestElevator`, `RoundRobin`, `ZoneBased`)
