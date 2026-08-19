# Food Permit Full-Bag Swap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When Food Permit extracts food into a full maid inventory (backpack + hands), exchange via a call-stack buffer so chest items never vanish; if the dump item cannot fit without clearing the food slot, take the whole food stack.

**Architecture:** Extract pure decision logic into `FoodPermitSwapPolicy` (unit-tested). Rewrite `FoodPermitTransfer` swap to be transactional (simulate or snapshot+rollback), include main/offhand as dump/receive slots, and use whole-stack take when needed. Picnic path already uses `extractToBackpack` then pending move — fixing transfer covers both.

**Tech Stack:** Java 17, Minecraft 1.20.1 Forge, existing `FoodPermitTransfer` / `FoodPermitTargets` / JUnit 5 unitTest source set.

## Global Constraints

- Scope: Food Permit → backpack and picnic-refill-via-backpack only; **not** Snack Raid.
- Never lose items: failed swap must leave chest and maid unchanged.
- Dump candidates: backpack + main hand + offhand.
- Dump priority: (1) food not in effective/request pool → (2) other non-tools → (3) tools (soft last resort).
- Whole-stack take may exceed configured `maxTake` (5/16) for that step.
- Buffer is call-stack only — no NBT persistence.
- After feature work: append bilingual entry under current version in `版本改动.md`.

---

## File map

| File | Role |
|------|------|
| `src/main/java/.../food/FoodPermitSwapPolicy.java` | Pure ranking / take-count / need-whole-stack decisions |
| `src/unitTest/java/.../food/FoodPermitSwapPolicyTest.java` | Unit tests for policy |
| `src/main/java/.../food/FoodPermitTransfer.java` | Transactional extract + swap (hands included) |
| `版本改动.md` | Player-facing changelog |
| `下一步的计划-取粮满包交换.md` | Check off M1–M3 when done |

---

### Task 1: Swap policy + unit tests

**Files:**
- Create: `src/main/java/com/touhoumaid/eatproperly/food/FoodPermitSwapPolicy.java`
- Create: `src/unitTest/java/com/touhoumaid/eatproperly/food/FoodPermitSwapPolicyTest.java`

**Interfaces:**
- Produces:
  - `enum DumpTier { NON_POOL_FOOD, OTHER_NON_TOOL, TOOL }`
  - `static DumpTier classifyDump(boolean isFood, boolean inEffectivePool, boolean isTool)`
  - `static int compareDumpPriority(DumpTier a, DumpTier b)` — lower rank number = higher priority (`NON_POOL_FOOD` best)
  - `static int decideTakeCount(int stackCount, int maxTake, boolean needWholeStack)` — returns `stackCount` if `needWholeStack`, else `Math.min(stackCount, maxTake)` (at least 1 if stackCount>0)
  - `static boolean needWholeStackToFitDump(boolean dumpFitsWithoutClearingFoodSlot)` — `true` when dump does **not** fit without clearing the food slot

- [ ] **Step 1: Write the failing tests**

```java
package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FoodPermitSwapPolicyTest {
    @Test
    void dumpTierOrdersNonPoolFoodThenOtherThenTool() {
        assertTrue(FoodPermitSwapPolicy.compareDumpPriority(
                FoodPermitSwapPolicy.DumpTier.NON_POOL_FOOD,
                FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL) < 0);
        assertTrue(FoodPermitSwapPolicy.compareDumpPriority(
                FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.DumpTier.TOOL) < 0);
    }

    @Test
    void classifyMatchesAgreedRules() {
        assertEquals(FoodPermitSwapPolicy.DumpTier.NON_POOL_FOOD,
                FoodPermitSwapPolicy.classifyDump(true, false, false));
        assertEquals(FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.classifyDump(true, true, false)); // pool food
        assertEquals(FoodPermitSwapPolicy.DumpTier.OTHER_NON_TOOL,
                FoodPermitSwapPolicy.classifyDump(false, false, false)); // block/misc
        assertEquals(FoodPermitSwapPolicy.DumpTier.TOOL,
                FoodPermitSwapPolicy.classifyDump(false, false, true));
    }

    @Test
    void takeCountUsesMaxTakeUnlessWholeStackRequired() {
        assertEquals(5, FoodPermitSwapPolicy.decideTakeCount(64, 5, false));
        assertEquals(64, FoodPermitSwapPolicy.decideTakeCount(64, 5, true));
        assertEquals(3, FoodPermitSwapPolicy.decideTakeCount(3, 5, false));
    }

    @Test
    void wholeStackNeededWhenDumpDoesNotFitWithoutClearingFoodSlot() {
        assertTrue(FoodPermitSwapPolicy.needWholeStackToFitDump(false));
        assertFalse(FoodPermitSwapPolicy.needWholeStackToFitDump(true));
    }
}
```

- [ ] **Step 2: Run tests — expect FAIL (class missing)**

Run: `.\gradlew.bat unitTest --tests com.touhoumaid.eatproperly.food.FoodPermitSwapPolicyTest`

- [ ] **Step 3: Implement `FoodPermitSwapPolicy`**

