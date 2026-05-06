# Abstract Factory Pattern

## What is it?
Creates **families of related objects** without specifying their concrete classes. A factory produces multiple related products that are designed to work together.

**Difference from Factory Method** — Factory Method creates *one* product. Abstract Factory creates *a family* of related products (Account + Card together).

---

## Example — Banking System

A bank offers two customer tiers. Each tier has its own account type and card type that must match — you can't mix a Retail account with a Premium card.

```
«interface» BankFactory          «interface» Account    «interface» Card
─────────────────────────        ──────────────         ──────────
+ createAccount(): Account   ──▶ + deposit()            + pay()
+ applyCard(): Card          ──▶ + getBalance()         + getCreditLimit()
        ▲
        │ implements
   ┌────┴────────────┐
   │                 │
RetailFactory    PremiumFactory
   │                 │
   ├─ RetailAccount  ├─ PremiumAccount  (Current Account)
   └─ RetailCard     └─ PremiumCard     (Credit Card, limit: ₹25,000)
      (Debit Card)
```

| Factory | Account | Card |
|---|---|---|
| `RetailFactory` | Savings Account | Debit Card (balance-linked) |
| `PremiumFactory` | Current Account | Credit Card (₹25,000 limit) |

---

## Key Roles

| Role | Class |
|---|---|
| Abstract Factory | `BankFactory` interface |
| Concrete Factory | `RetailFactory`, `PremiumFactory` |
| Abstract Product | `Account`, `Card` interfaces |
| Concrete Product | `RetailAccount`, `RetailCard`, `PremiumAccount`, `PremiumCard` |
| Client | `AbstractFactoryDemo` |

---

## When to Use
- The system must work with **multiple families** of related objects.
- You want to enforce that products from the same family are used together.
- Adding a new family (e.g., `CorporateFactory`) should require **zero changes** to the client.

## Real-World Examples
- UI toolkit families (Windows/Mac/Linux buttons + checkboxes + dialogs)
- Cloud provider SDKs (AWS/GCP/Azure storage + compute + network clients)
- Database drivers (MySQL/Postgres connection + statement + result-set)
