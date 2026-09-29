# BSD-28081 — Order status for Unknown line items (pending QA)

**Story:** [BSD-28081](https://paramount.atlassian.net/browse/BSD-28081)  
**Status:** Saved for later discussion — not automated yet  
**Category:** Manual QA scope (to be refined)

---

## Preconditions

| Item | Value |
|------|--------|
| Tab | **Orders** |
| Date | **Today** |
| Left filter | **Order status → Unknown** |
| Approach | Go through several orders and validate status rules below |

---

## Test cases (draft)

### Setup

1. Set calendar to **Today**.
2. Apply left filter **Order status → Unknown**.
3. Open / review multiple orders that match the filter.

### Case 1 — No line items

**When:** Order has **no line items**.

**Expected:** Order status should be **Failed**.

### Case 2 — Only Pnd or Delivery line items

**When:** Order has line items, but **only**:
- **Pnd** line item(s), **or**
- **Delivery** line item(s), **or**
- a mix of **only** Pnd + Delivery (no other line-item statuses)

**Expected:** Order status should be **In Progress**.

---

## Open questions (for later discussion)

- Exact UI labels for Pnd / Delivery line-item statuses on Orders vs expanded row?
- Does Case 2 apply when Unknown filter still shows the order?
- Automation vs manual-only for BSD-28081?
- Target environment: DEV / UAT / PROD?

---

## Related

- Left-filter regression: separate from this story
- In-sprint automation folder: TBD after scope is confirmed
