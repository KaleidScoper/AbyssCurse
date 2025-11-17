## Minecraft Advancement 触发器速查（1.21）

> 资料来源：Mojang 官方数据驱动文档、Paper 1.21.10 `AdvancementTrigger` 实现与 Minecraft Wiki。该表仅罗列 Vanilla 自带的触发 ID 以及它们监听的行为，为设计 datapack/插件时选择合适的进度条件。

### 基础/杂项
| 触发器 ID | 监听行为 |
|-----------|----------|
| `minecraft:tick` | 玩家在主世界任意 tick 存活即可触发（最常用的“自动完成”条件）。 |
| `minecraft:impossible` | 永远不会自然触发，只能由命令或插件授予；适合作为手动控制的节点。 |
| `minecraft:recipe_unlocked` | 玩家解锁指定配方时触发。 |
| `minecraft:inventory_changed` | 玩家背包满足指定物品集合/数量。 |
| `minecraft:consume_item` | 食用/使用指定物品（药水、牛奶等）。 |
| `minecraft:using_item` | 玩家持续使用某物品达到指定时长。 |
| `minecraft:item_durability_changed` | 物品耐久发生变动，常用于“修补”或“损坏”条件。 |
| `minecraft:player_hurt_entity` | 玩家对实体造成伤害。 |
| `minecraft:entity_hurt_player` | 玩家受实体伤害。 |
| `minecraft:player_killed_entity` | 玩家击杀指定实体。 |
| `minecraft:entity_killed_player` | 指定实体击杀玩家。 |
| `minecraft:player_interacted_with_entity` | 右键交互（喂食、使用物品等）。 |
| `minecraft:thrown_item_picked_up_by_entity` | 玩家投掷物被实体捡起。 |
| `minecraft:item_used_on_block` | 对方块使用物品（如镐开关、刷颜色）。 |
| `minecraft:placed_block` | 玩家放置某方块。 |
| `minecraft:started_riding` | 开始骑乘任何实体。 |
| `minecraft:tame_animal` | 驯服动物成功。 |
| `minecraft:summoned_entity` | 召唤出实体（铁傀儡、唤魔者等）。 |
| `minecraft:target_hit` | 射中靶子方块，支持距离条件。 |
| `minecraft:shot_crossbow` | 发射弩（可带有多实体条件）。 |
| `minecraft:killed_by_crossbow` | 使用弩击杀目标。 |
| `minecraft:using_item` | 使用某物品达到持续时间，可用于“蓄力 X 秒”场景。 |
| `minecraft:effects_changed` | 玩家获得/失去指定状态效果组合。 |

### 移动/位置
| 触发器 ID | 监听行为 |
|-----------|----------|
| `minecraft:location` | 玩家进入指定坐标区域或结构。 |
| `minecraft:nether_travel` | 从主世界进入下界或反向传送，带特定距离要求。 |
| `minecraft:hero_of_the_village` | 玩家成为村庄英雄。 |
| `minecraft:voluntary_exile` | 玩家获得掠夺者不祥之兆。 |
| `minecraft:enter_block` | 站入（或头部处于）目标方块内。 |
| `minecraft:slept_in_bed` | 成功在床上睡觉。 |
| `minecraft:slide_down_block` | 在蜂蜜块等“可滑动”方块上滑落。 |
| `minecraft:ride_entity_in_lava` | 在熔岩中骑乘实体（如炽足兽）。 |
| `minecraft:fall_from_height` | 跌落高度达到指定值。 |
| `minecraft:levitation` | Levitation 状态持续一段时间。 |
| `minecraft:enter_block` | 玩家身体部位穿过某方块（用于检测踩到方块）。 |

### 物品/方块互动
| 触发器 ID | 监听行为 |
|-----------|----------|
| `minecraft:filled_bucket` | 用桶装取指定流体/实体（鱼桶、熔岩等）。 |
| `minecraft:brewed_potion` | 成功酿造出指定药水。 |
| `minecraft:enchanted_item` | 对物品施加附魔。 |
| `minecraft:construct_beacon` | 建造信标，支持层数过滤。 |
| `minecraft:bee_nest_destroyed` | 破坏蜂巢/蜂箱并带有蜂蜜等级判定。 |
| `minecraft:brewed_potion` | 在酿造台产出目标药水。 |
| `minecraft:player_generates_container_loot` | 打开生成战利品容器（沙漠神殿等）。 |
| `minecraft:used_ender_eye` | 朝要塞使用末影之眼。 |
| `minecraft:used_totem` | 触发不死图腾。 |
| `minecraft:consume_item` | 食用/喝下指定物品。 |
| `minecraft:home_bed` | 设置重生点（1.20+）。 |

### 生物/战斗事件
| 触发器 ID | 监听行为 |
|-----------|----------|
| `minecraft:bred_animals` | 繁殖指定动物组合。 |
| `minecraft:axolotl_killed` | 美西螈协助击杀生物。 |
| `minecraft:cured_zombie_villager` | 治愈僵尸村民。 |
| `minecraft:channeled_lightning` | 三叉戟引雷命中实体。 |
| `minecraft:allay_drop_item_on_block` | 悦灵在目标方块上投掷物品。 |
| `minecraft:voluntary_exile` | 击杀袭击队长获得不祥之兆。 |
| `minecraft:hero_of_the_village` | 成功防御袭击。 |
| `minecraft:fishing_hook_hooked` | 鱼竿钩到某种实体/物品。 |

### 结构/世界交互
| 触发器 ID | 监听行为 |
|-----------|----------|
| `minecraft:used_ender_eye` | 使用末影之眼定位要塞并落地。 |
| `minecraft:player_generates_container_loot` | 打开天然生成的容器触发战利品表。 |
| `minecraft:voluntary_exile` / `minecraft:hero_of_the_village` | 与袭击事件相关。 |

### 经典例子
- **探索线**：`minecraft:location` + `minecraft:nether_travel`（探索结构/维度）。  
- **战斗线**：`minecraft:player_killed_entity`、`minecraft:killed_by_crossbow`。  
- **收集线**：`minecraft:inventory_changed` 或 `minecraft:consume_item` 组合。  
- **剧情/自动授予**：`minecraft:tick`（玩家上线即完成）或 `minecraft:impossible` 配合插件授予。

### 如何选择触发器
1. **优先使用能描述行为的触发器**：例如“踏入第一层”就可以用 `enter_block` 或 `location`，不需要插件授予。  
2. **组合条件**：大多数触发器支持 `conditions`（如玩家、实体、位置、天气等）——在 JSON 中通过 `conditions` 字段设置。  
3. **只在必要时使用 `impossible`**：它给了插件最大控制权，但会失去原版 UI 自动展示的优势。  

如需详尽的 `conditions` 写法，可参考：
- <https://minecraft.wiki/w/Advancement#JSON_format>  
- Paper API 源码 `net.minecraft.advancements.critereon` 包下的各 Criteria 类。

