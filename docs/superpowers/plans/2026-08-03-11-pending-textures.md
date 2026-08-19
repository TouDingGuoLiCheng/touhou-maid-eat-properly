# 待补贴图清单（里程碑回礼 · PixelLab 延后）

> **归档**：2026-08-03（随 1.3.0 打包）。序号 **11**。  \n> 原路径：`docs/待补贴图清单.md` → 本文件。

> 状态：奖励物品贴图已齐；幸福 BUFF 图标已补。  
> 规格参考：物品多为 **32×32** PNG；幸福效果图标 Forge 1.20.1 为 **18×18** PNG。

## A. 效果图标（优先）

| 用途 | 目标路径 | 说明 |
|------|----------|------|
| **幸福 BUFF** ✅ | `assets/touhou_maid_eat_properly/textures/mob_effect/happiness.png` | 18×18；淡黄色开心笑脸；2026-07-27 重生成 |

## B. 奖励物品（各 16×16，替换同名 layer0）

模型已指向各自 `item/<id>`，但文件尚未制作——目前回退到共用 placeholder。补图时放到：  
`assets/touhou_maid_eat_properly/textures/item/<id>.png`  
并确认 `models/item/<id>.json` 的 `layer0` 为 `touhou_maid_eat_properly:item/<id>`（多数仍写着 `reward_placeholder`，补图时一并改模型）。

### 信物 · 低档 ✅（2026-07-26 PixelLab 已生成 32×32，各 1 次）
| id | 中文名 |
|----|--------|
| forget_me_not_bouquet | 一小束勿忘我 |
| praise_certificate | 表扬奖状 |
| thanks_note_empty_box | 装着谢谢便签的空点心盒 |
| paper_crane | 纸鹤一隻 |
| meal_bell | 餐前铃铛 |
| thanks_candy | 答谢糖一颗 |
| half_strawberry_cake | 吃了一半的草莓蛋糕 |
| field_photo | 原野风景照 |
| banner_honest_boss | 锦旗 |
| crumpled_candy_wrapper | 褶皱的糖纸 |
| cake_note | 内含于蛋糕的字条 |

### 信物 · 中档 ✅（2026-07-26 PixelLab 已生成 32×32，各 1 次）
| id | 中文名 |
|----|--------|
| heart_gem | 心意宝石 |
| handwritten_letter | 手写信 |
| moonlight_shard | 月华碎片 |
| two_tone_hair_tie | 双色发绳 |
| embroidered_handkerchief | 绣花手帕 |
| paired_cup | 对杯之一 |
| unopened_souvenir | 未拆伴手礼 |
| pressed_diary_page | 压花日记页 |
| block_figurine | 方块积木 |
| silver_bell_charm | 银铃挂饰 |

### 信物 · 高档 ✅（2026-07-26 PixelLab 已生成 32×32，各 1 次）
| id | 中文名 |
|----|--------|
| warm_amulet | 温热护身符 |
| eternal_meal_ticket | 永恒餐券 |
| heart_knot | 同心结 |
| sugar_jar | 精致的砂糖罐 |
| unnamed_ring | 无名约指 |
| plain_pouch | 素色荷包 |

### 养成 ✅（2026-07-27 PixelLab 已生成 32×32，各 1 次）
| id | 中文名 |
|----|--------|
| bound_lunchbox | 贴名送餐盒 |
| dual_lunchbox | 双层送餐盒 |
| extra_meal_permit | 加餐许可 |
| hypnosis_note | 催眠便签 |

### 增益 ✅（2026-07-27 PixelLab 已生成 32×32，各 1 次）
| id | 中文名 |
|----|--------|
| heart_seasoning | 爱心调味料 |
| insulated_meal_bag | 保温餐袋 |
| moon_apple_candy | 月见苹果糖 |
| scarlet_amulet | 绯想护符 |
| night_sparrow_wrapper | 夜雀糖纸 |
| sweet_soft_candy | 甜蜜软糖 |
| onsen_manju_bag | 温泉馒头袋 |
| sobering_raisin | 醒酒葡萃 |
| lake_fairy_keepsake | 湖之冰精的信物 |

## C. 已有贴图（不必重做，除非你要重画）

| 路径 | 用途 |
|------|------|
| `textures/item/lunchbox.png` | 普通送餐盒 |
| `textures/item/reward_placeholder.png` | 临时共用占位 |
| `textures/gui/eat_properly_panel.png` | 好好吃饭面板 |
| `textures/gui/green_tea.png` | GUI 页签图标 |
| `textures/gui/lunchbox.png` | 送餐盒 GUI |
| `textures/gui/satiety_icons.png` | 饱食度图标 |

## D. 后续可能新增（Task 9+，现在可先不做）

| 用途 | 预估路径 | 说明 |
|------|----------|------|
| 饰品袋 / 饰品 GUI | `textures/gui/trinkets.png` 等 | Task 9 饰品栏 UI，实现时再定 |

## 计数

- 效果图标：**1** ✅
- 奖励物品独立贴图：**40**（低 11 ✅ + 中 10 ✅ + 高 6 ✅ + 养成 4 ✅ + 增益 9 ✅）
- **合计待补：0**（低/中/高/养成/增益/幸福均 ✅；不含已有 lunchbox / GUI）

补图后记得改对应 `models/item/<id>.json` 的 `layer0`，否则仍显示 placeholder。（低/中/高档/养成/增益模型已改指向各自贴图）
