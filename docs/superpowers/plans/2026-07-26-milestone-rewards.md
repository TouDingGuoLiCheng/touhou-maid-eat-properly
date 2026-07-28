# Milestone Rewards (里程碑回礼) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> **Execution gates (project override):** One task at a time. After each task closes, write `.superpowers/sdd/2026-07-26-milestone-rewards/progress.md` immediately and **STOP** until the human explicitly continues. Do not batch-dispatch remaining tasks. Ledger writes are real-time (`started` / implementer status / review / fix round / `complete` / `awaiting human continue`) — never deferred to plan end.

**Goal:** Ship per-maid milestone rewards (token / care / buff pools), claim UI on the Eat Properly tab, player 3-slot trinkets, and item behaviors defined in `下一步的计划-好好吃饭里程碑回礼.md`.

**Architecture:** Reuse maid/player `PersistentData` root `TouhouMaidEatProperly` (no Forge Capability). Favor level changes already hit `MilestoneRegistry.onFavorLevelChange` via `MilestoneEvents`. Claim state + locked draws live on the maid; buff items read from a player (and optionally maid) trinket inventory stored on player PersistentData + ItemStack NBT for lunchboxes. Item icons stay placeholder until PixelLab; handwritten letter opens like vanilla written book.

**Tech Stack:** Minecraft 1.20.1 Forge, Touhou Little Maid 1.5.3+, existing DeferredRegister / AbstractMaidContainerGui patterns, soft dep `youkaishomecoming`.

**Spec (source of truth):** `下一步的计划-好好吃饭里程碑回礼.md` especially §1–§3.6.

## Global Constraints

- Favor milestones: levels `5,10,20,30,40,50,60,70,80,90,100` with types 信物/养成/增益 as in spec table.
- Per-maid once per level; no duplicate item id from same maid; pool empty → chat/bubble thanks only, no filler item.
- Claim only via Eat Properly GUI (manual).
- 信物 lore: bind source maid UUID + snapshot display name; texts from §3.6 (do not rewrite tone).
- Icons: **placeholder textures only** this phase; **do not call PixelLab**. Shared 16×16 stub is fine until a later art pass.
- Hand letter: vanilla book-and-quill / written-book style UI.
- Trinkets: player 3 slots; stack policy per item (`additive` / `unique` / `max`).
- Lunchboxes and contained foods: non-stackable (`stacksTo(1)`).
- Commits: **only when the user explicitly asks** (project rule overrides per-task commit habit).
- Empty equip slots / cooldown values: ask user at implement time if unspecified.

---

## File map (create / modify)

| Responsibility | Path |
|----------------|------|
| Milestone NBT + draw/claim API | Create `milestone/MilestoneData.java` |
| Pool defs + pick | Create `milestone/RewardPools.java`, `milestone/RewardCategory.java` |
| Wire level-up → pending claim | Modify `milestone/MilestoneRegistry.java` |
| Claim networking | Create `network/ClaimMilestoneRewardPacket.java` (+ register in mod) |
| Config defaults for levels→category | Modify `config/EatProperlyConfig.java` |
| GUI claim list | Modify `client/EatProperlyScreen.java`, `inventory/EatProperlyContainer.java` |
| Items registry | Modify `item/ModItems.java`, create item classes under `item/reward/` |
| Named lunchboxes | Create `item/reward/BoundLunchboxItem.java`, `DualLunchboxItem.java` |
| Care tools | Create `item/reward/ExtraMealPermitItem.java`, `HypnosisNoteItem.java` |
| Player trinket inv | Create `trinket/PlayerTrinketData.java`, `trinket/TrinketContainer.java`, `trinket/TrinketScreen.java` |
| Buff tick / events | Create `trinket/TrinketEffects.java` |
| Happiness effect | Create `effect/ModEffects.java`, `effect/HappinessEffect.java` |
| Written letter | Create `item/reward/HandwrittenLetterItem.java` (WrittenBook-like) |
| Lang | Modify `assets/.../lang/zh_cn.json`, `en_us.json` |
| Adv names 40↔60 | Same lang files |
| Models/textures placeholder | `assets/.../models/item/*.json`, `textures/item/*.png` |
| Docs | Modify `使用说明.md` later (Task polish) |
| Soft dep lake fairy | Create `compat/YoukaiHomecomingCompat.java` |

