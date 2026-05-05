# Concurrency Guide

---

# Part 1 — Designing for Concurrency

A practical reference for building thread-safe systems in Java — mental models, tools, patterns, and common pitfalls.

---

## The Three Distinct Problems

Most concurrency bugs collapse into one of three problems. Each has a different tool.

| Problem | What it means | Tool |
|---|---|---|
| **Visibility** | Thread B reads a stale value written by thread A | `volatile` |
| **Atomicity of a single op** | One read or write must be indivisible | Concurrent collections — see Part 2 |
| **Atomicity of a compound op** | A *sequence* of reads and writes must appear indivisible | `synchronized` / `ReentrantLock` |

`synchronized` solves all three at once but blocks other threads.  
`volatile` solves only visibility — never use it for compound operations.

---

## Mental Model — Two-Layer Defense

```
┌───────────────────────────────────────────────┐
│           Service Layer (outer lock)           │
│   synchronized methods — serializes sequences  │
│   of operations (check-then-act, multi-field   │
│   updates, cross-object consistency)           │
│                                                │
│   ┌─────────────────────────────────────────┐ │
│   │        Model / Repo Layer (inner)        │ │
│   │  Concurrent collections — atomicity of  │ │
│   │  individual field ops even if the outer  │ │
│   │  lock is bypassed                        │ │
│   └─────────────────────────────────────────┘ │
└───────────────────────────────────────────────┘
```

- **Outer lock** handles compound logic — finding a slot, adjusting two balances together, reading then writing.
- **Inner layer** handles individual field safety — model is not completely broken if accessed directly.
- This is **defense-in-depth**, not redundancy. Both layers have a distinct job.

---

## Checklist — Designing a Concurrent System

### Step 1: Identify Shared Mutable State

```
For every field, ask:
  [ ] Can two or more threads reach this field simultaneously?
  [ ] Can any thread write to it?

If both yes → it needs protection.
```

Fields that are:
- `final` pointing to an immutable object → **no protection needed**
- `final` pointing to a concurrent collection → **individual ops safe, compound ops need a lock**
- Non-final, written by any thread → **must be `volatile` or `synchronized`**

---

### Step 2: Assign the Right Tool

```
Singleton INSTANCE:
    → volatile + Double-Checked Locking (DCL)

Status flag (written once or rarely, read many):
    → volatile

Mutable counter shared across threads:
    → AtomicInteger   (not volatile int — increment is 3 ops, not 1)

Shared collection (map, set, queue, list):
    → see Part 2 — Thread-Safe Collections

Sequence of multiple operations that must be atomic:
    → synchronized method / block wrapping the sequence
```

---

### Step 3: Eliminate Check-Then-Act Races

The most common concurrency bug — two separate operations that together form one logical decision.

```java
// BROKEN — check and act are two steps, not one
if (map.get(key) > 0) {
    map.put(key, map.get(key) - 1);   // another thread may have changed it between these lines
}

// FIXED OPTION A — merge into one atomic compute lambda
boolean[] acted = {false};
map.compute(key, (k, v) -> {
    if (v != null && v > 0) {
        acted[0] = true;
        return v - 1;
    }
    return v;
});

// FIXED OPTION B — outer synchronized serializes the sequence
synchronized (this) {
    if (map.get(key) > 0) {
        map.put(key, map.get(key) - 1);
    }
}
```

Other common check-then-act patterns:

```java
// isEmpty() + poll() — BROKEN: poll() returns null if another thread drained first
while (!queue.isEmpty()) {
    Item item = queue.poll();  // may be null → NullPointerException
    process(item);
}
// Fixed — poll() returning null is the atomic empty signal
Item item;
while ((item = queue.poll()) != null) {
    process(item);
}

// containsKey() + put() — BROKEN
if (!map.containsKey(key)) {
    map.put(key, value);  // another thread may have inserted same key
}
// Fixed
map.putIfAbsent(key, value);

// Two synchronized calls are NOT atomic together
if (service.contains(key)) {   // lock acquired and released
    service.remove(key);       // lock acquired and released — key may be gone
}
// Fixed — expose one method that does both under one lock
public synchronized boolean removeIfPresent(K key) {
    if (contains(key)) { remove(key); return true; }
    return false;
}
```

---

### Step 4: Understand Happens-Before

Java Memory Model guarantee:

> **Releasing a monitor happens-before any subsequent acquisition of the same monitor.**

Everything written inside a `synchronized` block is fully visible to the next thread that enters any `synchronized` block on the same object.

```
Thread 1: synchronized { write X }  →  releases lock
Thread 2: synchronized { read X  }  →  acquires lock  →  sees Thread 1's write guaranteed
```

