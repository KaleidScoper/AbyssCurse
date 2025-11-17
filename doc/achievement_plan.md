## 深渊成就达成方案

依据《需求文档》中的层级与行为设定，推荐使用原版 advancement 触发器完成判定，避免依赖 `minecraft:impossible`。以下方案默认插件已经能判定玩家所在层级与诅咒处理逻辑。

| 成就 | 需求文档定位 | 推荐原版触发器 | 条件 & 说明 | 授予时机 |
|------|--------------|----------------|-------------|----------|
| **阿比斯之渊** (`abyss_edge`) | 根节点、故事开始 | `minecraft:tick` + `player` 条件 | 任意玩家在服务器存活 1 tick 即满足；代表“发现深渊”。 | 采用 `tick`，且 `hidden=false`，玩家初次进入世界即显示分页。 |
| **赤笛** (`red_whistle`) | 第一层 Edge of the Abyss（96>y≥85） | `minecraft:location` 或 `minecraft:enter_block` | `player` 条件附带 `position`（Y 范围 85-96）并且位于 `abyss` 模式区域；推荐在插件监听到玩家层级切换时，通过 `player_tag` 或 `score` 作为条件。 | 玩家首次踏入第一层时自动完成。 |
| **苍笛** (`blue_whistle`) | 第二层 Forest of Temptation（85>y≥75） | 同上 `minecraft:location` | 条件改为 Y 范围 75-85。 | 玩家首次踏入第二层。 |
| **月笛** (`moon_whistle`) | 第三层 Great Fault（75>y≥40） | `minecraft:location` | Y 范围 40-75。 | 玩家首次踏入第三层。 |
| **黑笛** (`black_whistle`) | 第四层 Goblets of Giants（40>y≥0） | `minecraft:location` | Y 范围 0-40。 | 玩家首次踏入第四层。 |
| **白笛** (`white_whistle`) | 第五层 Sea of Corpses（0>y≥-8）挑战 | `minecraft:location` + `frame: challenge` | Y 范围 -8~0。触发时可附带 `effects` 条件（例如玩家正在承受该层负面效果）。 | 玩家首次踏入第五层。 |
| **绝界行** (`unreturned`) | 第六层 Capital of the Unreturned（-8>y≥-28）挑战 | `minecraft:location` | Y 范围 -28~-8。该层在需求中要求“位于此层就永久生效不许聊天”，因此自然抵达即可授予。 |
| **奈落之底** (`naraku`) | 第七层 Final Maelstrom（-28>y≥-64）挑战 | `minecraft:location` | Y 范围 -64~-28。 | 玩家首次进入最底层。 |
| **来自深渊** (`from_abyss`) | 成为生骸（豁免者） | `minecraft:player_hurt_entity` 无法表达 -> 推荐组合 `minecraft:effects_changed` 或 `minecraft:impossible` | 因“成为生骸”是插件内部事件，原版 trigger 无法直接描述，建议保留 `minecraft:impossible` 并在 `NarehateManager` 中监听“化身豁免者”事件后手动授予；也可借助 `minecraft:effects_changed`，将生骸特性标记为自定义状态效果标签后触发。 |

### 触发 JSON 实现要点
1. **区域判定**：`minecraft:location` 支持 `position`（坐标区间）与 `dimension`。通过 `predicate` 附加 `location` 范围，仅在玩家位于 `abyss` 区域时触发，可借助 scoreboard/tag 判断（在 `player` 条件里引用 `entity_properties`）。  
2. **树独立展示**：根节点使用 `tick` 触发保证分页可见；后续节点设置 `parent`、`requirements` 即可。  
3. **挑战音效**：对需求中标记为挑战的成就，将 `display.frame` 设置为 `"challenge"`，原版会自动播放 `ui.toast.challenge_complete`。  
4. **防重复授予**：进度系统本身具备一次性特性，不必在代码里调用 `grant`。若仍需强制同步（例如插件内部某层逻辑失败），可保留 `AchievementManager.grantAchievement` 作为补偿。  

### 额外建议
- **开关模式**：当模式为 `off` 或玩家处于豁免区/豁免名单时，可在 `criterion` 的 `player` 谓词中加 `nbt` 条件，借助 scoreboard/tag（如 `abyss_active=1`）控制。  
- **调试**：可以为每个层级准备 `debug` advancement（隐藏）验证触发是否正常，再决定是否外露给玩家。  
- **数据驱动兼容**：若未来考虑发布为 datapack，也可以把 `entity_properties` 中的 scoreboard 条件写死在 JSON；插件只需在对应时机给玩家加减 scoreboard 值即可。  

以上内容可直接作为推进 JSON/插件逻辑的参考模板，并与 `doc/advancement_triggers.md` 的触发器列表互补。