---

### Task 1: Swap achievement titles 40 ↔ 60

- [x] **Done by user** (zh/en verified 2026-07-26)

---

### Task 2: MilestoneData + RewardPools (pure logic)

**Files:**
- Create: `src/main/java/com/touhoumaid/eatproperly/milestone/MilestoneData.java`
- Create: `src/main/java/com/touhoumaid/eatproperly/milestone/RewardCategory.java`
- Create: `src/main/java/com/touhoumaid/eatproperly/milestone/RewardPools.java`
- Create: `src/test/java/com/touhoumaid/eatproperly/milestone/RewardPoolsTest.java` (add junit to `build.gradle` `test` if missing)

**Interfaces:**
- Produces:
  - `enum RewardCategory { TOKEN_LOW, TOKEN_MID, TOKEN_HIGH, CARE, BUFF }`
  - `RewardPools.categoryForLevel(int level) -> Optional<RewardCategory>`
  - `MilestoneData` methods on `EntityMaid`:
    - `ListTag getPendingClaims()` / `void addPending(int level, ResourceLocation itemId)`
    - `boolean hasClaimedLevel(int level)`
    - `Set<ResourceLocation> getObtainedItems()`
    - `void markClaimed(int level, ResourceLocation itemId)`
  - `RewardPools.pick(RewardCategory cat, Set<ResourceLocation> exclude, RandomSource random) -> Optional<ResourceLocation>`

- [x] **Step 1: Add test dependency if needed**

In `build.gradle` ensure:
```gradle
test {
    useJUnitPlatform()
}
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}
```

- [x] **Step 2: Write failing test for level→category and exclude pick**

```java
@Test
void level50IsBuff() {
    assertEquals(RewardCategory.BUFF, RewardPools.categoryForLevel(50).orElseThrow());
}

@Test
void pickExcludesObtained() {
    Set<ResourceLocation> exclude = Set.of(new ResourceLocation("touhou_maid_eat_properly", "heart_seasoning"));
    // after items registered in pool list as strings for unit test, assert never returns excluded
}
```

(For unit test without full registry, keep pool as `List<String>` path ids inside `RewardPools`.)

- [x] **Step 3: Implement `RewardCategory`, hardcoded level map, `RewardPools.pick`**

Hardcode map from spec:
`5 TOKEN_LOW, 10 CARE, 20 BUFF, 30 TOKEN_LOW, 40 CARE, 50 BUFF, 60 TOKEN_MID, 70 CARE, 80 BUFF, 90 TOKEN_HIGH, 100 TOKEN_HIGH`.

Pool membership lists: use item registry path strings matching Task 4 ids (define the id list here first; Task 4 must use the same ids).

- [x] **Step 4: Implement `MilestoneData` on maid PersistentData root `TouhouMaidEatProperly`**

Suggested keys:
- `MilestonePending` — ListTag of compounds `{Level:int, Item:string}`
- `MilestoneClaimedLevels` — IntArray
- `MilestoneObtained` — ListTag of strings (item ids)

- [x] **Step 5: Run unit tests**

Run: `gradlew.bat test --tests com.touhoumaid.eatproperly.milestone.RewardPoolsTest`  
Expected: PASS

---

### Task 3: Wire level-up → lock pending reward

**Files:**
- Modify: `src/main/java/com/touhoumaid/eatproperly/milestone/MilestoneRegistry.java`
- Modify: `src/main/java/com/touhoumaid/eatproperly/food/MaidSpeech.java` (optional thanks line)

**Interfaces:**
- Consumes: `MilestoneData`, `RewardPools`
- Produces: pending entries when crossing milestone levels upward

- [x] **Step 1: Implement `onFavorLevelChange`**