`volatile` gives a weaker guarantee:

> **A write to a volatile variable happens-before every subsequent read of that variable.**

Useful for flags and singleton initialization. Not sufficient for compound operations.

---

### Step 5: Choose Lock Granularity

| Granularity | Throughput | Complexity | Deadlock risk |
|---|---|---|---|
| **Coarse** — one lock for the whole service | Low | Low | None |
| **Fine** — one lock per resource (per floor, per group) | High | High | Possible |

**For LLD design:** coarse locks are correct and expected.  
**For production:** move to fine-grained when profiling shows lock contention.

**Deadlock prevention with fine-grained locks:**  
Always acquire multiple locks in a consistent global order (e.g., by resource ID ascending).  
Never acquire lock A then B in one thread and B then A in another.

---

### Step 6: Singleton — Always Use DCL + volatile

```java
public class Service {

    private static volatile Service INSTANCE;  // volatile — prevents partially-constructed publish

    private Service() { }

    public static Service getInstance() {
        if (INSTANCE == null) {                    // first check — no lock, fast path after init
            synchronized (Service.class) {
                if (INSTANCE == null) {            // second check — handles race on first check
                    INSTANCE = new Service();
                }
            }
        }
        return INSTANCE;
    }
}
```

Without `volatile`: the JVM can reorder the write to `INSTANCE` before the constructor finishes — another thread sees a non-null but partially-constructed object.  
Without the second `if`: two threads that both pass the first check will both construct a new instance.

---

## Common Mistakes

### 1. volatile on a field used in compound ops

```java
// BROKEN — volatile does not make ++ atomic
private volatile int count;
count++;  // read + increment + write — three separate steps

// Fixed
private final AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
```

### 2. Synchronized on a non-shared object

```java
// BROKEN — new Object() is a different lock every time — useless
synchronized (new Object()) { ... }
```

The lock object must be the **same instance** across all threads.

### 3. Returning a mutable reference from a synchronized method

```java
// BROKEN — caller can mutate the list outside the lock
public synchronized List<Item> getItems() {
    return items;
}

// Fixed
public synchronized List<Item> getItems() {
    return Collections.unmodifiableList(items);
}
```

### 4. Missing volatile on Singleton INSTANCE

```java
private static ServiceClass INSTANCE;           // BROKEN — DCL requires volatile
private static volatile ServiceClass INSTANCE;  // Fixed
```

### 5. Iterating ConcurrentHashMap expecting a consistent snapshot

```java
// Weakly consistent — reflects some but not necessarily all concurrent modifications
for (Map.Entry<K, V> entry : concurrentMap.entrySet()) { ... }

// For a point-in-time snapshot, copy under a lock
Map<K, V> snapshot;
synchronized (this) { snapshot = new HashMap<>(concurrentMap); }
for (Map.Entry<K, V> entry : snapshot.entrySet()) { ... }
```

---

## When the Service Lock Is Sufficient

If **all mutations to model objects flow exclusively through synchronized service methods**, you have guaranteed consistency. The service lock serializes all writes and establishes happens-before for all reads that follow.

This breaks down when:
- Model objects are returned via getters and mutated directly by the caller
- Multiple service instances share the same model objects
- Background threads access model objects without going through the service

Guard against these by:
- Returning `unmodifiableList` / `unmodifiableMap` from getters
- Making financial / state fields `private final` on model objects
- Routing all mutations through explicit service methods

---

## synchronized vs ReentrantLock

| Feature | `synchronized` | `ReentrantLock` |
|---|---|---|
| Syntax | Keyword on method/block | Explicit `lock()` / `unlock()` in try-finally |
| Fairness | No | Optional — `new ReentrantLock(true)` |
| Non-blocking attempt | No | `tryLock()` |
| Timed wait | No | `tryLock(timeout, unit)` |
| Multiple condition queues | Single `wait/notify` | Multiple `Condition` objects |
| Performance (modern JVM) | Comparable | Comparable |

**Use `synchronized` by default.** Switch to `ReentrantLock` only when you need try-lock, timed waits, or multiple condition queues.

---

## Quick Decision Tree

```
Is the state shared and mutable?
├── No  → no synchronization needed
└── Yes → is it a single field, written once after construction?
           ├── Yes → volatile is sufficient
           └── No  → is it one collection, each op independent?
                      ├── Yes → pick a concurrent collection (see Part 2)
                      └── No  → synchronized method/block wrapping the sequence
```

---
---

# Part 2 — Thread-Safe Collections

A selection guide for choosing the right concurrent collection based on access pattern and performance trade-offs.