```java
package com.touhoumaid.eatproperly.food;

public final class FoodPermitSwapPolicy {
    public enum DumpTier { NON_POOL_FOOD, OTHER_NON_TOOL, TOOL }

    private FoodPermitSwapPolicy() {}

    public static DumpTier classifyDump(boolean isFood, boolean inEffectivePool, boolean isTool) {
        if (isTool) {
            return DumpTier.TOOL;
        }
        if (isFood && !inEffectivePool) {
            return DumpTier.NON_POOL_FOOD;
        }
        return DumpTier.OTHER_NON_TOOL;
    }

    /** Negative = a better than b. */
    public static int compareDumpPriority(DumpTier a, DumpTier b) {
        return Integer.compare(a.ordinal(), b.ordinal());
    }

    public static int decideTakeCount(int stackCount, int maxTake, boolean needWholeStack) {
        if (stackCount <= 0) {
            return 0;
        }
        if (needWholeStack) {
            return stackCount;
        }
        return Math.min(stackCount, Math.max(1, maxTake));
    }

    public static boolean needWholeStackToFitDump(boolean dumpFitsWithoutClearingFoodSlot) {
        return !dumpFitsWithoutClearingFoodSlot;
    }
}
```

- [ ] **Step 4: Re-run unitTest — expect PASS**

- [ ] **Step 5: Check off M1 in `下一步的计划-取粮满包交换.md`**

Do **not** commit unless the user asks.

---

### Task 2: Transactional `FoodPermitTransfer` swap

**Files:**
- Modify: `src/main/java/com/touhoumaid/eatproperly/food/FoodPermitTransfer.java`
- Uses: `FoodPermitSwapPolicy`, `FoodPermitTargets.isEffectivePoolFood`, maid `getMaidInv()`, `getMainHandItem()` / `getOffhandItem()` (set via `setItemInHand` / `setItemSlot` as appropriate for `EntityMaid`)

**Interfaces:**
- Consumes: policy methods from Task 1
- Keeps public API: `extractToBackpack(BlockEntity, EntityMaid, int maxTake)` return total moved count
- Internal: one successful swap/insert step may move `decideTakeCount(...)` items (can be >1 when whole-stack)

**Behavior requirements:**

1. **Direct insert** into maid inventory (backpack first via existing `ItemHandlerHelper.insertItemStacked(maid.getMaidInv(), ...)`; if that fails, try empty main/offhand) for up to remaining `maxTake` — prefer keeping current per-item loop or batch by stack; must not extract from chest before a successful place is guaranteed (or rollback).
2. **Swap path** when full:
   - Enumerate dump candidates: all non-empty backpack slots + main + offhand.
   - Sort/scan by `DumpTier` (stable: try all NON_POOL_FOOD before OTHER before TOOL).
   - For each dump candidate + each effective-pool food chest slot:
     - `needWhole = needWholeStackToFitDump(simulateStash(dump, skipFoodSlot=true))`
     - `take = decideTakeCount(foodCount, remainingMaxTake, needWhole)` — note: when `needWhole`, take full stack even if `remainingMaxTake` is smaller; when not whole-stack, clamp to remaining budget.
     - Simulate full transaction: remove `take` food → buffer; remove dump from maid; insert dump into chest (using emptied food slot if whole-stack); place buffer into freed maid slot. If any step fails simulation → try next candidate. On success → commit once.
   - If no candidate works → return 0 for that attempt (outer loop stops); **no mutation**.
3. **Hands:** dump and receive both allowed on main/offhand.
4. **Tool detection:** treat as tool if item is `DiggerItem`, `SwordItem`, `BowItem`, `CrossbowItem`, `TridentItem`, `ShieldItem`, or `ShearsItem` (keep list local private helper). Food = `stack.isEdible()`.
5. **`extractToItemHandler`:** if insert leaves leftover, **put leftover back** into the source container (or never extract without simulate). Must not void items.
6. Update class javadoc: exchange is implemented; items must not vanish.

- [ ] **Step 1: Implement helper stubs + rewrite `moveOne` / swap** following requirements above (snapshot lists of `ItemStack.copy()` for maid inv + hands + container slots, mutate, on failure restore snapshots).

- [ ] **Step 2: Run `.\gradlew.bat unitTest`** — all existing + Task 1 tests pass.

- [ ] **Step 3: Run `.\gradlew.bat compileJava`** — expect SUCCESS.

- [ ] **Step 4: Append to `版本改动.md` under current `mod_version` (1.3.0 unless bumped):**

```markdown
- 修复取粮许可在背包与双手已满时从箱子取走食物却消失的问题；满载时通过缓冲交换腾槽，双满则整槽取食以保证可交换
 - Fixed Food Permit vanishing items when backpack and hands are full; uses buffered slot swap, and whole-stack take when both sides are full so the exchange can complete
```

- [ ] **Step 5: Check off M2 in `下一步的计划-取粮满包交换.md`**

Do **not** commit unless the user asks.

---

### Task 3: Build verify + hand-test checklist

**Files:**
- None required beyond confirming docs checkboxes

- [ ] **Step 1: `.\gradlew.bat build`** — SUCCESS; jar lands in `releases/`.

- [ ] **Step 2: Mark M3 automated parts done; leave hand-test boxes for the player:**
  - Full bag+hands, chest has free space → swap OK
  - Full bag+hands, full chest, food stack >1 → whole-stack swap OK
  - Unswappable (filter) → no change either side

- [ ] **Step 3: Check off completed M3 items in `下一步的计划-取粮满包交换.md`**

---

## Spec coverage check

| Spec item | Task |
|-----------|------|
| Buffer swap when full | T2 |
| Hands in dump/receive | T2 |
| Priority non-pool → other → tool | T1+T2 |
| Whole-stack when dump needs cleared food slot | T1+T2 |
| Fail → full rollback | T2 |
| Backpack + picnic-via-backpack | T2 (`extractToBackpack`) |
| Not snack raid | out of scope |
| Changelog | T2 |
| No NBT buffer | T2 (call-stack only) |
