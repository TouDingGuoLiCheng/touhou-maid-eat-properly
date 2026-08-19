# 模组介绍（上传用文案）

> **归档**：2026-08-03（随 1.3.0 打包）。序号 **05**。  \n> 原仓库根路径：`模组介绍-上传用.md` → 本文件。

可直接粘贴到 CurseForge / Modrinth / MC 百科等平台。

**CurseForge 注意：** Summary 与 Description 都必须有英文；Description 里**英文必须写在最上面**，中文等其它语言放在英文之后。

---

## Summary（摘要 — 先贴英文）

```
Raise Touhou Little Maid favorability cap, and add food requests, satiety, lunchbox delivery, and milestone keepsake rewards.
```

中文备选（仅其它平台需要时可另用）：
```
扩展车万女仆好感上限，并加入索要食物、饱食度、送餐盒与里程碑回礼，让照顾女仆变得更有养成感。
```

---

## Description（完整介绍 — 英文在上，中文在下）

直接整段复制到 CurseForge Description 即可：

```
### Touhou Maid: Eat properly

Touhou Little Maid's favorability normally caps at level 3. No matter how long you stay together or how often you feed her, the relationship seems stuck at an invisible limit.

This addon fills in the "eat properly" loop: raise the favorability cap, let maids request food from you, keep a satiety meter, and unlock milestone keepsakes along the way. What you feed her truly affects your relationship.

### Features

**Favorability**
- Default max level **100** (configurable)
- After level **3**, each further level grants **+2 max HP** (removed on level-down)

**Food requests**
- Maids periodically request a specific food and remind the owner via bubble / chat (owner-only)
- Place the food in the maid's **inventory**, or deliver it with a **Lunchbox**, to complete the request and gain **+1 favorability level**
- Unmet requests are eventually abandoned; picnic-basket feeding does **not** count as delivery
- Public chat (chat bar) only sends immediately during in-game **08:00–20:00** (aligned with the maid GUI clock); outside that window, bubbles still show and chat is deferred to a random daytime slot

**Lunchbox**
- Altar craft: planks + bamboo + string, costs **0.2 P**
- One food slot; right-click a maid to deliver
- Milestone rewards may also grant a named lunchbox or a dual-slot lunchbox (right-click to open a 2-slot GUI)

**Satiety**
- Cap 20; drains gradually each day; restored by eating based on food nutrition
- Staying low for too long (after a grace period) slowly reduces favorability
- Check the current request and satiety bar on the maid GUI tab **Eat properly**

**Milestone rewards**
- Reaching favorability level 5 / 10 / 20 / 30 / 40 / 50 / 60 / 70 / 80 / 90 / 100 unlocks a claimable reward on the **Eat properly** tab
- Three reward types: keepsakes signed by the maid (collectible), care items (e.g. named lunchbox, extra meal permit, hypnosis note, meal bell), and buff trinkets
- Open inventory with **E**, click the mod button near the player model to expand a **3-slot trinket panel** on the left; who wears it benefits. Night Sparrow Wrapper is player-only (night vision); Silver Bell Charm grants Speed I
- Some keepsakes are edible for a "Happiness" buff; others include a readable handwritten letter or a meal bell that summons the maid (home mode → follow)

### Dependencies

| Mod | Notes |
|---|---|
| **Touhou Little Maid** **1.5.3+** | Required |
| Farmer's Delight | Optional — its edible foods join the request pool automatically |
| Youkai's Homecoming | Optional — Lake Fairy Keepsake trade bonus; its edible foods also join the pool |

### Version

- Minecraft **1.20.1**
- Loader: **Forge**

### Config

`config/touhou_maid_eat_properly-common.toml`
Tune favorability cap, request timing, satiety drain, favor decay grace, and speech templates. Rejoin the world / restart the server after editing.

### Source

https://github.com/TouDingGuoLiCheng/touhou-maid-eat-properly

---

### 车万女仆：好好吃饭
**Touhou Maid: Eat properly**

原版车万女仆的好感最高只有 3 级——陪得再久、喂得再勤，关系也像停在了某个看不见的上限上。

本模组为女仆补上「好好吃饭」这一环：提高好感上限，让她会向你索要食物；同时维护饱食度，并在升级路上送出里程碑回礼。照顾她吃什么，会真正影响你们的关系。

### 主要功能

**好感扩展**
- 好感默认最高 **100 级**（可在配置中修改）
- **3 级之后**，每升 1 级最大生命 **+2**；降级时加成会收回

**索要食物**
- 女仆会定期想吃某样食物，并通过气泡 / 聊天提醒主人（仅主人可见）
- 把指定食物放进女仆**背包**，或用**送餐盒**交付，即可完成索要并 **提升 1 级好感**
- 长期不满足会放弃本次请求；野餐篮喂食**不算**完成索要
- 公屏聊天栏仅在游戏内 **08:00–20:00**（与女仆 GUI 日程一致）立即发送；窗外仍立刻出气泡，公屏话会延迟到白天随机时刻

**送餐盒**
- 祭坛合成：木板 + 竹子 + 线，消耗 **0.2 P**
- 单格食物槽；装好后右键女仆即可送餐
- 里程碑还可获得贴名送餐盒、双层送餐盒（右键打开 2 格 GUI）

**饱食度**
- 上限 20，每天会缓慢下降；吃东西按食物营养回升
- 长期处于低饱食，过了宽限期后好感会慢慢下降
- 在女仆 GUI 的「好好吃饭」页可查看当前需求与饱食条

**里程碑回礼**
- 好感每达到 5 / 10 / 20 / 30 / 40 / 50 / 60 / 70 / 80 / 90 / 100 级，都能在「好好吃饭」页领取一份专属回礼
- 回礼分三类：来源女仆签名的**信物**（收藏向）、**养成道具**（如送餐盒、加餐许可、催眠便签、餐前铃铛）、**增益饰品**
- 按 **E** 开背包，点玩家模型旁的本模组按钮，可在左侧展开 **3 格饰品栏**；谁戴谁受益。夜雀糖纸仅玩家（夜视），银铃挂饰提供迅捷 I
- 部分信物可吃获得「幸福」增益；手写信可翻阅；餐前铃铛可召唤女仆（居家模式会切跟随）

### 依赖

| 模组 | 说明 |
|---|---|
| **车万女仆** Touhou Little Maid **1.5.3+** | 硬依赖，必须安装 |
| 农夫乐事 Farmer's Delight | 可选，其可食用食物自动进入索要池 |
| 妖怪的归家 Youkai's Homecoming | 可选，湖之冰精信物交易加成；可食用食物亦自动入池 |

### 版本

- Minecraft **1.20.1**
- 加载器：**Forge**

### 配置

配置文件：`config/touhou_maid_eat_properly-common.toml`
可调整好感上限、索要间隔、饱食下降、掉好感宽限期，以及各类话术文本。修改后重新进档 / 重启服务端即可生效。

### 源码

https://github.com/TouDingGuoLiCheng/touhou-maid-eat-properly
```

