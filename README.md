# NBSMusicPlugin

一个基于 **Minecraft Paper 1.8.8** 的服务端音乐插件，使用 **NoteBlockAPI** 播放 `.nbs` 音乐文件，提供箱子 GUI 浏览与播放控制，支持：

- 浏览 `music` 目录中的所有 NBS 音乐
- 在 GUI 中显示音乐名称、作者、时长、层数、文件名等信息
- 对自己播放音乐
- 对指定玩家播放音乐
- 对全服所有玩家播放音乐
- 对某个世界中的所有玩家播放音乐
- 单人 / 指定玩家播放时启用 **3D 音效**
- 在 GUI 中调整 3D 音效播放范围
- 每次打开 GUI 时自动刷新和缓存音乐列表

---

## 1. 功能介绍

### 音乐管理
插件启动后会自动创建插件数据目录，并生成：

```text
plugins/NBSMusicPlugin/
├── config.yml
└── music/
```

你只需要把 `.nbs` 音乐文件放入 `music` 目录中，插件就会在启动或刷新时自动扫描并缓存这些音乐。

### GUI 浏览
玩家执行 `/music` 后会打开一个箱子 GUI：

- 分页查看所有音乐
- 每首音乐显示：
  - 音乐名称
  - 作者
  - 原作者
  - 时长
  - 速度
  - 层数
  - 文件名
  - 描述（如果 NBS 文件中存在）

### 播放目标
在选择某首音乐后，会进入播放设置 GUI，可选择：

- 播放给自己
- 播放给指定玩家
- 播放给当前世界所有玩家
- 播放给全服所有玩家

### 3D 音效
在对单个玩家播放时，可以开启 3D 音效：

- 可在 GUI 中切换开启 / 关闭
- 可调整播放范围
- 适用于：
  - 播放给自己
  - 播放给指定玩家

---

## 2. 使用方法

### 安装插件
1. 将构建后的插件 Jar 放入服务端 `plugins/` 目录
2. 启动服务器
3. 插件会自动生成：
   - `plugins/NBSMusicPlugin/config.yml`
   - `plugins/NBSMusicPlugin/music/`

### 添加音乐
把 `.nbs` 文件放入：

```text
plugins/NBSMusicPlugin/music/
```

例如：

```text
plugins/NBSMusicPlugin/music/song1.nbs
plugins/NBSMusicPlugin/music/song2.nbs
```

然后执行：

```text
/music reload
```

或重新打开 GUI，即可刷新音乐缓存。

---

## 3. 命令说明

### `/music`
打开音乐 GUI。

**用法：**
```text
/music
```

---

### `/music play <音乐名>`
将指定音乐播放给自己。

**用法：**
```text
/music play MySong
```

如果音乐名中有空格，直接正常输入即可：

```text
/music play My Favorite Song
```

---

### `/music play <音乐名> <玩家名>`
将指定音乐播放给某个玩家。

**用法：**
```text
/music play MySong Steve
```

---

### `/music playall <音乐名>`
向全服所有在线玩家播放该音乐。

**用法：**
```text
/music playall MySong
```

---

### `/music world <世界名> <音乐名>`
向指定世界中的所有玩家播放该音乐。

**用法：**
```text
/music world world MySong
```

---

### `/music stop`
停止与你相关的当前播放。

**用法：**
```text
/music stop
```

---

### `/music reload`
重载配置文件并重新扫描音乐缓存。

**用法：**
```text
/music reload
```

---

## 4. 权限节点

### `nbsmusic.use`
允许玩家使用基础功能：

- 打开 GUI
- 播放给自己
- 停止自己的播放

默认：`true`

### `nbsmusic.admin`
允许管理员使用高级功能：

- 重载插件
- 全服播放
- 世界播放

默认：`op`

---

## 5. GUI 使用说明

## 主界面
执行 `/music` 后打开音乐浏览界面。

可进行以下操作：

- 点击音乐物品：进入播放设置界面
- 点击上一页 / 下一页：分页浏览音乐
- 点击刷新缓存：重新扫描 `music` 目录
- 点击关闭：关闭 GUI

## 播放设置界面
选择音乐后可进行以下操作：

- 切换目标类型：
  - 自己
  - 指定玩家
  - 当前世界
  - 全服
