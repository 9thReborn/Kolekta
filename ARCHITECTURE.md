# Kolekta — Architecture & Security Note

Kolekta is a multi-tenant collections & reconciliation engine built on the Nomba API. Merchants issue each of their end-customers a dedicated virtual account (a NUBAN); inbound bank transfers are then reconciled to the correct customer automatically, with exactly-once accounting.

## System layers

```
Kolekta platform            → Nomba parent account
  └─ Merchant (tenant)       → Nomba sub-account (segregated funds)
       └─ End-customer        → dedicated virtual account (NUBAN)
Kolekta double-entry ledger  → per-customer balances (derived from immutable entries)
```

Because each end-customer has a *dedicated* account number, an inbound transfer self-identifies: the account it lands on tells us exactly who paid. That is the core value — deterministic reconciliation instead of manual matching.

## Authentication (auth)

- **OAuth2 client-credentials** (server-to-server; no user present).
- A `TokenManager` holds **one cached access token**, is **thread-safe** (a volatile fast-path plus a lock with double-check), and **refreshes proactively** ~5 minutes before the 60-minute expiry, so many concurrent workers share a single token rather than requesting one per call.
- Every authenticated request carries `Authorization: Bearer <token>` and the `accountId` header.
- The client secret and webhook secret are read **only from environment variables** — never committed to source.

## Provisioning (Virtual Accounts)

- `POST /accounts/virtual/{subAccountId}` issues a dedicated NUBAN per customer, scoped to the merchant's sub-account.
- Built with a **ports-and-adapters** design: a `VirtualAccountProvider` interface with a live Nomba adapter and a mock adapter, so the whole flow runs offline in development and swaps to live via one environment flag.
- Kolekta's own `accountRef` is the **stable primary reference**; Nomba's generated ids are stored only as foreign references (they can rotate on retry).

## Webhooks (inbound handling)

- Endpoint: `POST /webhooks/nomba`.
- **Signature verification:** the `nomba-signature` header is an HMAC-SHA256 of the raw request body under a shared secret. Kolekta recomputes the HMAC over the **raw bytes** and compares it in constant time. Nothing is trusted until the signature verifies.
- **Inbox pattern:** the raw event is persisted first, **deduplicated on Nomba's `requestId`** via a unique database constraint, and the endpoint returns **200 immediately**. The money logic runs asynchronously afterward.
- Webhook delivery is **at-least-once** (retried up to five times) and can also be missed entirely, so the system is designed for **exactly-once effect** on top of at-least-once delivery.

## Reconciliation (exactly-once)

- A scheduled **inbox processor** drains verified, unprocessed events.
- For a `virtual_account.funded` event it: matches by `accountRef` → customer; validates the amount; and posts a **balanced double-entry ledger transaction** (credit the customer, debit the platform) inside a single database transaction, then marks the event processed.
- **Idempotency is enforced by the database** (the unique `requestId` constraint), so retried deliveries cannot double-post.
- **Misdirected payments** — unmatched, closed-account, over-payment, and under-payment cases — are routed to a **resolution queue** instead of being silently mis-posted.
- **Requery reconciler:** a scheduled job cross-checks Nomba's `GET /transactions` against the ledger and back-fills anything the webhook channel missed — a *pull* safety net complementing the *push* of webhooks.

## Payouts (outbound) — design

- A **transactional outbox**: the payout intent is written in the same database transaction as the ledger entry, and a dispatcher calls `POST /transfers/bank` with a unique **`merchantTxRef` idempotency key** after a `POST /transfers/bank/lookup` name check. Settlement is confirmed by `transfer.success` / `transfer.failed` webhooks (or `GET /transfers/{merchantTxRef}`).

## Data model (core tables)

`merchants`, `customers`, `virtual_accounts`, `webhook_events` (the inbox; unique `request_id`), `ledger_entries` (immutable double-entry), `misdirected_payments` (resolution queue). Schema is managed by **Liquibase**; the application never mutates schema (Hibernate `ddl-auto: none`).

## Data handling & security

- **Money is stored as integer minor units (kobo)** — never floating point.
- **Secrets** (client secret, webhook secret) come from environment variables and are excluded from version control.
- **PII** (BVN, customer contact details) is minimized and kept out of application logs; Nomba masks BVN in responses and Kolekta mirrors that discipline.
- The **ledger is immutable and double-entry**, so balances are always reconstructable and tamper-evident — a full audit trail of every transaction.
- A **global exception handler** returns correct HTTP status codes (e.g. 404 / 409) rather than leaking 500s.

## Stack

Java 21 · Spring Boot · PostgreSQL · Liquibase · Docker (backend) · Angular 21 (frontend). Nomba APIs used: Authentication, Virtual Accounts, Webhooks, Transactions, Transfers, Sub-accounts.

## Running locally

1. Start PostgreSQL (Docker): `docker start kolekta-postgres` (or the documented `docker run`).
2. Provide sandbox credentials via environment variables (`NOMBA_ACCOUNT_ID`, `NOMBA_SUB_ACCOUNT_ID`, `NOMBA_CLIENT_ID`, `NOMBA_CLIENT_SECRET`, `NOMBA_WEBHOOK_SECRET`).
3. Backend: `mvn spring-boot:run` (Liquibase applies the schema on boot).
4. Frontend: `cd frontend && ng serve --proxy-config proxy.conf.json`.
5. Health check: `GET /actuator/health`.