# Observer Pattern

## What is it?
Defines a **one-to-many dependency** between objects. When the subject changes state, all registered observers are notified automatically. Also known as **Publish-Subscribe** (when the subject doesn't know observer types).

---

## Example — Stock Broker

Investors subscribe to a `StockBroker`. When stock prices update, all registered investors are notified. Investors can subscribe or unsubscribe at any time.

```
«interface» Subject                     «interface» Observer
────────────────────────────            ──────────────────────
+ addObserver(Observer)                 + update(message)
+ removeObserver(Observer)                      ▲
+ notifyObservers(message)                      │ implements
        ▲                                    Investor
        │ implements                         - name: String
    StockBroker                              + update() → prints alert
    - observers: List<Observer>
    + updateStockPrice(message)
         → notifyObservers()
```

**Flow:**
```
stockBroker.updateStockPrice("Nifty50 at 25333")
   → investor1.update(...)   → "dharun stock prices updated → Nifty50 at 25333"
   → investor2.update(...)   → "kumar stock prices updated → Nifty50 at 25333"

stockBroker.removeObserver(investor2)
stockBroker.updateStockPrice("Nifty50 at 24876")
   → investor1.update(...)   → "dharun stock prices updated → Nifty50 at 24876"
```

---

## Key Roles

| Role | Class |
|---|---|
| Subject Interface | `Subject` |
| Concrete Subject | `StockBroker` |
| Observer Interface | `Observer` |
| Concrete Observer | `Investor` |
| Client | `ObserverDemo` |

---

## Design Decisions
- **`Subject` interface** keeps `StockBroker` decoupled from concrete observers — it only knows `Observer`.
- **`removeObserver`** enables dynamic unsubscription at runtime — observer list is mutable.
- **Push model** — the subject pushes data (`message`) to observers. Alternative is the pull model where observers call back to fetch state.

---

## When to Use
- A change in one object requires updating **many others**, and you don't know how many.
- Objects should be able to **subscribe and unsubscribe** at runtime.
- You want **loose coupling** between the publisher and its consumers.

## Real-World Examples
- Event listeners in UI frameworks (`onClick`, `onChange`)
- Kafka / message queue consumers (producer notifies, consumers react)
- Spring `ApplicationEventPublisher` / `@EventListener`
- `java.util.Observable` (legacy), `Flow.Publisher` (reactive streams)
