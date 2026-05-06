# Singleton Pattern

## What is it?
Ensures a class has **only one instance** and provides a global access point to it. The constructor is private; a static method controls instance creation.

---

## Example — Thread-Safe Singleton (DCL)

Uses **Double-Checked Locking (DCL)** with `volatile` — the industry-standard thread-safe Singleton in Java.

```java
class Singleton {
    private static volatile Singleton instance;  // volatile: cross-thread visibility

    private Singleton() {}   // private: no external instantiation

    public static Singleton getInstance() {
        if (instance == null) {              // 1st check — avoid lock on every call
            synchronized (Singleton.class) {
                if (instance == null) {     // 2nd check — only one thread creates
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

---

## Why Two Checks?

| Without DCL | With DCL |
|---|---|
| `synchronized` on every `getInstance()` call | Lock acquired only once — during first creation |
| High contention under load | Near-zero overhead after initialisation |

**Why `volatile`?**
Object creation (`new Singleton()`) is not atomic — it's: allocate memory → initialise fields → assign reference. Without `volatile`, the JVM can reorder steps 2 and 3. Another thread may see a non-null but partially constructed object. `volatile` prevents this reordering.

---

## Alternatives

| Approach | Thread-Safe | Lazy | Notes |
|---|---|---|---|
| DCL + `volatile` | ✅ | ✅ | Standard; verbose |
| Static inner class (Bill Pugh) | ✅ | ✅ | Cleaner; preferred |
| Enum Singleton | ✅ | ❌ | Serialization-safe; no lazy init |
| `synchronized getInstance()` | ✅ | ✅ | Safe but slow under contention |

**Bill Pugh (recommended):**
```java
class Singleton {
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    public static Singleton getInstance() { return Holder.INSTANCE; }
}
```

---

## When to Use
- Exactly **one shared instance** is needed (config, connection pool, logger, registry).
- The instance is **resource-heavy** and should be created only once.

## Real-World Examples
- `Runtime.getRuntime()` — JVM runtime instance
- Spring `ApplicationContext` — single bean container per app
- Database connection pools (`HikariPool`)
- Logger instances (`LogManager.getLogger`)
