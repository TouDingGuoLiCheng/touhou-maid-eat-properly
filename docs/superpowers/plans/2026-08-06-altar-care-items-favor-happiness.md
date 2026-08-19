# 加餐/催眠祭坛化 + 高好感陪伴幸福 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将加餐许可与催眠便签改为祭坛合成并重排里程碑池；好感≥80 的女仆在主人 16 格内刷新「幸福」（3 分钟，离开不清）。

**Architecture:** 池表纯逻辑改档；两份 `altar_crafting` JSON；陪伴幸福用无 MC 的 policy（是否应刷新）+ 女仆 `LivingTickEvent` 挂载，复用 `ModEffects.HAPPINESS` 与 `OwnerProximityPolicy` / `MaidSpeech.OWNER_SPEECH_RANGE`。

**Tech Stack:** Forge 1.20.1、车万女仆祭坛配方、JUnit 5 unitTest、现有 Happiness / 里程碑代码。

**Spec:** `docs/superpowers/specs/2026-08-06-altar-care-items-favor-happiness-design.md`

## Global Constraints

- 模组版本：`1.3.0`（`gradle.properties`）；改动写入 `版本改动.md`（中英）
- 幸福刷新时长：**3600** tick；刷新阈值：**20** tick（对齐夜雀 `TRINKET_EFFECT_REFRESH_THRESHOLD_TICKS`）
- 距离：`MaidSpeech.OWNER_SPEECH_RANGE`（16.0）；离开**不** `removeEffect`
- 祭坛 `power`：`0.2`；配方类型：`touhou_little_maid:altar_crafting`
- 不改加餐/催眠使用逻辑；不改取粮许可；不新建第二套幸福效果
- **不要自动 commit**（除非用户明确要求）
- 工作区 SDD：一次只做一个 Task；Task 间等用户「继续」；开工前确认本会话/子代理

---

## File map

| 文件 | 职责 |
|------|------|
| `milestone/RewardCategory.java` | 删除 `CARE` |
| `milestone/RewardPoolTables.java` | 档位 10/40/70 + 池内容 |
| `unitTest/.../RewardPoolsTest.java` | 档位/池断言 |
| `effect/FavorHappinessPolicy.java` | 纯逻辑：是否应 refresh |
| `unitTest/.../FavorHappinessPolicyTest.java` | 策略单测 |
| `effect/FavorHappinessHandler.java` | 女仆 tick 挂载 |
| `effect/HappinessTiming.java` | 可选：常量 `PROXIMITY_DURATION_TICKS = 3600` |
| `item/ModItems.java` | 加餐/催眠改为非 `REWARD_ITEMS` 注册 |
| `item/ModCreativeTabs.java` | 显式放入创造页（若离开 REWARD_ITEMS） |
| `recipes/altar/craft_extra_meal_permit.json` | 新建 |
| `recipes/altar/craft_hypnosis_note.json` | 新建 |
| `版本改动.md` | 中英条目 |

---

### Task 1: 里程碑池重排（去掉 CARE）

**Files:**
- Modify: `src/main/java/com/touhoumaid/eatproperly/milestone/RewardCategory.java`
- Modify: `src/main/java/com/touhoumaid/eatproperly/milestone/RewardPoolTables.java`
- Modify: `src/unitTest/java/com/touhoumaid/eatproperly/milestone/RewardPoolsTest.java`
- Grep: 全仓库 `RewardCategory.CARE` / `"CARE"` 引用并清掉

**Interfaces:**
- Produces: `categoryForLevel(10|70) → TOKEN_MID`；`40 → BUFF`；`poolPaths(TOKEN_MID)` 含 `bound_lunchbox`；`poolPaths(BUFF)` 含 `dual_lunchbox`；无 `CARE`

- [x] **Step 1: 写失败单测**

在 `RewardPoolsTest` 增加/替换：

```java
@Test
void level10IsTokenMid() {
    assertEquals(RewardCategory.TOKEN_MID, RewardPoolTables.categoryForLevel(10).orElseThrow());
}

@Test
void level40IsBuff() {
    assertEquals(RewardCategory.BUFF, RewardPoolTables.categoryForLevel(40).orElseThrow());
}

@Test
void level70IsTokenMid() {
    assertEquals(RewardCategory.TOKEN_MID, RewardPoolTables.categoryForLevel(70).orElseThrow());
}

@Test
void tokenMidContainsBoundLunchbox() {
    assertTrue(RewardPoolTables.poolPaths(RewardCategory.TOKEN_MID).contains("bound_lunchbox"));
}

@Test
void buffContainsDualLunchbox() {
    assertTrue(RewardPoolTables.poolPaths(RewardCategory.BUFF).contains("dual_lunchbox"));
}

@Test
void poolsDoNotContainAltarCareItems() {
    for (RewardCategory cat : RewardCategory.values()) {
        assertFalse(RewardPoolTables.poolPaths(cat).contains("extra_meal_permit"));
        assertFalse(RewardPoolTables.poolPaths(cat).contains("hypnosis_note"));
    }
}
```

