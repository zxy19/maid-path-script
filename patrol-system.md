# 女仆巡逻系统设计

## 概述

让女仆沿预计算路径点（BlockPos 列表）自动移动，并在合适时机与玩家交互、等待玩家。

路径点来源：原版 `Path` 对象的节点列表（相邻方块，曼哈顿距离 ≤ 2），拼接后作为巡逻路线。

---

## 核心类

### `MaidPatrolTask` — 巡逻行为

继承 `Behavior<EntityMaid>`，注册于 CORE Activity，优先级 4。

#### 记忆依赖

| MemoryModuleType | 状态 | 说明 |
|---|---|---|
| `PATROL_MODE` | `VALUE_PRESENT` | 布尔标记，巡逻模式开关 |
| `PATROL_WAYPOINTS` | `VALUE_PRESENT` | `List<BlockPos>`，完整路径点列表 |
| `PATROL_WAYPOINT_INDEX` | `REGISTERED` | `int`，当前正在前往的路径点索引 |
| `WALK_TARGET` | `REGISTERED` | 需要擦除，防止 `MoveToTargetSink` 干扰 |

#### 生命周期

```
checkExtraStartConditions:
  maid.canBrainMoving() == true

start:
  waypoints = brain.getMemory(PATROL_WAYPOINTS)
  index = brain.getMemory(PATROL_WAYPOINT_INDEX).orElse(0)
  if index >= waypoints.size(): index = 0
  brain.eraseMemory(WALK_TARGET)
  navigateToNext(maid, waypoints, index)

tick:
  1. 玩家距离 > 阈值(24格) → navigation.stop(), return
  2. 如果 navigation.isStuck() → 跳过当前路径点, index++, 继续
  3. 检查是否到达当前路径点 (distanceSqr < 2²):
     a. index++, 写入 PATROL_WAYPOINT_INDEX
     b. 触发交互钩子
     c. 如果 index >= waypoints.size():
        - 停止巡逻（或循环到 0）
     d. 否则 navigateToNext()

canStillUse:
  PATROL_MODE 仍存在
  maid.canBrainMoving() == true

stop:
  navigation.stop()
  // 不清除 PATROL_MODE/WAYPOINTS/INDEX，保留状态
```

#### `navigateToNext` 方法

```java
void navigateToNext(EntityMaid maid, List<BlockPos> waypoints, int index) {
    List<BlockPos> remaining = waypoints.subList(index, waypoints.size());
    List<Node> nodes = remaining.stream()
        .map(p -> new Node(p.getX(), p.getY(), p.getZ()))
        .toList();
    Path path = new Path(nodes, remaining.getLast(), true);
    maid.getNavigation().moveTo(path, speedModifier);
}
```

**关键**：一次性构造 `Path` 对象喂给 `PathNavigation`，完全绕过 A* 寻路计算。

---

## 与其他 CORE 行为的交互

### `MoveToTargetSink` (priority 2)

`MaidPatrolTask` 每 tick 擦除 `WALK_TARGET`，`MoveToTargetSink` 因 `WALK_TARGET` 为 `VALUE_ABSENT` 而无法启动，不会干扰巡逻。

### `MaidFollowOwnerTask` (priority 3)

在 `checkExtraStartConditions` 中新增判断：

```java
if (maid.getBrain().hasMemoryValue(InitBrains.PATROL_MODE.get())) {
    return false;
}
```

巡逻模式下不跟随主人。

### `MaidAwaitTask` (priority 1)

`MaidAwaitTask` 会检查 `WALK_TARGET` 的目标是否在 home 范围内，不在则清除。巡逻模式下 `WALK_TARGET` 已被擦除，`MaidAwaitTask` 无操作。

---

## 交互行为钩子

到达路径点时预留的交互钩子，由子类或回调实现：

```java
// MaidPatrolTask 中预留
protected void onWaypointReached(ServerLevel level, EntityMaid maid, BlockPos waypoint) {
    // 默认空实现
    // 子类可重写：打开 GUI、给予物品、播放动画等
}
```

交互行为包括但不限于：
- 等待玩家靠近（停留若干 tick）
- 打开交易/对话 GUI
- 给予/索要物品
- 播放特定动画或音效

---

## 玩家距离管理

| 条件 | 行为 |
|------|------|
| 玩家距离 > 24 格 | `navigation.stop()`，原地等待 |
| 玩家距离 ≤ 24 格 | 继续沿路径移动 |
| 玩家距离 < 3 格且路径点已到达 | 触发交互钩子 |

距离阈值可在构造时配置。

---

## 路径循环

| 模式 | 行为 |
|------|------|
| 单次 | 到达最后一个路径点后停止巡逻，清除 `PATROL_MODE` |
| 循环 | 到达最后一个路径点后 `index = 0`，重新从起点开始 |

通过 `PATROL_MODE` 记忆控制，`stop()` 不清除记忆，再次 `start()` 时恢复。

---

## 卡住处理

利用 `PathNavigation.isStuck()` 检测卡住：
- 卡住时跳过当前路径点，索引 +1，继续下一个
- 连续卡住超过 N 次则停止巡逻

---

## 数据流

```
Item 右击女仆
  → C→S 网络包 (SyncPatrolRoutePacket)
  → 写入 Brain 记忆:
      PATROL_MODE = true
      PATROL_WAYPOINTS = List<BlockPos>
      PATROL_WAYPOINT_INDEX = 0
  → MaidPatrolTask.start() 触发

每 tick:
  MaidPatrolTask.tick()
    → 检查玩家距离
    → 检查 navigation 状态
    → 到达路径点 → 索引 +1 → navigateToNext()
    → 玩家过远 → navigation.stop()

Shift+右击女仆
  → C→S 网络包
  → 清除 PATROL_MODE/WAYPOINTS/INDEX
  → MaidPatrolTask.stop() 触发
  → 恢复正常行为
```

---

## 文件清单

| 文件 | 类型 | 说明 |
|------|------|------|
| `InitBrains.java` | 修改 | 新增 `PATROL_MODE`、`PATROL_WAYPOINTS`、`PATROL_WAYPOINT_INDEX` 三个带 codec 的 MemoryModuleType |
| `MaidBrain.java` | 修改 | `getMemoryTypes()` 加入三个新类型；`initCoreActivity()` 注册 `MaidPatrolTask` |
| `MaidPatrolTask.java` | 新建 | 核心巡逻逻辑 |
| `MaidFollowOwnerTask.java` | 修改 | `checkExtraStartConditions` 中巡逻模式返回 false |
| `ItemPatrolRoute.java` | 新建 | 记录/写入路径点的物品 |
| `SyncPatrolRoutePacket.java` | 新建 | C→S 传输路径点列表 |
| `NetworkHandler.java` | 修改 | 注册新数据包 |