---

## The Selection Trade-offs

Every thread-safe collection balances four axes:

| Axis | What it means |
|---|---|
| **Read throughput** | How fast concurrent reads are |
| **Write cost** | How expensive individual writes are |
| **Consistency** | Snapshot (frozen view) vs weakly-consistent (live, may reflect concurrent changes) |
| **Blocking** | Whether threads wait or proceed without waiting when empty/full |

No single collection wins on all four. Pick the one whose trade-off matches your access pattern.

---

## Decision Tree

```
What is your data structure?
│
├── Map (key → value)
│   ├── Independent per-key ops, high concurrency
│   │   └── ConcurrentHashMap
│   ├── Sorted key order needed
│   │   └── ConcurrentSkipListMap
│   └── Specific impl needed (LinkedHashMap, TreeMap) or low concurrency
│       └── Collections.synchronizedMap
│
├── Set
│   ├── General purpose concurrent set
│   │   └── ConcurrentHashMap.newKeySet()
│   ├── Rare writes, many concurrent iterations
│   │   └── CopyOnWriteArraySet
│   └── Sorted order needed
│       └── ConcurrentSkipListSet
│
├── List
│   ├── Rare writes, many concurrent reads/iterations
│   │   └── CopyOnWriteArrayList
│   └── Frequent writes + reads
│       └── Collections.synchronizedList  (+ manual sync on iteration)
│
└── Queue / Deque
    ├── Non-blocking — consumer moves on if empty
    │   ├── FIFO unbounded          →  ConcurrentLinkedQueue
    │   └── FIFO / LIFO deque       →  ConcurrentLinkedDeque
    ├── Blocking — consumer waits if empty, producer waits if full
    │   ├── FIFO, growable          →  LinkedBlockingQueue
    │   ├── FIFO, fixed size        →  ArrayBlockingQueue
    │   ├── Priority-ordered        →  PriorityBlockingQueue
    │   └── Delayed / scheduled     →  DelayQueue
    └── Direct hand-off (one producer → one consumer, no buffering)
        └── SynchronousQueue
```

---

## Collection Reference

---

### ConcurrentHashMap

**Best for:** High-concurrency key-value store where threads mostly operate on different keys.

**How it works:** Divided into buckets. Threads on different keys rarely contend. Reads are non-blocking. Writes lock only the affected bucket.

```java
Map<String, Integer> map = new ConcurrentHashMap<>();

// Individual ops — atomic, no extra lock needed
map.get(key);
map.put(key, value);
map.remove(key);

// Compound ops — entire lambda executes atomically under the key's bucket lock
map.putIfAbsent(key, 1);                                        // insert only if absent
map.compute(key, (k, v) -> v == null ? 1 : v + 1);             // insert or update
map.computeIfPresent(key, (k, v) -> v + 1);                    // update only if present
map.computeIfAbsent(key, k -> new ArrayList<>());              // insert only if absent, lazy
map.merge(key, 1, Integer::sum);                               // read → merge → write, atomic
```

**Iteration:** Weakly consistent — will not throw `ConcurrentModificationException` but may not reflect all in-flight writes.

| | |
|---|---|
| Read | Fast, non-blocking |
| Write | Low contention — locks one bucket |
| Iteration | Weakly consistent |
| Null keys / values | Not allowed |

---

### ConcurrentHashMap.newKeySet()

**Best for:** General-purpose concurrent `Set`. Identical concurrency properties to `ConcurrentHashMap`.

```java
Set<User> members = ConcurrentHashMap.newKeySet();

members.add(user);       // non-blocking
members.remove(user);    // non-blocking
members.contains(user);  // non-blocking
for (User u : members) { ... }  // weakly consistent iteration
```

Preferred over `Collections.synchronizedSet(new HashSet<>())` for any high-concurrency scenario.

---

### CopyOnWriteArrayList / CopyOnWriteArraySet

**Best for:** Collections that are read or iterated frequently and written rarely — subscriber lists, event listener registries.

**How it works:** Every write creates a **complete copy** of the underlying array. Reads and iterations always operate on the snapshot at the moment the iterator was created — they never block and never see partially-written state.

```java
List<Subscriber> listeners = new CopyOnWriteArrayList<>();

listeners.add(subscriber);     // copies entire array — O(n)
listeners.remove(subscriber);  // copies entire array — O(n)

// Iteration: always safe — snapshot, no ConcurrentModificationException
// A subscriber that removes itself mid-broadcast does not affect the current iteration
for (Subscriber s : listeners) {
    s.onEvent(event);
}
```