删除或改写依赖 `RewardCategory.CARE` 的 `pickEmptyWhenAllExcluded`（改为对 `TOKEN_MID` 或任意非空池测「全排除为空」）。

- [x] **Step 2: 跑测确认失败**

Run: `.\gradlew.bat unitTest --tests com.touhoumaid.eatproperly.milestone.RewardPoolsTest`  
Expected: 新断言 FAIL（档位仍为 CARE / 池无 lunchbox）

- [x] **Step 3: 最小实现**

`RewardCategory`：删除 `CARE`。

`RewardPoolTables`：

```java
levels.put(10, RewardCategory.TOKEN_MID);
levels.put(40, RewardCategory.BUFF);
levels.put(70, RewardCategory.TOKEN_MID);
// 删除 CARE 的 pools.put；TOKEN_MID 列表追加 "bound_lunchbox"；BUFF 追加 "dual_lunchbox"
```

- [x] **Step 4: 跑测通过**

Run: 同上  
Expected: PASS

- [x] **Step 5: 收尾** — 不 commit；更新本 plan 勾选；停，等用户继续

---

### Task 2: 祭坛配方 + 物品退出奖励列表

**Files:**
- Create: `src/main/resources/data/touhou_maid_eat_properly/recipes/altar/craft_extra_meal_permit.json`
- Create: `src/main/resources/data/touhou_maid_eat_properly/recipes/altar/craft_hypnosis_note.json`
- Modify: `src/main/java/com/touhoumaid/eatproperly/item/ModItems.java`
- Modify: `src/main/java/com/touhoumaid/eatproperly/item/ModCreativeTabs.java`
- Reference: `recipes/altar/craft_order_book.json`（格式模板）

**Interfaces:**
- Consumes: 现有 `ExtraMealPermitItem` / `HypnosisNoteItem` 类不变
- Produces: 祭坛可合成两物品；创造页仍可见；不在 `REWARD_ITEMS`

- [x] **Step 1: 写配方 JSON**

`craft_extra_meal_permit.json`：

```json
{
  "type": "touhou_little_maid:altar_crafting",
  "output": {
    "type": "minecraft:item",
    "nbt": {
      "Item": {
        "id": "touhou_maid_eat_properly:extra_meal_permit",
        "Count": 1
      }
    }
  },
  "power": 0.2,
  "ingredients": [
    { "item": "minecraft:paper" },
    { "item": "minecraft:leather" },
    { "item": "minecraft:ink_sac" }
  ]
}
```

`craft_hypnosis_note.json`：纸 + `glowstone` + `pufferfish` + `purple_dye`，output id `hypnosis_note`，`power` 0.2。

- [x] **Step 2: ModItems 改为非奖励注册**

```java
public static final RegistryObject<Item> EXTRA_MEAL_PERMIT = ITEMS.register(
        "extra_meal_permit",
        () -> new ExtraMealPermitItem(new Item.Properties().stacksTo(1)));
public static final RegistryObject<Item> HYPNOSIS_NOTE = ITEMS.register(
        "hypnosis_note",
        () -> new HypnosisNoteItem(new Item.Properties().stacksTo(1)));
```

从 CARE 区块/`rewardCustom` 调用挪出；**不要** `REWARD_ITEMS.add`。

`ModCreativeTabs`：在 `ORDER_BOOK` / `FOOD_PERMIT` 旁显式 `accept` 这两件（因不再进 REWARD 循环）。

- [x] **Step 3: 编译确认**

Run: `.\gradlew.bat compileJava`  
Expected: SUCCESS

- [x] **Step 4: 收尾** — 不 commit；勾选；停

**手工验收（本 Task 后可测）：** 进游戏祭坛合成两物品。

---

### Task 3: 陪伴幸福 policy（TDD）+ tick 挂载

**Files:**
- Create: `src/main/java/com/touhoumaid/eatproperly/effect/FavorHappinessPolicy.java`
- Create: `src/unitTest/java/com/touhoumaid/eatproperly/effect/FavorHappinessPolicyTest.java`
- Create: `src/main/java/com/touhoumaid/eatproperly/effect/FavorHappinessHandler.java`
- Modify: `src/main/java/com/touhoumaid/eatproperly/effect/HappinessTiming.java`（增加常量）