- 切换 3D 音效开关
- 调整 3D 音效范围
- 点击开始播放
- 点击停止播放
- 返回音乐列表

### 指定玩家说明
当你在 GUI 中点击“播放给指定玩家”后，插件会要求你在聊天栏输入目标玩家名：

- 输入玩家名：设置目标玩家
- 输入 `cancel`：取消输入

---

## 6. 配置文件说明

默认配置文件：`plugins/NBSMusicPlugin/config.yml`

示例配置：

```yaml
messages:
  prefix: "&6[NBSMusic]&r "
  no-permission: "&c你没有权限执行该操作。"
  player-only: "&c该命令只能由玩家执行。"
  player-not-found: "&c未找到该玩家。"
  music-not-found: "&c未找到对应音乐。"
  world-not-found: "&c未找到对应世界。"
  reloaded: "&a插件配置与音乐缓存已重载。"
  playback-started: "&a已开始播放音乐: &f%s"
  playback-stopped: "&e已停止当前播放。"
  target-playback-started: "&a已为玩家 &f%s &a播放音乐: &f%s"
  global-playback-started: "&a已向全服播放音乐: &f%s"
  world-playback-started: "&a已向世界 &f%s &a播放音乐: &f%s"

settings:
  auto-refresh-on-gui-open: true
  default-3d-enabled: false
  default-3d-range: 16
  min-3d-range: 4
  max-3d-range: 64
  gui-size: 54
  songs-per-page: 45
  music-item-material: JUKEBOX
  info-item-material: PAPER
  control-item-material: NOTE_BLOCK

paths:
  music-folder: music

gui:
  title: "&6&lNBS 音乐列表"
  settings-title: "&e&l播放设置"
  empty-slot-name: "&7暂无内容"
```

### 可调节内容
你可以根据需要修改：

- GUI 标题
- 提示消息
- 默认 3D 音效状态
- 默认 3D 范围
- 最小 / 最大 3D 范围
- 每页显示的歌曲数量
- GUI 中使用的物品材质

---

## 7. 项目结构

```text
NBSMusicPlugin/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── musicplugin/
        │           ├── NBSMusicPlugin.java
        │           ├── command/
        │           │   └── MusicCommand.java
        │           ├── config/
        │           │   └── ConfigManager.java
        │           ├── gui/
        │           │   ├── GUIListener.java
        │           │   ├── GUIUtils.java
        │           │   ├── MusicBrowserGUI.java
        │           │   └── PlaySettingsGUI.java
        │           └── music/
        │               ├── MusicManager.java
        │               ├── MusicPlayerManager.java
        │               └── MusicTrack.java
        └── resources/
            ├── config.yml
            └── plugin.yml
```

---

## 8. 构建方式

本项目使用 **Maven** 构建。

在项目目录下执行：

```bash
mvn package
```

构建完成后，生成的 Jar 一般位于：

```text
target/NBSMusicPlugin-1.0.0.jar
```

把它放入你的 Paper 1.8.8 服务端 `plugins/` 目录即可使用。

---

## 9. 适用版本

- **服务端核心**：Paper 1.8.8
- **Java**：Java 8
- **音乐格式**：`.nbs`

---

## 10. 插件可以做什么

这个插件适合以下用途：

- 服务器大厅背景音乐播放
- 活动 / 剧情音乐播放
- 给指定玩家播放专属音乐
- 在某个世界中播放区域性音乐
- 使用 GUI 快速管理 NBS 音乐而不需要频繁输入命令
- 通过 3D 音效实现更有沉浸感的单人音乐体验

---

## 11. 注意事项

1. 请确保 `.nbs` 文件格式正确，否则可能无法被成功加载。
2. 建议使用兼容 1.8.8 的 Paper 服务端和 Java 8 运行。
3. 如果新增了音乐文件但 GUI 中未显示，可执行：
   ```text
   /music reload
   ```
4. 3D 音效仅适用于单个玩家目标，不适用于全服和世界群体播放。
5. 如果某些音乐名称包含特殊字符，建议优先通过 GUI 选择播放。

---

## 12. 后续可扩展方向

如果你后续还想继续扩展，这个插件可以继续加入：

- GUI 搜索功能
- 收藏夹功能
- 循环播放 / 随机播放
- 播放队列
- 权限分组控制不同歌曲
- 多语言支持
- 更精细的世界 / 区域播放策略
