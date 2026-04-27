# Pub-Sub System

## Problem Statement

Design a Publish-Subscribe messaging system where publishers send messages to named topics and subscribers receive those messages asynchronously. Publishers and subscribers are fully decoupled — neither knows about the other.

This is a classic low-level design problem that tests asynchronous design, thread safety, and the Observer pattern at scale.

---

## Requirements

### Functional
- `createTopic(name)` — Register a new topic in the system.
- `publish(topicName, payload)` — Post a message to a topic. All current subscribers receive it asynchronously.
- `subscribe(topicName, subscriber)` — Register a subscriber to receive future messages on a topic.
- `unSubscribe(topicName, subscriber)` — Remove a subscriber from a topic.
- `shutdown()` — Drain all pending messages and cleanly stop the system.

### Non-Functional
- Message delivery must be **asynchronous** — publishers must not block waiting for subscribers.
- The system must be **thread-safe** — concurrent publish, subscribe, and unsubscribe calls must not corrupt state.
- A subscriber must **never receive the same message twice** from duplicate registration.
- All pending messages must be **drained on shutdown** — no message loss.

---

## UML Diagram

```
                    ┌──────────────────────────────────────────┐
                    │         PubSubService <<Singleton>>      │
                    ├──────────────────────────────────────────┤
                    │ - INSTANCE: PubSubService <<volatile>>   │
                    │ - topics: Map<String, Topic>             │
                    │ - executorService: ExecutorService       │
                    ├──────────────────────────────────────────┤
                    │ + getInstance(): PubSubService           │
                    │ + createTopic(topicName: String): void   │
                    │ + publish(topicName, payload): void      │
                    │ + subscribe(topicName, subscriber): void │
                    │ + unSubscribe(topicName, subscriber): void│
                    │ + shutdown(): void                       │
                    └──────────────┬───────────────────────────┘
                                   │ manages 0..*
                                   ▼
                    ┌──────────────────────────────────────┐
                    │               Topic                  │
                    ├──────────────────────────────────────┤
                    │ - topicName: String                  │
                    │ - messages: ConcurrentLinkedQueue    │
                    │ - subscribers: CopyOnWriteArraySet   │
                    ├──────────────────────────────────────┤
                    │ + addSubscriber(subscriber): void    │
                    │ + removeSubscriber(subscriber): void │
                    │ + addMessage(message): void          │
                    │ + broadCastMessages(): void          │
                    └───────┬──────────────┬───────────────┘
                            │ queues       │ notifies
                            ▼             ▼
              ┌─────────────────┐   ┌─────────────────────┐
              │     Message     │   │   <<interface>>      │
              ├─────────────────┤   │     Subscriber       │
              │ - payload:String│   ├─────────────────────┤
              │ - timeStamp:long│   │ + onMessage(Message) │
              ├─────────────────┤   └──────────┬──────────┘
              │ + getPayload()  │              │ implements
              │ + getTimeStamp()│    ┌─────────┴──────────┐
              │ + toString()    │    ▼                    ▼
              └─────────────────┘  SportsSubscriber  WeatherSubscriber
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `Message` | Immutable value object carrying a string payload and wall-clock timestamp. Shared freely across threads since it cannot be mutated. |
| `Subscriber` | Interface defining the single callback `onMessage`. Any class implementing it can receive messages from any topic. |
| `SportsSubscriber` / `WeatherSubscriber` | Concrete subscribers demonstrating that the same subscriber can listen to multiple topics simultaneously. |
| `Topic` | Owns the message queue and subscriber set for a single topic. Handles thread-safe enqueue and broadcast without explicit locks by using concurrent collections. |
| `PubSubService` | Singleton facade. Manages the topic registry, dispatches broadcasts to a thread pool on each publish, and drains messages on shutdown. |

---

## Design Decisions

### 1. `Subscriber` as Interface
Any class can become a subscriber by implementing a single method — `onMessage(Message)`. This follows the **Interface Segregation** and **Open/Closed** principles: new subscriber types (email, SMS, webhook) require no changes to any existing class.

### 2. `Message` Immutability
All fields in `Message` are `final`. Once constructed it is safe to share across all subscriber threads without synchronization. Mutable messages would require defensive copying on every delivery.

### 3. `ConcurrentLinkedQueue` for Message Queue
`LinkedList` is not thread-safe — concurrent `add` and `poll` corrupt its internal pointers. `ConcurrentLinkedQueue` is a lock-free, thread-safe queue. `poll()` is atomic, so `broadCastMessages` can drain with `while((msg = messages.poll()) != null)` without any additional locking.

### 4. `CopyOnWriteArraySet` for Subscribers
`HashSet` is not thread-safe for concurrent iteration. When `broadCastMessages` iterates the set, a concurrent `subscribe` or `unSubscribe` call would cause `ConcurrentModificationException`. `CopyOnWriteArraySet` takes a snapshot of the backing array on every write — iteration always sees a stable view. This is the right trade-off here because subscriber changes are rare compared to message delivery frequency. It also provides set semantics — duplicate `subscribe` calls are silently ignored.

### 5. Immediate Async Dispatch on Publish
An earlier design used `ScheduledExecutorService` to poll all topic queues every second. This introduced a fixed 1-second latency floor on every message. The current design submits `topic::broadCastMessages` to the executor directly inside `publish`, so delivery is near-instant. The publisher thread is never blocked — it enqueues the message and returns immediately.

The executor is sized to `availableProcessors`, so multiple topics can broadcast concurrently. For a single high-volume topic, multiple `broadCastMessages` tasks may run concurrently — `ConcurrentLinkedQueue.poll()` being atomic ensures each message is delivered exactly once even under concurrent drains.

### 6. Singleton with `volatile` Double-Checked Locking
`PubSubService` is a Singleton because the executor and topic registry are shared system resources. `volatile` on `INSTANCE` is required — without it, the JVM can reorder object construction and return a partially initialized instance to a second thread that passes the outer null check.

### 7. `computeIfPresent` for Publish / Subscribe / Unsubscribe
All three operations use `ConcurrentHashMap.computeIfPresent` rather than a separate `get` + conditional `put`. This makes the check-and-mutate atomic at the map level, avoiding a window where a topic could be deleted between the check and the mutation.

### 8. Drain on Shutdown
`shutdown()` iterates all topics and calls `broadCastMessages()` on the calling thread before stopping the executor. This ensures messages that were enqueued but not yet dispatched to the executor are delivered. Without this drain, any messages published after the last executor run and before `shutdown()` would be silently dropped.

---

## Complexity

| Operation | Time | Notes |
|---|---|---|
| `createTopic` | O(1) | `ConcurrentHashMap.putIfAbsent` |
| `publish` | O(1) | Enqueue + executor submit |
| `subscribe` / `unSubscribe` | O(n) | `CopyOnWriteArraySet` write copies backing array |
| `broadCastMessages` | O(M × S) | M messages × S subscribers |
| `shutdown` drain | O(M × S) | Same as broadcast, over all topics |