**Interfaces:**
- Produces:
  - `HappinessTiming.PROXIMITY_DURATION_TICKS = 3600`
  - `HappinessTiming.PROXIMITY_REFRESH_THRESHOLD_TICKS = 20`
  - `FavorHappinessPolicy.shouldRefresh(boolean eligible, int currentDurationOrZero) → boolean`  
    - `eligible` 已由调用方算好（好感≥80 且主人在范围内）  
    - `eligible && (duration <= 0 || duration <= threshold)` → true；否则 false  
    - **从不**表示 clear
  - Handler：服务端女仆 tick；`eligible` = `getFavorabilityManager().getLevel() >= 80` 且 owner 非空同维度且 `OwnerProximityPolicy.isWithinRange(maid.distanceToSqr(owner), MaidSpeech.OWNER_SPEECH_RANGE)`；若 `shouldRefresh` 则 `addEffect(HAPPINESS, 3600, …)`（参数对齐夜雀：`ambient=true, visible=false, showIcon=true` 或与现有食用效果一致，优先与 `TrinketEffects.refreshTrinketEffect` 一致）

- [x] **Step 1: 失败单测**

```java
@Test
void refreshWhenEligibleAndNoEffect() {
    assertTrue(FavorHappinessPolicy.shouldRefresh(true, 0));
}

@Test
void refreshWhenEligibleAndNearExpiry() {
    assertTrue(FavorHappinessPolicy.shouldRefresh(true, 20));
    assertTrue(FavorHappinessPolicy.shouldRefresh(true, 1));
}

@Test
void noRefreshWhenDurationAboveThreshold() {
    assertFalse(FavorHappinessPolicy.shouldRefresh(true, 21));
    assertFalse(FavorHappinessPolicy.shouldRefresh(true, 3600));
}

@Test
void neverRefreshWhenNotEligible() {
    assertFalse(FavorHappinessPolicy.shouldRefresh(false, 0));
    assertFalse(FavorHappinessPolicy.shouldRefresh(false, 5));
}
```

- [x] **Step 2: 跑测 FAIL**

Run: `.\gradlew.bat unitTest --tests com.touhoumaid.eatproperly.effect.FavorHappinessPolicyTest`

- [x] **Step 3: 实现 Policy + Timing 常量 + Handler**

`FavorHappinessPolicy`：

```java
public static boolean shouldRefresh(boolean eligible, int currentDurationTicks) {
    if (!eligible) {
        return false;
    }
    return currentDurationTicks <= HappinessTiming.PROXIMITY_REFRESH_THRESHOLD_TICKS;
}
```

`FavorHappinessHandler`：`@Mod.EventBusSubscriber`，镜像 `HappinessMaidSatietyHandler` 的 client/maid 守卫；不 clear。

- [x] **Step 4: 跑测 PASS**（PolicyTest + 全 unitTest 冒烟可选）

- [x] **Step 5: 收尾** — 不 commit；勾选；停

---

### Task 4: 版本改动 + 规格勾选

**Files:**
- Modify: `版本改动.md`（`## 1.3.0` 下追加）
- Modify: spec 验收项可改为 `[x]`（可选）
- Modify: 本 plan 全部 Task 勾选完成

- [x] **Step 1: 追加 `版本改动.md`（中英）**

示例条目（可润色，语义对齐）：

- 加餐许可、催眠便签改为祭坛合成，不再从养成里程碑池掉落；配方分别为纸+皮革+墨囊、纸+萤石+河豚+紫色染料  
- 里程碑：贴名送餐盒入中档信物池、双格送餐盒入增益池；好感 10/70 抽中档信物、40 抽增益  
- 好感≥80 的女仆在主人 16 格内会刷新幸福效果（约 3 分钟）；离开范围不立刻清除  

- [x] **Step 2: 全量 unitTest**

Run: `.\gradlew.bat unitTest`  
Expected: PASS

- [x] **Step 3: 收尾** — 汇报完成；不 commit；等用户验收/提交指令

---

## Spec coverage (self-review)

| Spec 要求 | Task |
|-----------|------|
| 祭坛配方两件 | T2 |
| 出 CARE 池 / 不再里程碑掉落 | T1 + T2 |
| 贴名→TOKEN_MID、双格→BUFF、10/40/70 档 | T1 |
| 好感≥80 + 16 格刷新幸福 3600、离开不清 | T3 |
| 单测池表 + policy | T1 + T3 |
| 版本改动 | T4 |

无 TBD；离开不 clear 仅在 Handler 注释与「不调用 removeEffect」中体现。

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-06-altar-care-items-favor-happiness.md`.

**执行方式（工作区规则：每个 Task 开工前确认）：**

1. **本会话直接做**（推荐：改动面清晰、需摸现有未提交代码）  
2. **派子代理**（每 Task 新上下文 + 两轮 review）

**难度自评 Task 1：** 低～中（池表 + 单测）。  
**建议：** 本会话直接做 Task 1。

你选：本会话 / 子代理？
