# Movie Ticket Booking

## Problem Statement

Design a movie ticket booking system that supports multiple theaters, screens, and shows across cities. Users can search for movies and theaters, view seat availability, and book tickets. The booking flow uses a lock-hold-confirm pattern — seats are reserved during payment and confirmed or released based on the outcome.

This is a classic low-level design problem that tests the ability to model a multi-entity domain with async workflows, strategy-based pricing and payments, and observer-based notifications.

---

## Requirements

### Functional
- `addTheater / addScreen / addShow` — Configure theaters, screens, and shows for a city.
- `searchTheatersByCity(city)` — List all theaters in a city.
- `getMoviesByCity(city)` — List all movies currently showing in a city.
- `searchShowsByTheater(theaterID)` — List all shows at a theater.
- `searchTheatersByMovie(movie)` — Find theaters showing a specific movie.
- `getSeatAvailability(...)` — Return all seats and their current state (`AVAILABLE`, `LOCKED`, `BOOKED`) for a show.
- `bookTicket(...)` — Lock the requested seats, process payment asynchronously, confirm booking on success or release seats on failure. Returns `CompletableFuture<Ticket>`.
- `getTicketsByUser(userID)` — Return all tickets booked by a user.
- Observer — Notify all registered users when a new show is added.

### Non-Functional
- **Thread-safe** — Concurrent bookings for the same show must never double-book a seat.
- **Async payment** — Payment processing must not block the service from accepting new requests.
- **Timeout & cleanup** — Payments that exceed 600 seconds must release the held seats.
- **Pluggable payment and pricing** — Adding a new payment method or pricing rule requires zero changes to `BookingManager` or `Show`.
- **Singleton service** — Only one `MovieBookingService` instance per JVM.

---

## UML Diagram

