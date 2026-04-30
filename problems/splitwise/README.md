# Splitwise

## Problem Statement

Design a bill-splitting system where users in a group can add shared expenses, track who owes whom, and settle debts. The system must support multiple split strategies and minimize the number of settlement transactions across a group.

This is a classic low-level design problem that tests domain modelling, the Strategy and Builder patterns, concurrency safety, and algorithmic thinking (minimize cash flow).

---

## Requirements

### Functional
- `createUser(name, email)` — Register a new user.
- `createGroup(name)` — Create a new group.
- `addParticipants(groupID, userIDs)` — Add members to a group.
- `addExpense(groupID, expense)` — Record a shared expense and update all participant balances.
- `settleUp(senderID, receiverID, amount)` — Record a direct payment between two users.
- `displayGroupBalance(groupID)` — Show each member's net balance scoped to the group.
- `removeUser(groupID, userID)` — Remove a member only if all group-scoped debts are settled.
- `simplifyDebts(groupID)` — Return the minimum set of transactions to clear all group debts.

### Non-Functional
- Adding a new split type must require **zero changes** to existing classes.
- Balances must be **group-scoped** — debts in other groups must not affect operations in this one.
- The service must be a **Singleton** — one shared instance manages all state.
- All public service operations must be **thread-safe**.

---

## UML Diagram

```
                    ┌─────────────────────────────────────────────┐
                    │         SplitwiseService <<Singleton>>      │
                    ├─────────────────────────────────────────────┤
                    │ - {final} allUsers: Map<String, User>       │
                    │ - {final} groups: Map<String, Group>        │
                    ├─────────────────────────────────────────────┤
                    │ + getInstance(): SplitwiseService           │
                    │ + createUser(name, email): String           │
                    │ + createGroup(name): String                 │
                    │ + addParticipants(groupID, IDs): void       │
                    │ + addExpense(groupID, expense): void        │
                    │ + settleUp(sender, receiver, amt): void     │
                    │ + displayGroupBalance(groupID): void        │
                    │ + removeUser(groupID, userID): void         │
                    │ + simplifyDebts(groupID): List<Transaction> │
                    └──────────┬──────────────┬───────────────────┘
                               │ manages      │ manages
                               ▼              ▼
             ┌──────────────────────┐    ┌────────────────────────┐
             │        Group         │    │          User          │
             ├──────────────────────┤    ├────────────────────────┤
             │ - groupID: UUID      │    │ - userID: UUID         │
             │ - name: String       │    │ - name: String         │
             │ - members: Set<User> │    │ - email: String        │
             │ - expenses: List     │    │ - balance: Balance     │
             ├──────────────────────┤    ├────────────────────────┤
             │ + addMember()        │    │ + getUserID(): String  │
             │ + removeMember()     │    │ + getName(): String    │
             │ + addExpense()       │    │ + getBalance()         │
             │ + getMembers()       │    │ + equals() / hashCode()│
             └──────────┬───────────┘    └──────────┬─────────────┘
                        │ contains                  │ owns
                        ▼                           ▼
             ┌─────────────────────┐    ┌────────────────────────────┐
             │       Expense       │    │          Balance           │
             ├─────────────────────┤    ├────────────────────────────┤
             │ - expenseID: UUID   │    │ - owner: User              │
             │ - description       │    │ - balances:                │
             │ - {final} paidBy    │    │     ConcurrentHashMap      │
             │ - {final} amount    │    │     <User, Double>         │
             │ - {final} participants   ├────────────────────────────┤
             │ - {final} splits    │    │ + adjustBalance()          │
             │ - {final} timeStamp │    │ + displayBalance()         │
             └──────────┬──────────┘    │ + getAllBalances()         │
                        │ built by      └────────────────────────────┘
                        ▼
             ┌─────────────────────┐    ┌──────────────────────────────┐
             │   Expense.Builder   │    │        <<interface>>         │
             ├─────────────────────┤    │        SplitStrategy         │
             │ + description()     │    ├──────────────────────────────┤
             │ + amount()          │    │ + getSplits(...): List<Split>│
             │ + paidBy()          │    └──────────┬───────────────────┘
             │ + participants()    │               │ implements
             │ + splitValues()     │    ┌──────────┼───────────┐
             │ + splitStrategy()   │    ▼          ▼           ▼
             │ + build(): Expense  │  Equal      Exact     Percent
             └─────────────────────┘  Split      Split      Split

       ┌──────────────────┐       ┌──────────────────────────┐
       │      Split       │       │       Transaction        │
       ├──────────────────┤       ├──────────────────────────┤
       │ - participant    │       │ - payer: User            │
       │ - amount: double │       │ - receiver: User         │
       └──────────────────┘       │ - amount: double         │
                                  │ + toString(): String     │
                                  └──────────────────────────┘
```

### Class Responsibilities

