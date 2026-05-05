# Elevator System

## Problem Statement

Design a multi-elevator system for a building that handles two types of requests: external requests (a person on a floor presses UP or DOWN) and internal requests (a person inside an elevator presses a destination floor). Elevators run concurrently, each independently processing their queues, and the system dispatches the most optimal elevator for each external request.

This is a classic low-level design problem that tests State pattern for lifecycle management, SCAN algorithm for efficient floor scheduling, Observer pattern for real-time display, and concurrent programming for independent elevator threads.

---

## Requirements

### Functional
- `requestElevator(floor, direction)` — External request: dispatch the nearest eligible elevator to the given floor.
- `selectFloor(elevatorID, floor)` — Internal request: queue a destination floor inside a specific elevator.
- `start()` — Begin all elevator run loops.
- `shutdown()` — Gracefully stop all elevators and the thread pool.
- Observer display — Print elevator ID, current floor, and direction on every state change.

### Non-Functional
- Each elevator runs on its own thread — concurrent, independent operation.
- Request queues must be thread-safe — service thread adds requests while elevator thread processes them.
- State transitions and floor visibility must be consistent across threads.
- Adding a new elevator selection strategy requires zero changes to `ElevatorService`.

---

## UML Diagram

```
                ┌────────────────────────────────────────────────────────┐
                │              ElevatorService <<Singleton>>              │
                ├────────────────────────────────────────────────────────┤
                │ - elevators: Map<Integer, Elevator>                     │
                │ - executorService: ExecutorService                      │
                │ - selectionStrategy: ElevatorSelectionStrategy          │
                ├────────────────────────────────────────────────────────┤
                │ + getInstance(noOfElevators): ElevatorService           │
                │ + requestElevator(floor, direction): void               │
                │ + selectFloor(elevatorID, floor): void                  │
                │ + start(): void                                         │
                │ + shutdown(): void                                      │
                └────────────┬─────────────────────┬──────────────────────┘
                             │ dispatches           │ uses
                             ▼                      ▼
              ┌──────────────────────────┐   «interface»
              │         Elevator         │   ElevatorSelectionStrategy
              ├──────────────────────────┤   ─────────────────────────────
              │ - elevatorID: int        │   + selectElevator(...):
              │ - currentFloor:          │     Optional<Elevator>
              │     AtomicInteger        │          ▲
              │ - state: volatile State  │          │ implements
              │ - isRunning: volatile    │   NearestElevationStrategy
              │ - upRequests:            │
              │   ConcurrentSkipListSet  │
              │ - downRequests:          │
              │   ConcurrentSkipListSet  │
              │ - observers: List        │
              ├──────────────────────────┤
              │ + addRequest(request)    │
              │ + move()                 │
              │ + setState(state)        │
              │ + notifyObservers()      │
              │ + run()                  │
              └────────┬─────────────────┘
                       │ has 1
                       ▼
              ┌─────────────────────────────────────────┐
              │            State (abstract)              │
              ├─────────────────────────────────────────┤
              │ + getDirection(): Direction              │
              │ + move(elevator): void                   │
              │ + addRequest(elevator, request): void    │
              └────────────────┬────────────────────────┘
                               │ extends
               ┌───────────────┼────────────────┐
               ▼               ▼                ▼
         IdleState      MovingUpState     MovingDownState
         ──────────      ─────────────     ───────────────
         move():         move():           move():
          check queues    floor++           floor--
          → transition    stop if match     stop if match
                         addRequest():     addRequest():
                          UP ahead → up     DOWN ahead → down
                          DOWN → down       UP → up

┌──────────────────────────────────┐    «interface»
│            Request               │    Observer
├──────────────────────────────────┤    ─────────────────────────
│ - targetFloor: int               │    + update(elevator): void
│ - direction: Direction           │          ▲
│ - requestSource: RequestSource   │          │ implements
└──────────────────────────────────┘       Display

┌──────────────┐   ┌──────────────────┐
│  Direction   │   │  RequestSource   │
├──────────────┤   ├──────────────────┤
│  IDLE        │   │  INTERNAL        │
│  UP          │   │  EXTERNAL        │
│  DOWN        │   └──────────────────┘
└──────────────┘
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `ElevatorService` | Singleton entry point. Creates elevators, submits them to the thread pool, and routes external requests via the selection strategy. Owns the elevator lifecycle. |
| `Elevator` | Runnable domain object. Maintains its own request queues, current floor, and state. Drives its own movement loop. Notifies observers on state change. |
| `State` | Abstract base defining default `addRequest` routing: floor above current → `upRequests`, floor below → `downRequests`. Used as-is for INTERNAL requests; overridden by moving states for EXTERNAL requests. |
| `IdleState` | No movement. On `move()`, transitions to `MovingUpState` if `upRequests` non-empty, else `MovingDownState` if `downRequests` non-empty. UP is prioritised when both queues have requests. |
| `MovingUpState` | Increments floor each tick, stops at the next `upRequests` target. External same-direction (UP) requests ahead are served this trip; opposite-direction (DOWN) requests are deferred to `downRequests`. |
| `MovingDownState` | Decrements floor each tick, stops at the next `downRequests` target. External same-direction (DOWN) requests ahead are served this trip; opposite-direction (UP) requests are deferred to `upRequests`. |
| `Request` | Value object carrying target floor, direction, and source (INTERNAL / EXTERNAL). |
| `ElevatorSelectionStrategy` | Interface for pluggable dispatch algorithms. |
| `NearestElevationStrategy` | Selects the eligible elevator with the minimum distance to the requested floor. Eligible = IDLE, or moving in the same direction and hasn't passed the floor yet. |
| `Observer` | Interface for display/monitoring callbacks triggered on elevator state changes. |
| `Display` | Prints elevator ID, current floor, and direction to console on every state change. |

---

## Design Decisions

### 1. State Pattern for Elevator Lifecycle
Each elevator has exactly three states: `IdleState`, `MovingUpState`, `MovingDownState`. Each state owns its own `move()` and `addRequest()` logic. Adding a new state (e.g., `MaintenanceState`) requires zero changes to `Elevator` — just a new `State` subclass. Without the State pattern, `Elevator.move()` would be a large if-else chain over direction enum that grows with every new behaviour.

### 2. SCAN Algorithm — Dual Sorted Queues
Each elevator maintains two queues: `upRequests` (ascending `ConcurrentSkipListSet`) and `downRequests` (descending `ConcurrentSkipListSet`). The elevator exhausts all UP stops in ascending order, transitions to IDLE, then processes all DOWN stops in descending order. This mirrors the SCAN (elevator) algorithm — one full sweep in each direction before reversing — minimising total travel distance compared to FIFO or random dispatch.

### 3. `ConcurrentSkipListSet` for Thread-Safe Sorted Queues
The service thread (via `addRequest`) and the elevator thread (via `move`) concurrently access the request queues. `TreeSet` is not thread-safe — concurrent access can corrupt the tree structure. `ConcurrentSkipListSet` provides the same sorted iteration and O(log n) add/remove with lock-free thread safety. The descending comparator on `downRequests` ensures `first()` always returns the highest pending floor, matching the DOWN sweep direction.

### 4. `volatile` on `State` — Cross-Thread Visibility
`setState()` is called from the elevator thread. `getState().getDirection()` is read from the service thread inside `NearestElevationStrategy.isOpt()`. Without `volatile`, the service thread may cache a stale state (e.g., see IDLE when the elevator has already transitioned to UP), causing incorrect dispatch decisions. `volatile` guarantees the service thread always reads the latest state.

### 5. `AtomicInteger` for `currentFloor` — Visibility Without Lock
`currentFloor` is written by the elevator thread and read by the service thread (in `NearestElevationStrategy` for distance calculation). `AtomicInteger` provides thread-safe visibility on reads without requiring synchronization — the service thread always sees the latest floor value. Only the elevator thread writes it, so no compound CAS is needed.

### 6. `ElevatorSelectionStrategy` Interface — Pluggable Dispatch
`NearestElevationStrategy` picks the closest eligible elevator. Future strategies (round-robin, zone-based, load-balanced) implement the same interface without touching `ElevatorService`. The service delegates dispatch entirely to the strategy — OCP compliant.

### 7. Observer for Display — Decoupled Monitoring
`setState()` in `Elevator` calls `notifyObservers()` on every direction change. The `Display` observer prints current state without any coupling to elevator internals. Adding a new observer (e.g., `AlertSystem`, `LoggingObserver`) requires zero changes to `Elevator`.

### 8. Direction-Aware Deferred Routing — Don't Make Passengers Ride the Wrong Way

When an external request arrives, the elevator must decide: serve it this trip or the return trip. The rule is simple — **never stop for a passenger going in the opposite direction.**

Consider an elevator going UP at floor 5. Someone at floor 8 presses DOWN:
- If floor 8 is added to `upRequests` → elevator stops at 8 going UP → passenger boards, but is now stuck riding all the way to the top before the elevator reverses and comes back down.
- Correct: add floor 8 to `downRequests` → elevator stops at 8 on the return trip → passenger boards going DOWN immediately.

The same applies in reverse for `MovingDownState`. A passenger at floor 3 pressing UP while the elevator is going DOWN should not be forced to ride to the basement first.

**INTERNAL** requests (passenger already inside) use the base `State.addRequest` — above current floor goes to `upRequests`, below goes to `downRequests`. Direction is irrelevant once you are inside; the elevator will eventually serve your floor on the correct sweep.

**EXTERNAL** requests use the state-aware override:

| State | External request condition | Queue |
|---|---|---|
| `MovingUpState` | UP + floor ahead | `upRequests` — serve this trip |
| `MovingUpState` | DOWN (any floor) | `downRequests` — serve return trip |
| `MovingDownState` | DOWN + floor ahead | `downRequests` — serve this trip |
| `MovingDownState` | UP (any floor) | `upRequests` — serve return trip |

### 9. Request Priority Across States

Each state has a deliberate priority order for which requests to serve first:

| State | Priority |
|---|---|
| `IdleState` | `upRequests` checked first — UP wins if both queues are non-empty |
| `MovingUpState` | Same-direction (UP) requests ahead of the elevator are served this trip; opposite-direction (DOWN) requests are deferred |
| `MovingDownState` | Same-direction (DOWN) requests ahead of the elevator are served this trip; opposite-direction (UP) requests are deferred |

This mirrors real elevator behaviour — keep going in the current direction until the queue is exhausted, then reverse. Passengers pressing in the same direction as the elevator are picked up immediately on the way. Passengers pressing the opposite direction wait for the return trip, but board going their intended way rather than riding backward.

### 10. Each Elevator Runs on Its Own Thread
`Elevator implements Runnable` and is submitted to a fixed thread pool with one thread per elevator. Each elevator's `run()` loop calls `move()` every 500ms independently, with no shared locks between elevators. This models real-world elevator independence and maximises throughput.

---

## Complexity

| Operation | Time | Notes |
|---|---|---|
| `addRequest` (to queue) | O(log n) | `ConcurrentSkipListSet.add` |
| `move()` — next stop check | O(log n) | `first()` + `pollFirst()` on skip list |
| `selectElevator` (nearest) | O(E) | E = number of elevators |
| State transition | O(1) | Direct `setState` call |
| `notifyObservers` | O(k) | k = number of registered observers |
