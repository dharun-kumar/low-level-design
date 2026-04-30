# Parking Lot

## Problem Statement

Design a multi-floor parking lot system that can park and unpark vehicles of different types (Car, Bike, Truck), issue tickets on entry, and calculate fare on exit based on the duration parked.

This is a classic low-level design problem that tests the ability to model a real-world system using OOP principles — encapsulation, polymorphism, and extensibility.

---

## Requirements

### Functional
- `checkIn(vehicle)` — Find the first available floor with a free spot for the vehicle type, assign it, and return a `ParkingTicket`. Returns `Optional.empty()` if the lot is full.
- `checkOut(ticket)` — Free the occupied spot and calculate the fare based on time parked.
- `getFare()` — Returns `Optional<Double>` on the ticket. Empty if the vehicle has not been checked out yet.

### Non-Functional
- The system must be **thread-safe** — concurrent check-ins and check-outs must not corrupt slot counts.
- The lot must be a **Singleton** — only one instance exists per JVM.
- Adding a new vehicle type must require **zero changes** to `ParkingFloor`.

---

## UML Diagram

```
                        ┌──────────────────────────────────────┐
                        │     ParkingLotService <<Singleton>>  │
                        ├──────────────────────────────────────┤
                        │ - parkingFloors: List<ParkingFloor>  │
                        │ - INSTANCE: ParkingLotService        │
                        ├──────────────────────────────────────┤
                        │ + getInstance(): ParkingLotService   │
                        │ + setCapacity(...): void             │
                        │ + checkIn(vehicle): Optional<ParkingTicket> │
                        │ + checkOut(ticket): void             │
                        └────────────┬─────────────────────────┘
                                     │ contains 1..*
                                     ▼
                        ┌──────────────────────────────┐
                        │         ParkingFloor         │
                        ├──────────────────────────────┤
                        │ - floorNumber: int           │
                        │ - totalSpots: Map<VehicleType, Integer>    │
                        │ - availableSpots: Map<VehicleType, Integer>│
                        ├──────────────────────────────┤
                        │ + parkVehicle(vehicle): boolean            │
                        │ + freeVehicleSpot(vehicle): void           │
                        │ + getFloorNumber(): int       │
                        └──────────────────────────────┘

┌──────────────────────────────────┐       ┌─────────────────────────────┐
│          ParkingTicket           │       │       Vehicle (abstract)    │
├──────────────────────────────────┤       ├─────────────────────────────┤
│ - vehicle: Vehicle               │──────▶│ - vehicleType: VehicleType  │
│ - entryTimeStamp: long           │       │ - vehicleNumber: String     │
│ - parkedFloor: int               │       │ - farePerHour: double       │
│ - fare: double                   │       │ - status: VehicleStatus     │
├──────────────────────────────────┤       ├─────────────────────────────┤
│ + calculateFare(): void          │       │ + getVehicleType()          │
│ + getVehicle(): Vehicle          │       │ + getFarePerHour()          │
│ + getParkedFloor(): int          │       │ + getStatus()               │
│ + getFare(): Optional<Double>    │       │ + setStatus()               │
└──────────────────────────────────┘       └──────────┬──────────────────┘
                                                      │ extends
                                          ┌───────────┼───────────┐
                                          ▼           ▼           ▼
                                        Car          Bike        Truck

                    ┌──────────────┐         ┌────────────────┐
                    │  VehicleType │         │  VehicleStatus │
                    ├──────────────┤         ├────────────────┤
                    │  CAR         │         │  UNPARKED      │
                    │  BIKE        │         │  PARKED        │
                    │  TRUCK       │         │  CHECKED_OUT   │
                    └──────────────┘         └────────────────┘
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `Vehicle` | Abstract domain object holding vehicle identity, type, fare rate, and current parking status. |
| `Car` / `Bike` / `Truck` | Concrete vehicles — each supplies its type and hourly fare via the parent constructor. |
| `VehicleType` | Enum used as a type-safe key for slot maps. Eliminates string-based dispatch. |
| `VehicleStatus` | Enum tracking vehicle lifecycle: `UNPARKED → PARKED → CHECKED_OUT`. |
| `ParkingFloor` | Manages available and total slot counts per vehicle type using a `Map<VehicleType, Integer>`. Handles park and free operations. |
| `ParkingTicket` | Receipt issued on check-in. Records entry time and floor. Calculates fare on checkout. Exposes fare only after checkout via `Optional<Double>`. |
| `ParkingLotService` | Singleton entry point. Iterates floors to find the first available slot, owns the vehicle status lifecycle, and synchronizes concurrent access. |

---

## Design Decisions

### 1. `Map<VehicleType, Integer>` in `ParkingFloor`
The original design used three separate integer fields (`availableCarSpots`, `availableBikeSpots`, `availableTruckSpots`) with an `if-else` chain dispatching on vehicle type. This violates the **Open/Closed Principle** — adding a new vehicle type required editing `ParkingFloor`.

Using `Map<VehicleType, Integer>` keyed by the enum reduces both `parkVehicle` and `freeVehicleSpot` to a single map lookup. `ParkingFloor` now never needs to change when a new `VehicleType` is added.

### 2. `VehicleType` Enum over String Dispatch
Earlier versions called `vehicle.getType()` and compared strings like `"Car"` with `equalsIgnoreCase`. This caused a real bug — `Truck.TYPE` was accidentally set to `"Bike"`, silently routing trucks to bike slots. An enum makes this class of bug impossible at compile time.

### 3. Singleton with Separated Configuration
`ParkingLotService` is a Singleton because only one physical lot exists. Passing configuration parameters directly into `getInstance(...)` is an anti-pattern — after the first call, subsequent calls with different parameters are silently ignored.

`getInstance()` handles identity only. `setCapacity(...)` handles one-time configuration and throws `IllegalStateException` if called more than once (`!parkingFloors.isEmpty()`), preventing silent state corruption from accidental double-initialization.

### 4. `Optional<ParkingTicket>` on `checkIn`
Returning `null` when the lot is full gives the caller no way to distinguish an empty lot from a bug. `Optional.empty()` is an explicit, type-safe signal that no slot was found, and forces the caller to handle the case.

### 5. `Optional<Double>` on `getFare`
`getFare()` returns `Optional.empty()` if the vehicle has not been checked out yet (status is not `CHECKED_OUT`). This prevents callers from silently reading a `0.0` fare before checkout has occurred.

### 6. Vehicle Status Owned by `ParkingLotService`
`ParkingTicket` is a receipt — it records a transaction, not manages a vehicle's lifecycle. `ParkingTicket` constructor does not call `setStatus`. Both transitions are in `ParkingLotService` exclusively: `checkIn` sets `PARKED` after a floor confirms a slot, `checkOut` sets `CHECKED_OUT` after fare is calculated. This makes `ParkingLotService` the single source of truth for vehicle lifecycle and enables double-park prevention with one guard in `checkIn`.

### 7. Thread Safety — Two-Layer Locking
`checkIn` and `checkOut` are `synchronized` on the `ParkingLotService` instance providing a single outer lock for all slot mutations. Inside `ParkingFloor`, `parkVehicle` uses `ConcurrentHashMap.compute` and `freeVehicleSpot` uses `ConcurrentHashMap.computeIfPresent` — making each slot decrement and increment atomically at the map level. The two layers together eliminate the check-then-act race: the outer lock serializes cross-floor decisions, the inner compute ensures no torn writes on the slot counters themselves.

### 8. Fare Calculation with Ceiling Hours
`Math.ceil(parkedHours)` is used so that any partial hour is billed as a full hour — standard real-world parking billing. Entry and exit timestamps use `Instant.now().getEpochSecond()` (wall-clock seconds) rather than `System.nanoTime()` (monotonic, not wall-clock), which is correct for billing duration.

---

## Complexity

| Operation | Time | Notes |
|---|---|---|
| `checkIn` | O(F) | F = number of floors; scans for first available |
| `checkOut` | O(1) | Direct floor access via ticket's `parkedFloor` |
| `parkVehicle` | O(1) | Single map lookup by `VehicleType` |
| `freeVehicleSpot` | O(1) | Single map update by `VehicleType` |
| `calculateFare` | O(1) | Arithmetic on timestamps |