| Class | Responsibility |
|---|---|
| `User` | Domain entity with UUID identity. Overrides `equals`/`hashCode` on UUID for correct use as `ConcurrentHashMap` key and `Set` element. |
| `Balance` | Bidirectional ledger per user. Positive = keyed user owes owner. Negative = owner owes keyed user. Atomic updates via `ConcurrentHashMap.merge`. Returns unmodifiable view. Supports group-scoped display. |
| `Split` | Immutable value object pairing a participant with their computed share. |
| `Expense` | Receipt of a shared expense. Financial fields (`paidBy`, `amount`, `participants`, `splits`, `timeStamp`) are `final` — immutable after construction. `description` is intentionally mutable to allow metadata edits without reversing balance effects. Private constructor enforces `Builder` as the only construction path. |
| `Expense.Builder` | Fluent builder. Accepts `SplitStrategy` as a dependency — splits are computed at `build()` time via the injected strategy. |
| `Group` | Owns the member set (`ConcurrentHashMap.newKeySet`) and expense history (`synchronizedList`). Enforces group-scoped unsettled-balance check on `removeMember`. |
| `Transaction` | Immutable value object for a single directional settlement. Returned by `simplifyDebts`. `toString` formats human-readable output. |
| `SplitStrategy` | Interface for the split algorithm (`strategy` package). Three implementations: equal shares, exact amounts, percentage. Each validates inputs independently. |
| `SplitwiseService` | Singleton facade. Every public method `synchronized` on the instance. Orchestrates expense recording, paired balance updates, and debt simplification. |

---

## Design Decisions

### 1. Strategy Pattern for Split Types
Each split algorithm is a separate class in the `strategy` package implementing `SplitStrategy`. Adding a new type (e.g., `ShareSplit`) requires only a new class — `Expense`, `Group`, and `SplitwiseService` are never modified. Validation lives inside each strategy independently. This is the **Open/Closed Principle** applied directly.

### 2. Builder Pattern for `Expense`
`Expense` has six fields, one optional depending on strategy. The `Builder` makes each field self-documenting, prevents partial construction, and computes splits at `build()` time. The constructor is `private` — `build()` is the sole construction path. This also isolates the caller from any future constructor changes.

### 3. Selective Mutability in `Expense`
`description` is the only non-`final` field because it is pure metadata — changing it has zero effect on balances. Financial fields (`paidBy`, `amount`, `participants`, `splits`) are all `final` because they drove the balance updates that already happened at `addExpense` time. Mutating them without reversing and reapplying those balance effects would silently corrupt state. Financial edits are handled at the service level via `updateExpense`, which reverses old splits, validates the new expense, applies new splits, and replaces the expense in group history atomically under the service lock.

### 4. Balance as a Bidirectional Ledger
Each `User` owns a `Balance` — a `ConcurrentHashMap<User, Double>` where:
- **Positive value**: the keyed user owes the owner
- **Negative value**: the owner owes the keyed user

`merge` with `Double::sum` handles running totals atomically per-call. In `addExpense`, both sides of every split update happen inside the `SplitwiseService` lock, making each pair atomic end-to-end.

### 5. `User.equals` and `hashCode` on UUID
`User` is used as a key in `ConcurrentHashMap<User, Double>` (Balance) and `ConcurrentHashMap.newKeySet()` (Group). Java's contract requires equal objects to produce the same hash code. Both are implemented on `userID`. Without this, two equal `User` objects would hash to different buckets, silently breaking all lookups.

### 6. `getAllBalances()` returns `unmodifiableMap`
Exposing the raw `ConcurrentHashMap` would let callers mutate balance state without going through `adjustBalance`, bypassing the self-guard and the service lock. `Collections.unmodifiableMap(balances)` prevents mutation while allowing safe iteration for display and debt simplification.

### 7. Group-Scoped Balance Operations
`displayBalance` accepts a `Set<User>` and skips entries outside it — cross-group debts are invisible within a group view. `removeMember` iterates only entries whose key is in `members`, so debts in other groups never block removal from this group. `simplifyDebts` computes net balances scoped to group members by the same filter.

### 8. Simplify Debts — Greedy Minimum Cash Flow
Computes each member's net group-scoped balance, then greedily pairs the largest creditor with the largest debtor each round, settling the minimum of their absolute amounts. Reduces up to N*(N-1)/2 pairwise debts to at most N-1 transactions. Two-pointer on sorted lists — equivalent to priority-queue approach, avoids per-round re-insertion. Returns `List<Transaction>` for programmatic consumption.

### 9. Concurrency Model — Single Service Lock
All public service methods are `synchronized` on the `SplitwiseService` instance. Since it is a Singleton, this is a single global write lock ensuring paired balance updates are always atomic. `Group` and `Balance` use concurrent collections (`ConcurrentHashMap.newKeySet`, `synchronizedList`, `ConcurrentHashMap.merge`) as a second safety layer. All read operations (display, simplify) also hold the lock to prevent reads of torn state during concurrent writes.

---

## Complexity

| Operation | Time | Notes |
|---|---|---|
| `createUser` / `createGroup` | O(1) | `ConcurrentHashMap.put` |
| `addParticipants` | O(P) | P = number of participants |
| `addExpense` | O(P) | One `merge` per participant, both sides |
| `settleUp` | O(1) | Two `merge` calls |
| `displayGroupBalance` | O(M × B) | M members × B balance entries per user |
| `removeMember` check | O(B) | B = balance entries for the user |
| `simplifyDebts` | O(M log M) | Sort creditors + debtors; two-pointer O(M) |