```java
public static void onFavorLevelChange(EntityMaid maid, int oldLevel, int newLevel) {
    if (newLevel <= oldLevel) return;
    for (int level = oldLevel + 1; level <= newLevel; level++) {
        var cat = RewardPools.categoryForLevel(level);
        if (cat.isEmpty()) continue;
        if (MilestoneData.hasClaimedLevel(maid, level)) continue;
        if (MilestoneData.hasPendingLevel(maid, level)) continue;
        var item = RewardPools.pick(cat.get(), MilestoneData.getObtainedItems(maid), maid.getRandom());
        if (item.isEmpty()) {
            // owner-only speech: thanks only
            MaidSpeech.sayToOwner(maid, "speech.touhou_maid_eat_properly.milestone_empty");
            MilestoneData.markClaimedEmpty(maid, level); // prevent re-roll spam
            continue;
        }
        MilestoneData.addPending(maid, level, item.get());
        MaidSpeech.sayToOwner(maid, "speech.touhou_maid_eat_properly.milestone_ready");
    }
}
```

- [ ] **Step 2: Manual verify with commands**

Run world with `/ep favor setlevel 0` then `/ep favor setlevel 5` on a maid.  
Expected: pending exists in NBT (`data get entity ... ForgeData.TouhouMaidEatProperly`).

> Needs human verification: automated compile/tests cover the code path, but this session did not launch a game client.

---

### Task 4: Register all reward items (placeholders)

**Files:**
- Modify: `item/ModItems.java`, `item/ModCreativeTabs.java`
- Create: stub item classes under `item/reward/` (can start as plain `Item` with `stacksTo(1)` where needed)
- Create: placeholder `models/item/*.json` + copy a 16×16 PNG placeholder for each
- Modify: `lang/zh_cn.json`, `en_us.json` — names + lore keys from §3.6

**Interfaces:**
- Produces: `RegistryObject<Item>` for every pool id used in `RewardPools`
- Suggested ids (snake_case), keep in sync with pools:

**TOKEN_LOW:** `forget_me_not_bouquet`, `praise_certificate`, `thanks_note_empty_box`, `paper_crane`, `meal_bell`, `thanks_candy`, `half_strawberry_cake`, `field_photo`, `banner_honest_boss`  
**Derived:** `crumpled_candy_wrapper`, `cake_note`  
**TOKEN_MID:** `heart_gem`, `handwritten_letter`, `moonlight_shard`, `two_tone_hair_tie`, `embroidered_handkerchief`, `paired_cup`, `unopened_souvenir`, `pressed_diary_page`, `block_figurine`, `silver_bell_charm`  
**TOKEN_HIGH:** `warm_amulet`, `eternal_meal_ticket`, `同心结` → `同心结` id `同心` no — use `同心结` as `heart_knot`, `sugar_jar`, `unnamed_ring`, `plain_pouch`  
**CARE:** `bound_lunchbox`, `dual_lunchbox`, `extra_meal_permit`, `hypnosis_note`  
**BUFF:** `heart_seasoning`, `insulated_meal_bag`, `moon_apple_candy`, `scarlet_amulet`, `night_sparrow_wrapper`, `sweet_soft_candy`, `onsen_manju_bag`, `sobering_raisin`, `lake_fairy_keepsake`

- [x] **Step 1: Register DeferredRegister entries + creative tab**
- [x] **Step 2: Add lang entries from §3.6** (use `item.touhou_maid_eat_properly.<id>` + lore as `*.lore` lines with `%s` for maid name where needed)
- [x] **Step 3: Placeholder models pointing at one shared texture**
- [x] **Step 4: `gradlew.bat build`** — Expected: BUILD SUCCESSFUL; items appear in creative tab

---

### Task 5: Claim UI on Eat Properly tab + packet

**Files:**
- Create: `network/ModNetwork.java`, `network/ClaimMilestoneRewardPacket.java`
- Modify: `EatProperlyMod.java` (register network)
- Modify: `client/EatProperlyScreen.java`
- Modify: `inventory/EatProperlyContainer.java` (sync pending list to client via DataSlot or custom payload)

**Interfaces:**
- Produces: `ClaimMilestoneRewardPacket(int maidEntityId, int level)` server handler:
  1. Validate owner + pending
  2. Give `ItemStack` with maid bind NBT (`SourceMaidUuid`, `SourceMaidName`)
  3. `MilestoneData.markClaimed`
  4. If inventory full, drop at player