| | |
|---|---|
| Read / Iteration | Lock-free, always consistent snapshot |
| Write | O(n) — copies the entire array |
| Memory | Two copies of the array exist during a write |
| Best fit | Read-heavy, write-rare — listener lists, subscriber sets |

**Do NOT use** for write-heavy or large collections — every write is O(n) in time and memory.

---

### ConcurrentLinkedQueue

**Best for:** Non-blocking FIFO queue between producers and consumers where no thread should ever wait.

**How it works:** Lock-free linked list using CAS. `poll()` returns `null` when empty — that `null` is the atomic empty signal.

```java
Queue<Message> queue = new ConcurrentLinkedQueue<>();

queue.offer(message);          // add to tail — always succeeds, non-blocking
Message msg = queue.poll();    // remove from head — returns null if empty, non-blocking
Message msg = queue.peek();    // read head without removing — returns null if empty
```

**Correct drain pattern** — poll returning null is the only atomic empty check:

```java
Message msg;
while ((msg = queue.poll()) != null) {
    deliver(msg);
}
```

| | |
|---|---|
| Throughput | Very high — lock-free CAS |
| Blocking | Never — consumer gets null and moves on |
| Bounded | No — grows without limit |
| Best fit | Event queues, fire-and-forget message passing |

**Do NOT use** when the consumer should wait for work — use `LinkedBlockingQueue` instead.

---

### LinkedBlockingQueue

**Best for:** Classic thread pool work queue. Producers add tasks; worker threads block and wait when the queue is empty.

**How it works:** Two separate locks — one for the head (consumer) and one for the tail (producer) — so producers and consumers rarely contend.

```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);  // bounded
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();      // unbounded

queue.put(task);                                   // blocks if full (bounded only)
queue.offer(task);                                 // returns false immediately if full
queue.offer(task, 500, TimeUnit.MILLISECONDS);     // waits up to 500ms

Task t = queue.take();                             // blocks until available — key consumer op
Task t = queue.poll();                             // returns null immediately if empty
Task t = queue.poll(500, TimeUnit.MILLISECONDS);   // waits up to 500ms

int n = queue.drainTo(list);                       // atomically moves all elements — batch consume
```

`Executors.newFixedThreadPool` uses `LinkedBlockingQueue` internally — worker threads loop on `take()`.

| | |
|---|---|
| Producer blocks | Only when bounded and full |
| Consumer blocks | Yes — `take()` waits when empty |
| Throughput | High — separate head/tail locks |
| Best fit | Thread pools, work queues, producer-consumer pipelines |

---

### ArrayBlockingQueue

**Best for:** Fixed-size bounded queue where capacity is a hard constraint.

