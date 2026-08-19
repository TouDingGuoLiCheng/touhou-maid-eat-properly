# A9 连吃好感 + nutrition×1.5

## 目标

1. 低饱食连吃时每次工作餐/家餐真吃都加好感（不被 TLM `apply` 冷却吞掉）。
2. 好感改为 `floor(nutrition × 1.5)`（整数：`nutrition * 3 / 2`），不再固定 +8。

## 根因

`MaidMealSatietyMixin` 在 `onMaidEat` HEAD 先加饱食，随后 `apply` 里 `canAdd` 的连吃绕过看到的是**餐后**饱食，第二次常 ≥ 停线 → 不加分；任务启动时用的是餐前饱食故仍能开吃。

## 改动

| 文件 | 做什么 |
|------|--------|
| `MealFavorPolicy` | `pointsForNutrition(n)` → `n<=0 ? 0 : n*3/2` |
| `MealFavorPolicyTest` | 覆盖 0/5/8 等 |
| `MaidMealFavorMixin` | Redirect `apply`，按食物 nutrition 算分 |
| `FavorabilityManagerMixin` | `apply(Type,int)`：餐类型且 points>0 时强制加点并上冷却（假跳过 points=0 走原逻辑） |
| `版本改动.md` / `待修复.md` / `验收计划.md` | 同步 |

## 验收

- [x] 单测 `MealFavorPolicyTest` 通过
- [ ] 低饱食连吃两次真吃，每次 favor 约 +floor(nutrition×1.5)（进档）
- [ ] 假跳过仍 +0（进档）