- [x] **Step 1: Network register + packet**
- [x] **Step 2: Container sync pending rewards to client**
- [x] **Step 3: Screen draws list + claim button**
- [ ] **Step 4: In-game: reach level 5, open tab, claim, receive item with lore name** — 待人工进游戏验收

---

### Task 6: Care tools behavior

**Files:**
- Create/Modify: `item/reward/BoundLunchboxItem.java` (extends lunchbox; UUID bind on grant; reject other maids)
- Create: `item/reward/DualLunchboxItem.java` (2 slots; non-stackable food; complete if either matches request; other must be in `FoodPool`; +2 favor levels)
- Create: `item/reward/ExtraMealPermitItem.java` (right-click maid → force request / reroll)
- Create: `item/reward/HypnosisNoteItem.java` (forget state in `FoodRequestData`; GUI shows `?`)
- Modify: `food/FoodRequestData.java`, `food/FoodRequestHandler.java`, `client/EatProperlyScreen.java`

**Interfaces:**
- `FoodRequestData.setForgotten(boolean)` / `isForgotten()`
- Dual lunchbox complete → `FavorabilityManager` add enough points for **+2 levels** (use `FavorabilityExtension.getPointByLevel`)

- [ ] **Step 1: Extend FoodRequestData with Forgotten flag**
- [ ] **Step 2: Bound + dual lunchbox deliver paths** (mirror `LunchboxEvents`)
- [ ] **Step 3: Extra meal permit + hypnosis note useOn entity**
- [ ] **Step 4: Screen shows `?` + lang `gui.touhou_maid_eat_properly.request_forgotten` = `{0}好像忘记了这次想吃什么…`
- [ ] **Step 5: Manual tests via `/ep request` + items**

---

### Task 7: Edible tokens + Happiness effect

**Files:**
- Create: `effect/ModEffects.java`, `effect/HappinessEffect.java`
- Modify: thanks candy / half cake item classes (`FinishUsingItem` → effect + replace with wrapper/note stacks copying bind NBT)

**Interfaces:**
- Happiness: duration 12000 ticks; each tick (or every N ticks) restore hunger toward max; `unique` refresh
- Candy: nutrition restoring **5** hunger; cake **10**

- [ ] **Step 1: Register MobEffect + Brewing unused**
- [ ] **Step 2: Candy/cake `finishUsingItem`**
- [ ] **Step 3: In-game eat → effect + derived item**

---

### Task 8: Handwritten letter (written book UI)

**Files:**
- Create: `item/reward/HandwrittenLetterItem.java`
- On grant/claim: build `ItemStack` like written book (`tag.pages`, `title`, `author` = maid name) OR custom item that opens `BookViewScreen` with pages from §3.6 template

**Preferred approach:** On claim, create a `Items.WRITTEN_BOOK` stack with pages from template (simplest UX match). If custom item required for pool id, custom item `use` copies book open behavior.

- [ ] **Step 1: Helper `Letters.create(maidName)` → ItemStack**
- [ ] **Step 2: Claim path uses helper for `handwritten_letter`**
- [ ] **Step 3: Right-click opens book UI; content matches §3.6**

---

### Task 9: Player 3-slot trinket inventory + GUI

**Files:**
- Create: `trinket/PlayerTrinketData.java` (PersistentData on player, 3 slots `ItemStackHandler`)
- Create: `inventory/TrinketContainer.java`, `client/TrinketScreen.java`
- Create: `item/TrinketPouchOpener` or key/command `/ep trinkets` + item “饰品袋” if needed — **ask user for open method if unspecified; default `/ep trinkets` + button on Eat Properly screen “饰品”**
- Death: copy handler like inventory (drop on death unless gamerule keepInventory)

- [ ] **Step 1: PlayerTrinketData get/set**
- [ ] **Step 2: Menu + screen 3 slots; only items tagged `touhou_maid_eat_properly:trinkets` (item tag JSON)**
- [ ] **Step 3: Open from Eat Properly screen button + `/ep trinkets`**
- [ ] **Step 4: Survive relog**

