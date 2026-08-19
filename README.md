# 车万女仆：好好吃饭

**Touhou Maid: Eat properly** · `touhou_maid_eat_properly`  
Minecraft **1.20.1** Forge · 依赖车万女仆 **1.5.3+**

扩展女仆好感上限，并加入「索要食物 / 饱食度 / 长期饥饿掉好感」玩法。

## 依赖

| 模组 | 类型 |
|---|---|
| [车万女仆 (Touhou Little Maid)](https://modrinth.com/mod/touhou-little-maid) | 硬依赖 |
| 农夫乐事 (Farmer's Delight) | 可选（可食用食物自动进入索要池） |
| 妖怪的归家 (Youkai's Homecoming) | 软依赖（「湖之冰精的信物」交易加成；可食用食物亦自动入池） |

## 主要玩法

1. **好感上限**：默认可到 100 级（可配置）；3 级后每级 +2 最大生命，降级会撤回。
2. **索要食物**：隔若干游戏日随机想吃一样东西；放进女仆**背包**或用**送餐盒**交付即完成并 **+192 好点数**（双层达标 **+384**）。野餐篮不算交付。
3. **送餐盒**：祭坛合成（木板 + 竹子 + 线，0.2 P）；装食物后右键女仆送餐。
4. **饱食度**：上限 20；每日约降 30%；吃东西按食物营养回升。
5. **掉好感**：饱食长期过低超过宽限期后，每日扣约半级经验，最低 0 级。
6. **聊天**：索要/感谢/饥饿提示仅主人可见（气泡 + 聊天栏）。

打开女仆 GUI 的「好好吃饭」页可查看当前需求与饱食条。

## 配置

文件：`config/touhou_maid_eat_properly-common.toml`  
改完后重新进档 / 重启服务端即可。

## 构建

需要 JDK 17+（可用 JDK 21 跑 Gradle，工具链会用 17 编译）。

```bat
gradlew.bat build
```

产物：`build/libs/touhou_maid_eat_properly-1.2.0.jar`

## 调试指令

主命令：`/eatproperly`（别名 `/ep`），需权限等级 2。  
不指定女仆时，默认取附近 16 格内最近的女仆。详见 `docs/superpowers/plans/2026-08-03-06-user-guide.md`。

## License

All Rights Reserved（见 `gradle.properties` 中 `mod_license`）。
