# LRU Cache

## Problem Statement

Design a cache with a fixed capacity that evicts the **Least Recently Used (LRU)** entry when the cache is full and a new entry needs to be inserted. The cache must support fast `get`, `put`, and `remove` operations.

This is a classic low-level design problem that tests the ability to combine two data structures to achieve O(1) time complexity for all operations.

---

## Requirements

### Functional
- `get(key)` — Return the value associated with the key if it exists; otherwise return `null`. Marks the entry as recently used.
- `put(key, value)` — Insert or update a key-value pair. If the cache is at capacity, evict the least recently used entry before inserting.
- `remove(key)` — Explicitly remove a key-value pair from the cache.

### Non-Functional
- All operations must run in **O(1)** time.
- The cache must be **thread-safe** — safe for concurrent access.
- Capacity is fixed at construction time and cannot change.

---

## UML Diagram

```
┌──────────────────────────────────────────┐
│         LRUCacheService<K, V>            │
├──────────────────────────────────────────┤
│ - CAPACITY: int                          │
│ - CACHE: Map<K, Node<K,V>>              │
│ - DOUBLY_LINKED_LIST: DoublyLinkedList   │
│         <K,V>                            │
├──────────────────────────────────────────┤
│ + get(key: K): V                         │
│ + put(key: K, value: V): void            │
│ + remove(key: K): void                   │
└────────────┬──────────┬──────────────────┘
             │          │
         composes   maps via CACHE
             │          │
             ▼          ▼
┌───────────────────┐  ┌──────────────────────┐
│ DoublyLinkedList  │  │      Node<K, V>       │
│     <K, V>        │  ├──────────────────────┤
├───────────────────┤  │ ~ key: K             │
│ ~ head: Node<K,V> │  │ ~ value: V           │
│ ~ tail: Node<K,V> │  │ ~ prev: Node<K,V>    │
├───────────────────┤  │ ~ next: Node<K,V>    │
│ + addFirst()      │  └──────────────────────┘
│ + remove()        │           ▲
│ + moveToFirst()   │           │
│ + removeLast()    │     contains (0..*)
└───────────────────┘
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `Node<K, V>` | Holds a key-value pair with pointers to the previous and next nodes in the doubly linked list. |
| `DoublyLinkedList<K, V>` | Maintains insertion order with O(1) add, remove, and reorder. The head side is most-recent; the tail side is least-recent. Sentinel head and tail nodes eliminate null checks. |
| `LRUCacheService<K, V>` | Combines the map and list to serve as the public API. The map provides O(1) lookup; the list provides O(1) eviction ordering. |

---

## Design Decisions

### 1. HashMap + Doubly Linked List
The core insight is that no single data structure achieves O(1) for both lookup and ordered eviction:
- A `HashMap` alone gives O(1) lookup but has no ordering.
- A linked list alone gives O(1) insertion/removal but O(n) lookup.

Combining them solves both: the map stores `key → Node` for instant lookup, and the list maintains recency order so the LRU entry is always at the tail.

### 2. Sentinel Head and Tail Nodes
`DoublyLinkedList` initializes with dummy `head` and `tail` nodes that are never removed. This eliminates edge-case null checks when inserting into or removing from an empty list, making every operation uniform and simpler.

### 3. Storing the Key Inside the Node
`Node` stores both the key and the value even though the list is only used for ordering. This is necessary so that when `removeLast()` evicts the LRU node, `LRUCache` can look up and remove the corresponding entry from the `HashMap` without an O(n) reverse scan.

### 4. Generic Design (`<K, V>`)
All three classes are generic. This makes the cache reusable for any key and value types without sacrificing type safety, following the **Open/Closed Principle** — the cache is open for use with new types but closed for modification.

### 5. Thread Safety via `synchronized`
`get`, `put`, and `remove` in `LRUCacheService` are all declared `synchronized`. This ensures mutual exclusion on the shared `CACHE` map and `DOUBLY_LINKED_LIST` without introducing external locking concerns on the caller. It is a deliberate simplicity trade-off — sufficient for moderate concurrency, with `ReadWriteLock` or `ConcurrentHashMap`-based approaches as natural next steps for high-throughput scenarios.

### 6. Encapsulation of List Operations
All structural mutations of the doubly linked list are encapsulated in `DoublyLinkedList`. `LRUCacheService` only calls `addFirst`, `moveToFirst`, `remove`, and `removeLast` — it never manipulates node pointers directly. This separation of concerns keeps `LRUCacheService` focused on cache policy and `DoublyLinkedList` focused on list mechanics.

---

## Complexity

| Operation | Time | Space |
|---|---|---|
| `get` | O(1) | — |
| `put` | O(1) | O(capacity) |
| `remove` | O(1) | — |