---

### Task 10: Buff item effects + stack policies

**Files:**
- Create: `trinket/TrinketEffects.java` (player tick + LivingHurtEvent etc.)
- Create: `trinket/StackPolicy.java` enum
- Tag buff items in `data/.../tags/items/trinkets.json`
- Maid equip: for items allowed on maid, also check TLM bauble slots if API available; otherwise player-only first — **ask user when implementing maid side if TLM bauble insert is unclear**

**Effect checklist (from spec):**

| Item | Behavior | Policy |
|------|----------|--------|
| heart_seasoning | +1 hunger/satiety on eat | additive |
| insulated_meal_bag | −40% natural drain | max |
| moon_apple_candy | ~20 / day restored, per game-hour accrual | unique |
| scarlet_amulet | MAX_HEALTH `multiply_base` 0.1 | unique |
| night_sparrow_wrapper | Night Vision | unique |
| sweet_soft_candy | brief Absorption on eat | unique |
| onsen_manju_bag | immune powder-snow freeze | unique |
| sobering_raisin | negative effect duration ×0.5 | unique |
| lake_fairy_keepsake | TLM fairy maid ignore; youkai compat | unique |
| warm_amulet (token) | fire immune when equipped — slot TBD ask user | unique |
| silver_bell_charm | Speed I when worn | unique |
| meal_bell | summon bound maid on use | n/a |

- [ ] **Step 1: Attribute modifier for scarlet amulet (dynamic %)**
- [ ] **Step 2: Tick hunger restore for moon apple + bag drain reduction**
- [ ] **Step 3: Events for eat bonuses, freeze, effect duration**
- [ ] **Step 4: Fairy targeting cancel + `YoukaiHomecomingCompat`**
- [ ] **Step 5: Manual matrix test (two never-hunger-style uniques, two +HP)**

---

### Task 11: Meal bell summon + remaining token equip hooks

**Files:**
- `MealBellItem`: use → teleport/path bound maid to player (same dimension, cooldown ask user; default 60s)
- Equip hooks for moonlight_shard, hair_tie, silver_bell as trinket-tag or TLM bauble — **confirm with user at implement time**

- [ ] **Step 1: Bind UUID on claim for bell**
- [ ] **Step 2: Summon/teleport logic**
- [ ] **Step 3: Smoke test**

---

### Task 12: Polish — speech, docs, empty-pool message

**Files:**
- Lang speech keys for milestone ready / empty
- Modify: `使用说明.md`, optionally `模组介绍-上传用.md`
- Debug: `/ep milestone pending|claim` optional

- [ ] **Step 1: Lang + speech**
- [ ] **Step 2: Docs**
- [ ] **Step 3: Full ladder test 0→100 on two maids**
- [ ] **Step 4: Mark checkboxes in `下一步的计划-好好吃饭里程碑回礼.md` stages as done when verified**

---

## Spec coverage checklist

| Spec area | Task |
|-----------|------|
| Adv 40↔60 names | 1 |
| Level→type table + pools + dedupe + empty thanks | 2–3 |
| GUI claim | 5 |
| 信物/养成/增益 items + §3.6 lang | 4 |
| Care tools | 6 |
| Candy/cake/幸福 | 7 |
| Hand letter book UI | 8 |
| Player 3 trinket slots | 9 |
| Buff effects + stack policy | 10 |
| Lake fairy + youkaishomecoming | 10 |
| Meal bell | 11 |
| PixelLab icons | **Out of scope (deferred)** |
| Docs | 12 |

## Placeholder / ask-at-implement list

- Trinket GUI entry (default: Eat Properly button + `/ep trinkets`)
- Cooldowns for permit / hypnosis / meal bell
- Whether dual lunchbox is also maid-bound
- Which tokens go in player slots vs TLM maid baubles
- Happiness restore rate numbers

---

## Execution handoff

Plan saved to `docs/superpowers/plans/2026-07-26-milestone-rewards.md`.

**Two execution options:**

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
2. **Inline Execution** — run tasks in this session with executing-plans checkpoints  

**Which approach?**