```
                    ┌────────────────────────────────────────────────────┐
                    │          MovieBookingService <<Singleton>>          │
                    ├────────────────────────────────────────────────────┤
                    │ - theaters : Map<String, Theater>                  │
                    │ - cities   : Map<String, City>                     │
                    │ - movies   : Map<City, Set<Movie>>                 │
                    │ - users    : Map<String, User>                     │
                    │ - tickets  : Map<User, Set<Ticket>>                │
                    │ - bookingManager : BookingManager                  │
                    │ - observer       : MovieObserver                   │
                    ├────────────────────────────────────────────────────┤
                    │ + addTheater / addScreen / addShow(...)            │
                    │ + createUser(...): String                          │
                    │ + searchTheatersByCity(name): List<Theater>        │
                    │ + getMoviesByCity(city): Collection<Movie>         │
                    │ + searchShowsByTheater(id): Collection<Show>       │
                    │ + searchTheatersByMovie(movie): Collection<Theater>│
                    │ + getSeatAvailability(...): Collection<Seat>       │
                    │ + bookTicket(...): CompletableFuture<Ticket>       │
                    │ + getTicketsByUser(id): Set<Ticket>                │
                    └──────────┬──────────────────┬──────────────────────┘
                               │ owns             │ owns
                               ▼                  ▼
              ┌─────────────────────────┐  ┌──────────────────────┐
              │      BookingManager     │  │    MovieObserver      │
              ├─────────────────────────┤  ├──────────────────────┤
              │ - executorService       │  │ - subscribers:       │
              ├─────────────────────────┤  │   Set<User>          │
              │ + processBooking(...)   │  ├──────────────────────┤
              │   : CompletableFuture   │  │ + addSubscriber      │
              │ + lockSeats(...)        │  │ + removeSubscriber   │
              │ + confirmBooking(...)   │  │ + notifySubscribers  │
              │ + releaseSeats(...)     │  └──────────────────────┘
              │ + shutdown()           │
              └─────────────────────────┘

┌──────────────────────────────────┐         ┌──────────────────────────┐
│            Theater               │         │           Show            │
├──────────────────────────────────┤         ├──────────────────────────┤
│ - theaterID : UUID               │         │ - showID : String        │
│ - name      : String             │         │ - movie  : Movie         │
│ - city      : City               │         │ - screen : Screen        │
│ - screens   : Map<String,Screen> │         │ - showTime : ShowTime    │
│ - shows     : Map<String,Show>   │         │ - seats  : Map<String,   │
├──────────────────────────────────┤         │            Seat>         │
│ + addScreen / addShow            │         │ - priceStrategy          │
│ + getShow / getShows             │         ├──────────────────────────┤
└──────────────┬───────────────────┘         │ + getSeat / getSeats     │
               │ contains 0..*               └───────────┬──────────────┘
               └──────────────────┐                      │ contains 1..*
                                  ▼                      ▼
                    ┌──────────────────────┐   ┌───────────────────────┐
                    │        Screen        │   │         Seat          │
                    ├──────────────────────┤   ├───────────────────────┤
                    │ - name: String       │   │ - seatID: String      │
                    │ - rows: int          │   │ - type : SeatType     │
                    │ - seatsPerRow: int   │   │ - state: volatile     │
                    └──────────────────────┘   │         SeatState     │
                                               ├───────────────────────┤
                                               │ + lock()              │
                                               │ + book()              │
                                               │ + release()           │
                                               └───────────────────────┘

┌──────────────────────────────────────┐    ┌──────────────────────────┐
│               Ticket                 │    │         Payment          │
├──────────────────────────────────────┤    ├──────────────────────────┤
│ - ticketID : UUID                    │    │ - transactionID : UUID   │
│ - user     : User                    │───▶│ - amount : double        │
│ - show     : Show                    │    │ - status : PaymentStatus │
│ - seats    : Set<Seat>               │    └──────────────────────────┘
│ - payment  : Payment                 │
└──────────────────────────────────────┘

«interface»                               «interface»
PaymentStrategy                           PriceStrategy
─────────────────────────────             ──────────────────────────────
+ pay(amount: double): Payment            + calculatePrice(seats): double
         ▲                                             ▲
         │                                             │
CreditCardPayment                         WeekEndPrice

┌──────────────┐     ┌───────────────┐     ┌────────────────┐     ┌──────────────┐
│   SeatState  │     │   SeatType    │     │    ShowTime    │     │PaymentStatus │
├──────────────┤     ├───────────────┤     ├────────────────┤     ├──────────────┤
│  AVAILABLE   │     │ REGULAR (150) │     │  MORNING       │     │  SUCCESS     │
│  LOCKED      │     │ PREMIUM (200) │     │  AFTERNOON     │     │  PENDING     │
│  BOOKED      │     │               │     │  EVENING       │     │  FAILURE     │
└──────────────┘     └───────────────┘     │  NIGHT         │     └──────────────┘
                                           └────────────────┘
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `MovieBookingService` | Singleton entry point. Owns all catalogs (theaters, movies, users, tickets). Routes search and booking requests. All public methods synchronized. |
| `BookingManager` | Owns the booking state machine: lock seats → async payment → confirm or release. Manages the `ExecutorService` for payment processing. |
| `Theater` | Holds screen and show catalogs keyed by composite ID. Constructs `Show` objects on `addShow`. |
| `Show` | Owns the seat map for one screening. Initializes seat layout (REGULAR / PREMIUM by row zone) on construction. |
| `Seat` | Domain object tracking seat state with a volatile field. Enforces valid state transitions via guarded `lock / book / release` methods. |
| `Ticket` | Immutable receipt issued on confirmed booking. Holds user, show, seats, and payment references. |
| `Payment` | Value object recording transaction result and amount. |
| `User` | User identity with observer `notify` callback for new show alerts. |
| `Movie` / `Screen` / `City` | Plain domain value objects. |
| `PaymentStrategy` | Interface for pluggable payment methods. Each implementation handles its own I/O and returns a `Payment`. |
| `PriceStrategy` | Interface for pluggable pricing rules. Receives the seat set and returns a total amount. |
| `MovieObserver` | Maintains the subscriber set (`ConcurrentHashMap.newKeySet()`). Notifies all users on new show creation. |
| `SeatUnavailableException` | Typed unchecked exception thrown when a seat transition is attempted from an invalid state. |
| `SeatType` | Enum carrying base price per tier (REGULAR=150, PREMIUM=200). Eliminates hardcoded price dispatch. |

---

## Design Decisions

### 1. Lock-Hold-Confirm Pattern for Seat Reservation
The booking flow is three-phase: `AVAILABLE → LOCKED` on booking attempt, `LOCKED → BOOKED` on payment success, `LOCKED → AVAILABLE` on failure or timeout. Seats are held during the payment window so no concurrent booking can claim them, but they are not permanently consumed until payment succeeds. This mirrors how real ticketing systems (and hotel bookings) work.

### 2. `CompletableFuture<Ticket>` for Async Payment
Payment is I/O-bound (network call to a payment gateway). Blocking the service thread for each payment would collapse throughput under concurrent load. `bookTicket` submits payment to an `ExecutorService`, returns the future immediately, and releases the service lock. The caller registers a callback via `thenAccept`. The service can accept new bookings while payments are in flight.

### 3. `BookingManager` Separated from `MovieBookingService`
`MovieBookingService` is responsible for catalog management and request routing. `BookingManager` is responsible for the booking lifecycle — locking, payment, confirmation, release, timeout, and executor management. Single Responsibility: neither class needs to know the other's details beyond the interface boundary.

### 4. `volatile SeatState` + Service Lock — Two-Layer Safety
`volatile` on `Seat.state` ensures that state writes by the executor thread (in `confirmBooking` / `releaseSeats`) are immediately visible to the main thread's subsequent `getState()` reads. The outer `synchronized bookTicket` serializes the `lockSeats` call so only one booking can attempt to lock a seat at a time. Each layer has a distinct role: visibility from `volatile`, mutual exclusion from the service lock.

### 5. `SeatType` Enum Carries Base Price
Seat pricing is a property of the seat tier, not a lookup table or if-else chain. Adding a new tier (e.g., `VIP`) requires adding an enum constant — zero changes to `PriceStrategy` implementations or `Show`.

### 6. `PriceStrategy` — Decouples Pricing from Show
Weekend surge, early-bird discounts, VIP pricing — all are implementations of `PriceStrategy` injected at show creation. `Show` delegates to the strategy without knowing the pricing rule. OCP: `Show` is closed for modification, open for new pricing.

### 7. `PaymentStrategy` — Decouples Payment from BookingManager
Credit card, UPI, wallet, net banking — all implement `PaymentStrategy`. `BookingManager` calls `pay(amount)` and receives a `Payment` result. No conditional dispatch on payment type anywhere in the flow.

### 8. `orTimeout(600s)` + `exceptionally` for Seat Cleanup
A payment that stalls for any reason must not leave seats locked indefinitely. `orTimeout` fires a `TimeoutException` after 600 seconds, caught by `exceptionally`, which releases the seats back to `AVAILABLE`. Any other unexpected exception in the booking chain also triggers the same cleanup path.

### 9. `ConcurrentHashMap.newKeySet()` for User Ticket Sets
Multiple bookings for the same user (different shows) can complete concurrently — their `thenApply` callbacks run on executor threads simultaneously. The inner ticket set for each user must be thread-safe. `ConcurrentHashMap.newKeySet()` allows concurrent `add` calls without data races. `computeIfAbsent` on the outer map ensures the set is created exactly once per user.

### 10. `MovieObserver` with `ConcurrentHashMap.newKeySet()`
Subscribers can be added (`createUser`) or removed while `notifySubscribers` is iterating. `CopyOnWriteArraySet` would be appropriate for a strictly read-heavy pattern. Here `ConcurrentHashMap.newKeySet()` is chosen because subscriber churn (add/remove) is expected alongside notifications. Iteration is weakly consistent — a subscriber added during notification may or may not receive that notification, which is acceptable for an informational alert.

---

## Complexity

| Operation | Time | Notes |
|---|---|---|
| `bookTicket` (lock phase) | O(k) | k = number of seats requested |
| `bookTicket` (payment phase) | async | Non-blocking, runs on executor thread |
| `getSeatAvailability` | O(1) | Direct map lookup in Theater → Show |
| `getMoviesByCity` | O(1) | ConcurrentHashMap lookup by City |
| `searchTheatersByCity` | O(T) | Linear scan over all theaters; can be O(1) with a city→theaters index |
| `searchTheatersByMovie` | O(T × S) | T theaters, S shows per theater |
| `searchShowsByTheater` | O(1) | Direct map lookup in Theater |
| `getTicketsByUser` | O(1) | ConcurrentHashMap lookup by User |
