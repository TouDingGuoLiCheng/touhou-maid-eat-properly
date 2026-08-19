# Out-of-Pool Delivery & Anger Implementation Plan

> **For agentic workers:** Use executing-plans or subagent-driven-development. Steps use `- [ ]`.

**Goal:** Reject-bubble + count out-of-effective-pool player deliveries; escalate to anger that blocks maid GUI until forgive or 12000 ticks.

**Architecture:** Pure `OutOfPoolAngerPolicy` state machine + maid NBT `OutOfPoolAngerData` + `OutOfPoolDelivery` facade called from lunchbox/dual/beg/backpack-click hooks; `InteractMaidEvent` blocks non-delivery GUI while angry.

**Tech Stack:** Forge 1.20.1, TLM InteractMaidEvent/AbstractMaidContainer, JUnit unitTest sourceSet.

**Spec:** `下一步的计划-池外投递与生气.md`

## Global Constraints

- Effective pool via `FoodPool.isInEffectivePool` + Order Books
- Window: first out-of-pool stow → +2000; each further successful out-of-pool stow → count+=n, windowEnd+=1000; count≥10 → anger 12000
- While angry: out-of-pool must not stow; refresh anger 12000; in-pool forgive; empty-hand GUI blocked with angry bubble cooldown ~80 ticks
- Beg right-click out-of-pool stow only while begging
- Speech defaults per spec; config lists

---

### Task 1: Policy + unit tests

**Files:**
- Create: `src/unitTest/java/.../food/OutOfPoolAngerPolicyTest.java`
- Create: `src/main/java/.../food/OutOfPoolAngerPolicy.java`

- [ ] RED/GREEN: window start 2000, extend +1000, threshold 10 → anger, expiry clear, angry reject refresh, forgive clear

### Task 2: Data + config + speech

**Files:**
- Create: `OutOfPoolAngerData.java`
- Modify: `EatProperlyConfig.java`, `MaidSpeech.java`

- [ ] NBT keys; config ticks + 3 speech lists; format helpers

### Task 3: Delivery facade + lunchbox/beg hooks

**Files:**
- Create: `OutOfPoolDelivery.java` (apply after/before stow)
- Modify: `LunchboxEvents`, `CareItemEvents`, `LowSatietyBegDeliveryEvents`

- [ ] Not angry out-of-pool: stow + refuse + count; angry: no stow + refuse + refresh; in-pool while angry: forgive

### Task 4: Backpack GUI + block open GUI

**Files:**
- Create: mixin or click watcher on `AbstractMaidContainer`
- Create: `OutOfPoolAngerEvents` for GUI block

- [ ] Detect player insert into maid inv; angry block GUI with cooldown bubble

### Task 5: Changelog + mark plan acceptance

- [ ] `版本改动.md`; check M1–M4 boxes in 下一步的计划