---

## MC百科介绍（用 `[hx=标题]`，名词用「饥饿值」）

审核要求：标题必须用编辑器「T」的标题语法；「饱食度」改为「饥饿值」；封面 16:10 且勿裁剪。

```
[h1=简介]
原版「车万女仆」好感最高只有 3 级——陪得再久、喂得再勤，关系也像停在了某个看不见的上限上。

本模组为女仆补上「好好吃饭」这一环：扩展好感上限，让她会向你索要食物；维护饥饿值，并在升级路上送出里程碑回礼。你忙不过来时，她甚至可能自己去附近箱子「紧急自助」。照顾她吃什么，会真正影响你们的关系。

[h1=主要功能]

[h2=一、好感扩展]
好感默认最高可达 100 级（可在配置中修改）。达到 3 级之后，每再升 1 级，女仆最大生命值增加 2 点；降级时对应加成会收回。

[h2=二、索要食物]
女仆会定期向主人索要某样食物，并通过聊天气泡与聊天栏提示（仅主人可见）。须使用「送餐盒」或「双层送餐盒」主动交付才能完成索要并提升好感；往女仆界面背包里塞食物、或用野餐篮 / 野餐垫喂食，均不算完成索要。若长期未满足，女仆会放弃本次请求。公屏聊天的可发送时段跟随每只女仆的排班（白班 / 夜班 / 全天）；气泡仍会立刻出现，窗外公屏话会延迟发送，避免刷屏。

[h2=三、送餐盒]
可在车万女仆祭坛合成：任意木板、竹子、线各 1，消耗 0.2 P 点。基础送餐盒含 1 个食物槽，装入食物后右键女仆即可送餐并完成一次索要（提升 1 级好感）。里程碑回礼还可获得贴名送餐盒与双层送餐盒；双层送餐盒主手右键可打开两格界面，两格都装满且至少一格对上当前索要时，一次可连升 2 级。

[h2=四、饥饿值]
女仆拥有饥饿值（上限 20），会随游戏日缓慢下降，进食可按食物营养回升。工作餐 / 家餐仅在饥饿较低时才会主动吃；若长期处于低饥饿状态，超过宽限期后好感会逐渐下降。可在女仆界面的「好好吃饭」分页查看当前需求与饥饿条。

[h2=五、自己找吃的]
女仆饿了且背包没有可吃食物时，可能悄悄走到附近箱子或零食柜，取走一份食物补饥饿后再跑开。此行为只补饥饿，不算完成索要；成功时只有头顶气泡，不刷公屏。可在「好好吃饭」页、饥饿标签旁的饼干按钮按女仆单独开关（「自己找」或「等主人」），也可在配置中关闭整段功能。

[h2=六、里程碑回礼]
好感达到 5 / 10 / 20 / 30 / 40 / 50 / 60 / 70 / 80 / 90 / 100 级时，可在「好好吃饭」页右上角领取专属回礼，包括信物、养成道具与增益饰品。养成道具含加餐许可（立刻刷新一次索要）、催眠便签（让她忘掉当前点单）、餐前铃铛（召唤女仆，居家模式会切跟随）等。按 E 打开背包，点击玩家模型旁本模组按钮可展开三格饰品栏，谁佩戴谁受益。部分信物可食用获得「幸福」增益并留下纪念版；手写信可右键翻阅。

[h1=依赖]
必需：车万女仆（Touhou Little Maid）1.5.3 及以上。
可选：农夫乐事、妖怪的归家（装了会自动把其可食用食物纳入索要池；后者另有「湖之冰精的信物」交易相关加成）。索要池本身已覆盖所有模组的原版可食用、无负面效果食物。

[h1=运行环境]
Minecraft 1.20.1，Forge。

[h1=配置]
配置文件位于 config/touhou_maid_eat_properly-common.toml，可调整好感上限、索要间隔、饥饿值下降、掉好感宽限期、「自己找吃的」开关与半径，以及各类话术文本等。修改后需重新进入存档或重启服务端。
```
