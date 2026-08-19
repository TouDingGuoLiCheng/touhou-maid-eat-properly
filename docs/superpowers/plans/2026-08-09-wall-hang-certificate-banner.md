# 奖状 / 锦旗挂墙（仿原版画）

> 已批准设计要点：1×1 / 1×2；墙上渲染 Blockbench 3D；拆下保留绑定 NBT。

## 你问-我回答

- 挂墙尺寸？→ A：奖状 1×1，锦旗 1×2
- 墙上外观？→ A：Blockbench 三维模型
- 拆下掉落？→ A：掉回物品并保留女仆绑定

## 文件

- `entity/WallHangEntity.java` — 挂墙实体（尺寸、朝向、存 ItemStack）
- `entity/ModEntities.java` — 实体注册
- `item/reward/WallHangRewardItem.java` — 右键墙面放置
- `client/WallHangRenderer.java` — 渲染 `*_3d` 模型
- 改 `ModItems` / `EatProperlyMod` / `ClientSetup` / `版本改动.md`

## 验收

- [x] 竖直墙面可挂；上下面不可挂（代码：`face.getAxis().isVertical()` 拒绝）
- [x] 空间不足挂不上（`survives()`）
- [x] 墙上为立体模型；奖状约 1×1、锦旗约 1×2（编译通过；需进游戏目视微调朝向）
- [x] 拆下物品 lore/绑定仍在（实体保存完整 ItemStack）
- [x] 背包仍平面图标（既有 separate_transforms）
