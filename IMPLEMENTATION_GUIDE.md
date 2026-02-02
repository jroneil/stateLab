# Implementing Server-Authoritative Workflow State in Your Project

This guide outlines how to move from "Blind CRUD" to a robust **Server-Authoritative State Machine** pattern using Spring Boot and Next.js.

## When to use this pattern
Use this for domains where the order of operations matters and data integrity is high-stakes:
*   **Approvals & Claims** (Insurance, Finance)
*   **Onboarding & Compliance**
*   **Multi-step Forms**
*   **Inventory & Order Fulfillment**

---

## 1. Core Building Blocks

### A. The State Enum & Persisted Field
Defines your discrete lifecycle stages.
```java
public enum ClaimState { DRAFT, SUBMITTED, APPROVED, REJECTED }
```

### B. The State Machine (The Logic Room)
A centralized component that knows which `Action` moves `State A` to `State B`. Do not scatter this logic in services or controllers.

### C. The Transition Endpoint
Replace `PUT /claims/{id}` with a specific intent-based endpoint.
*   **Request:** `{ "action": "APPROVE", "notes": "Looks good" }`
*   **Flow:** Validate current state → Run Guards → Calculate Next State → Save.

### D. Optimistic Concurrency (ETags)
Enable standard JPA versioning.
```java
@Version 
private Long version; // Exposed as ETag in headers
```

---

## 2. Step-by-Step Adoption Plan

1.  **Phase 1: The Transition Shadow:** Add the `/transitions` endpoint and the backend logic without changing your existing UI. Prove the state rules work.
2.  **Phase 2: Intent-based UI:** Switch your frontend buttons to call `/transitions` instead of updating the state field directly via `PUT`.
3.  **Phase 3: Server-Driven Buttons:** Update your GET response to include `allowedActions: ["SUBMIT", "CANCEL"]`. Let the server tell the UI what is possible.
4.  **Phase 4: Conflict Hardening:** Start passing the `ETag` and requiring `If-Match`. Add a global error handler in the UI for `409 Conflict`.

---

## 3. Minimal Example

### The Request (Intent)
```http
POST /claims/123/transitions
If-Match: "5"  <-- The version you last saw

{ "action": "SUBMIT" }
```

### The Response (Updated State + Options)
```json
{
  "id": "123",
  "state": "SUBMITTED",
  "version": 6,
  "allowedActions": ["APPROVE", "REJECT"]
}
```

---

## 4. Common Pitfalls
*   **UI Setting State:** Never allow `PUT { "state": "APPROVED" }`. This bypasses all guards.
*   **Hiding Rules in Controllers:** Keep transition logic in a dedicated state machine or domain service for testability.
*   **Missing Version Checks:** If you skip `If-Match`, Two users will overwrite each other, causing "Lost Updates."
*   **Overusing Redis:** For workflow transitions, use your primary ACID-compliant database (PostgreSQL) as the source of truth for state.

---

## 5. Frontend Guidance
*   **Reflect, Don't Infer:** Don't write `if(state === 'DRAFT') showButton()`. Use the backend's `allowedActions`.
*   **Handle 409s:** When a `409 Conflict` occurs, stop the flow and show a "Stale Data: Refresh" prompt.
*   **Block Duplicate Clicks:** Always disable the button immediately after the first click to prevent "double-submission" race conditions.

---

## 6. Checklist: Did you actually implement server-authoritative state?

1.  [ ] Does the backend reject a jump from the first state to the last state?
2.  [ ] Is there a single class/component that defines the state transition map?
3.  [ ] Do you use `@Version` and `If-Match` for every state-changing request?
4.  [ ] Does your GET response include a list of currently valid next actions?
5.  [ ] Can you find a specific record in your DB that shows *why* a transition failed?
6.  [ ] Does the UI render buttons dynamically based on the server's `allowedActions`?
7.  [ ] If a user manually changes the state string in the browser console and clicks save, does the server block the update?
8.  [ ] Are guards (like "notes required for rejection") enforced on the server-side?
9.  [ ] Does every transition create an audit entry/event record?
10. [ ] When a transition fails, is the error message clear about the rule violation?