**How it works:** Backed by a fixed array. Single lock (unlike `LinkedBlockingQueue`'s two). Producer blocks when full; consumer blocks when empty.

```java
BlockingQueue<Request> queue = new ArrayBlockingQueue<>(50);

queue.put(request);   // blocks until space is available
queue.take();         // blocks until an element is available
```

| | |
|---|---|
| Capacity | Fixed — set at construction, cannot grow |
| Lock | Single lock — lower throughput than `LinkedBlockingQueue` under high contention |
| Memory | Pre-allocated array — predictable, cache-friendly |
| Best fit | Rate limiting, fixed-capacity work queues |

**Prefer over LinkedBlockingQueue** when capacity must be strictly fixed or memory predictability matters.

---

### ConcurrentSkipListMap / ConcurrentSkipListSet

**Best for:** Sorted concurrent map or set — range queries, floor/ceiling lookups needed concurrently.

**How it works:** Probabilistic skip list with O(log n) for get/put/remove. All operations lock-free using CAS.

```java
NavigableMap<Long, Event> timeline = new ConcurrentSkipListMap<>();

timeline.put(event.getTimestamp(), event);
timeline.headMap(cutoffTime).values();   // all events before cutoff
timeline.floorKey(timestamp);           // largest key ≤ timestamp
timeline.ceilingKey(timestamp);         // smallest key ≥ timestamp
```

| | |
|---|---|
| Order | Sorted by natural order or Comparator |
| Read / Write | O(log n) |
| Lock-free | Yes — CAS |
| Best fit | Leaderboards, event timelines, range queries |

**Do NOT use** for unordered data — `ConcurrentHashMap` is faster at O(1).

---

### Collections.synchronizedMap / synchronizedList / synchronizedSet

**Best for:** Wrapping a specific implementation (`LinkedHashMap` for LRU, `TreeMap` for sorted) when concurrency is low, or when migrating legacy code.

**How it works:** Wraps every method in a `synchronized` block on the collection itself. One lock, full serialization.

```java
Map<K, V>  map  = Collections.synchronizedMap(new LinkedHashMap<>());
List<Item> list = Collections.synchronizedList(new ArrayList<>());
```

**Critical — iteration must be manually synchronized:**

```java
// BROKEN — iteration is not covered by the per-method wrapper
for (Item item : synchronizedList) { ... }

// Fixed
synchronized (synchronizedList) {
    for (Item item : synchronizedList) { ... }
}
```

| | |
|---|---|
| Lock | One lock for all ops — full serialization |
| Throughput | Low under contention |
| Iteration | Must manually synchronize |
| Best fit | Low concurrency, specific impl needed (LinkedHashMap, TreeMap) |

---

## Use-Case Mapping

| Use Case | Collection | Reason |
|---|---|---|
| Subscriber / listener registry | `CopyOnWriteArraySet` | Rare writes, broadcast iteration is always safe |
| Parking spot counters per type | `ConcurrentHashMap<VehicleType, AtomicInteger>` | Per-key atomic increment/decrement |
| User balance ledger | `ConcurrentHashMap<User, Double>` + `merge` | Per-user atomic read-modify-write |
| Group membership | `ConcurrentHashMap.newKeySet()` | Concurrent add / remove / contains |
| Message queue (pub-sub, fire-and-forget) | `ConcurrentLinkedQueue` | Non-blocking, poll-based drain |
| Thread pool work queue | `LinkedBlockingQueue` | Blocking `take()`, optional bounded back-pressure |
| LRU cache backing map | `HashMap` under `synchronized` service | Eviction is a compound op — needs outer lock |
| Event timeline / leaderboard | `ConcurrentSkipListMap` | Sorted, concurrent range queries |
| Config / registry (written once, read many) | `Collections.unmodifiableMap` after init | Immutable after setup — no lock needed at all |
| Session store (ID → object) | `ConcurrentHashMap` | High-read, independent keys |
| Rate limiter token bucket | `AtomicInteger` or `Semaphore` | Single counter, CAS-based |
| In-memory task queue (bounded) | `ArrayBlockingQueue` | Hard capacity limit, predictable memory |
| Expense list per group | `Collections.synchronizedList` | Sequential appends, infrequent reads |

---

## Null Handling

None of the `java.util.concurrent` collections allow `null`. A `null` return from `poll()` or `get()` means "empty" or "absent" — not a stored null. This is intentional and is what enables `poll() == null` as an atomic empty check.

| Collection | Null keys | Null values |
|---|---|---|
| `ConcurrentHashMap` | ❌ | ❌ |
| `ConcurrentSkipListMap` | ❌ | ❌ |
| `ConcurrentLinkedQueue` | ❌ | ❌ |
| `LinkedBlockingQueue` | ❌ | ❌ |
| `ArrayBlockingQueue` | ❌ | ❌ |
| `CopyOnWriteArrayList` | ✅ | ✅ |
| `CopyOnWriteArraySet` | ✅ | — |
| `Collections.synchronizedMap(HashMap)` | ✅ (inherits HashMap) | ✅ |

---

## Synchronized Wrapper vs Concurrent Collection

| Criterion | `Collections.synchronized*` | `java.util.concurrent.*` |
|---|---|---|
| Lock granularity | One lock for all ops | Per-bucket / lock-free CAS |
| Compound ops | Everything under one coarse lock | Dedicated atomic methods (`compute`, `merge`) |
| Iteration | Must manually synchronize | Weakly consistent, no external sync needed |
| Throughput under contention | Low | High |
| Null support | Depends on wrapped collection | Not allowed |
| Specific impl (LinkedHashMap, TreeMap) | Yes | No |
| Use when | Low concurrency, specific impl needed | High concurrency, general use |

---

## Summary Card

```
High-concurrency map                  →  ConcurrentHashMap
Sorted concurrent map                 →  ConcurrentSkipListMap
Concurrent set                        →  ConcurrentHashMap.newKeySet()
Read-heavy list / set (listeners)     →  CopyOnWriteArrayList / CopyOnWriteArraySet
Sorted concurrent set                 →  ConcurrentSkipListSet
Non-blocking queue (events)           →  ConcurrentLinkedQueue
Blocking work queue (thread pool)     →  LinkedBlockingQueue
Fixed-size bounded queue              →  ArrayBlockingQueue
Single mutable counter                →  AtomicInteger / AtomicLong
Single mutable reference              →  AtomicReference
Low-concurrency wrap / specific impl  →  Collections.synchronized*
Immutable after initialization        →  Collections.unmodifiableMap / List / Set
```
