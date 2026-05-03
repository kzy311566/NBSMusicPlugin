package com.musicplugin.gui;

import com.musicplugin.NBSMusicPlugin;
import com.musicplugin.music.MusicTrack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaySettingsGUI {

    public static final int SLOT_TRACK_INFO = 4;
    public static final int SLOT_TARGET_SELF = 10;
    public static final int SLOT_TARGET_PLAYER = 11;
    public static final int SLOT_TARGET_WORLD = 12;
    public static final int SLOT_TARGET_GLOBAL = 13;
    public static final int SLOT_TOGGLE_3D = 15;
    public static final int SLOT_RANGE_MINUS = 16;
    public static final int SLOT_RANGE_PLUS = 17;
    public static final int SLOT_PLAY = 22;
    public static final int SLOT_STOP = 23;
    public static final int SLOT_BACK = 18;

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<UUID, Session>();

    private final NBSMusicPlugin plugin;

    public PlaySettingsGUI(NBSMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, MusicTrack track) {
        Session session = getOrCreateSession(player, track);
        session.setTrack(track);
        render(player, session);
    }

    public void reopen(Player player) {
        Session session = SESSIONS.get(player.getUniqueId());
        if (session == null || session.getTrack() == null) {
            return;
        }
        render(player, session);
    }

    public Session getSession(Player player) {
        return player == null ? null : SESSIONS.get(player.getUniqueId());
    }

    public Session getOrCreateSession(Player player, MusicTrack track) {
        Session session = SESSIONS.get(player.getUniqueId());
        if (session == null) {
            session = new Session();
            session.setTargetType(TargetType.SELF);
            session.setEnable3d(plugin.getConfigManager().isDefault3dEnabled());
            session.setRange(plugin.getConfigManager().getDefault3dRange());
            SESSIONS.put(player.getUniqueId(), session);
        }

        if (track != null) {
            session.setTrack(track);
        }

        return session;
    }

    public void clearSession(Player player) {
        if (player != null) {
            SESSIONS.remove(player.getUniqueId());
        }
    }

    public boolean isSettingsTitle(String title) {
        if (title == null) {
            return false;
        }
        String baseTitle = GUIUtils.color(plugin.getConfigManager().getSettingsTitle());
        return title.startsWith(baseTitle);
    }

    private void render(Player player, Session session) {
        MusicTrack track = session.getTrack();
        if (track == null) {
            return;
        }

        String title = plugin.getConfigManager().getSettingsTitle() + " &7- &f" + track.getDisplayName();
        Inventory inventory = Bukkit.createInventory(null, 27, GUIUtils.color(title));

        inventory.setItem(SLOT_TRACK_INFO, GUIUtils.createItem(
                GUIUtils.parseMaterial(plugin.getConfigManager().getInfoItemMaterial(), Material.PAPER),
                "&a" + track.getDisplayName(),
                "&7作者: &f" + track.getAuthor(),
                "&7原作者: &f" + track.getOriginalAuthor(),
                "&7时长: &f" + track.getFormattedDuration(),
                "&7层数: &f" + track.getLayerCount()
        ));

        inventory.setItem(SLOT_TARGET_SELF, GUIUtils.createItem(
                Material.SKULL_ITEM,
                session.getTargetType() == TargetType.SELF ? "&a播放给自己" : "&7播放给自己",
                "&7目标: &f当前打开菜单的玩家",
                "&e点击切换到该目标"
        ));

        inventory.setItem(SLOT_TARGET_PLAYER, GUIUtils.createItem(
                Material.NAME_TAG,
                session.getTargetType() == TargetType.PLAYER ? "&a播放给指定玩家" : "&7播放给指定玩家",
                "&7当前目标: &f" + (session.getTargetPlayerName() == null ? "未指定" : session.getTargetPlayerName()),
                "&e点击后默认锁定为自己，可在命令中指定玩家"
        ));

        World playerWorld = player.getWorld();
        inventory.setItem(SLOT_TARGET_WORLD, GUIUtils.createItem(
                Material.GRASS,
                session.getTargetType() == TargetType.WORLD ? "&a播放给当前世界" : "&7播放给当前世界",
                "&7世界名: &f" + (playerWorld == null ? "Unknown" : playerWorld.getName()),
                "&e点击切换到当前世界播放"
        ));

        inventory.setItem(SLOT_TARGET_GLOBAL, GUIUtils.createItem(
                Material.BEACON,
                session.getTargetType() == TargetType.GLOBAL ? "&a播放给全服" : "&7播放给全服",
                "&7目标: &f所有在线玩家",
                "&e点击切换到全服播放"
        ));

        boolean canUse3d = session.getTargetType() == TargetType.SELF || session.getTargetType() == TargetType.PLAYER;
        inventory.setItem(SLOT_TOGGLE_3D, GUIUtils.createItem(
                Material.NOTE_BLOCK,
                session.isEnable3d() ? "&a3D 音效: 已开启" : "&c3D 音效: 已关闭",
                "&7仅对单人/指定玩家播放生效",
                canUse3d ? "&e点击切换 3D 音效状态" : "&c当前目标下不可用"
        ));

        inventory.setItem(SLOT_RANGE_MINUS, GUIUtils.createItem(
                Material.REDSTONE,
                "&c减小范围",
                "&7当前范围: &f" + session.getRange(),
                "&e点击 -4"
        ));

        inventory.setItem(SLOT_RANGE_PLUS, GUIUtils.createItem(
                Material.EMERALD,
                "&a增大范围",
                "&7当前范围: &f" + session.getRange(),
                "&e点击 +4"
        ));

        inventory.setItem(SLOT_PLAY, GUIUtils.createItem(
                Material.RECORD_8,
                "&a开始播放",
                "&7歌曲: &f" + track.getDisplayName(),
                "&7目标类型: &f" + session.getTargetType().getDisplayName(),
                "&e点击立即播放"
        ));

        inventory.setItem(SLOT_STOP, GUIUtils.createItem(
                Material.RECORD_7,
                "&c停止播放",
                "&7停止与你相关的当前播放"
        ));

        inventory.setItem(SLOT_BACK, GUIUtils.createItem(
                Material.ARROW,
                "&e返回列表",
                "&7点击返回音乐浏览界面"
        ));

        player.openInventory(inventory);
    }

    public enum TargetType {
        SELF("自己"),
        PLAYER("指定玩家"),
        WORLD("当前世界"),
        GLOBAL("全服");

        private final String displayName;

        TargetType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class Session {

        private MusicTrack track;
        private TargetType targetType;
        private boolean enable3d;
        private int range;
        private String targetPlayerName;

        public MusicTrack getTrack() {
            return track;
        }

        public void setTrack(MusicTrack track) {
            this.track = track;
        }

        public TargetType getTargetType() {
            return targetType;
        }

        public void setTargetType(TargetType targetType) {
            this.targetType = targetType;
        }

        public boolean isEnable3d() {
            return enable3d;
        }

        public void setEnable3d(boolean enable3d) {
            this.enable3d = enable3d;
        }

        public int getRange() {
            return range;
        }

        public void setRange(int range) {
            this.range = range;
        }

        public String getTargetPlayerName() {
            return targetPlayerName;
        }

        public void setTargetPlayerName(String targetPlayerName) {
            this.targetPlayerName = targetPlayerName;
        }
    }
}
