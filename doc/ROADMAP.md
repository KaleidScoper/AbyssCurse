# AbyssCurse 插件开发路线图

本路线图基于 PaperMC 1.21.10，每个阶段结束后均应确保插件可在测试服务器上正常运行并演示核心功能。

---

## 阶段 I：框架与基础设施（第 1～2 周）
**目标**：插件能加载、切换模式、输出日志。

### 核心任务
- [ ] 初始化 Gradle 工程与 `plugin.yml`
- [ ] 创建主类 `AbyssCurse.java`
- [ ] 注册基础命令 `/abysscurse`
- [ ] 建立配置系统与 `ConfigManager`
- [ ] 日志与调试系统 (`DebugManager`)
- [ ] 模式切换命令实现 (`off | abyss | world`)
- [ ] 确认配置 reload 持久化

### 验证检查点
- 插件能正常加载并响应命令  
- 切换模式输出正确日志  
- 无 NPE / 无线程警告

---

## 阶段 II：数据结构与区域系统（第 3～4 周）
**目标**：Abyss 区域与豁免机制可用，能识别玩家位置。

### 核心任务
- [ ] 实现 `RegionManager`：中心区块 + 半径定义
- [ ] 添加豁免区与豁免玩家列表
- [ ] 提供 API：`isInAbyss(Player)` / `isExempt(Player, Location)`
- [ ] 创建 `PlayerDataManager`：记录安全高度、累计上升
- [ ] 事件监听：`PlayerMoveEvent` 检测 Y 变化
- [ ] 实现定时保存与 onJoin/onQuit 加载

### 验证检查点
- 玩家移动时能实时更新 Y  
- 玩家进入/离开 Abyss 输出提示  
- 豁免区正确判定

---

## 阶段 III：核心诅咒与效果系统（第 5～8 周）
**目标**：实现完整诅咒逻辑与视觉表现，核心玩法可体验。

### 核心任务
- [ ] `CurseManager`：累计上升+安全高度判定
- [ ] 枚举定义七层诅咒效果
- [ ] `EffectManager`：持续/时限效果控制
- [ ] `SoundManager`：不同层音效播放
- [ ] `VisualManager`：Title + BossBar 层提示
- [ ] 调试模式显示当前层与上升值
- [ ] 优化 tick 检测逻辑（每 20 tick 一次）

### 验证检查点
- 上升超过 2m 触发诅咒  
- 不同高度触发不同层效果  
- 效果不重复叠加或错乱

---

## 阶段 IV：扩展与打磨（第 9～10 周）
**目标**：生骸系统上线，性能优化，准备发布版本。

### 核心任务
- [ ] `NarehateManager`（生骸系统）
  - 第六层 10 分钟未死触发
  - 给予祝福效果与成就提示
- [ ] `PerformanceManager`：异步写入 + tick 优化
- [ ] 扩展命令：`/abysscurse info` / `/abysscurse simulate <layer>`
- [ ] 开放 `AbyssAPI` 供他人调用
- [ ] 调试与日志输出改进
- [ ] 全面测试与性能 Profile

### 验证检查点
- 生骸化逻辑正确触发  
- 所有效果在重启后保持一致  
- 多人在线时服务器稳定  
- 数据文件大小合理

---

## 团队管理建议
| 管理项 | 建议 |
|--------|------|
| 代码规范 | 统一包结构与命名；每个 Manager 单例化 |
| 版本控制 | Git 分支策略：`main` / `dev` / `feature/*` |
| 任务管理 | 使用 GitHub Projects，将本文件导入为任务卡 |
| 每日沟通 | 简短晨会：昨日进度 + 今日目标 |
| 测试流程 | 每阶段结束后生成测试版 JAR |
| 文档维护 | 更新 README 与 CHANGELOG |
| Bug 测试 | 每层级的触发条件单独测试 |

---

## 最终目标
| 阶段 | 目标 | 核心模块 |
|------|------|----------|
| I | 能启动并切换模式 | Main, Command, Config |
| II | 区域判定与数据管理 | Region, PlayerData |
| III | 诅咒触发与视觉效果 | Curse, Effect, Visual |
| IV | 生骸系统与性能优化 | Narehate, API |

---
