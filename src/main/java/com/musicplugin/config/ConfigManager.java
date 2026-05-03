package com.musicplugin.config;

import com.musicplugin.NBSMusicPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {

    private final NBSMusicPlugin plugin;

    private File musicFolder;

    public ConfigManager(NBSMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        ensureDataFolders();
    }

    public void reload() {
        plugin.reloadConfig();
        ensureDataFolders();
    }

    private void ensureDataFolders() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed to create plugin data folder: " + plugin.getDataFolder().getAbsolutePath());
        }

        String musicFolderName = plugin.getConfig().getString("paths.music-folder", "music");
        this.musicFolder = new File(plugin.getDataFolder(), musicFolderName);

        if (!musicFolder.exists() && !musicFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create music folder: " + musicFolder.getAbsolutePath());
        }
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public File getMusicFolder() {
        return musicFolder;
    }

    public boolean isAutoRefreshOnGuiOpen() {
        return getConfig().getBoolean("settings.auto-refresh-on-gui-open", true);
    }

    public boolean isDefault3dEnabled() {
        return getConfig().getBoolean("settings.default-3d-enabled", false);
    }

    public int getDefault3dRange() {
        return getConfig().getInt("settings.default-3d-range", 16);
    }

    public int getMin3dRange() {
        return getConfig().getInt("settings.min-3d-range", 4);
    }

    public int getMax3dRange() {
        return getConfig().getInt("settings.max-3d-range", 64);
    }

    public int getGuiSize() {
        return getConfig().getInt("settings.gui-size", 54);
    }

    public int getSongsPerPage() {
        return getConfig().getInt("settings.songs-per-page", 45);
    }

    public String getMusicItemMaterial() {
        return getConfig().getString("settings.music-item-material", "JUKEBOX");
    }

    public String getInfoItemMaterial() {
        return getConfig().getString("settings.info-item-material", "PAPER");
    }

    public String getControlItemMaterial() {
        return getConfig().getString("settings.control-item-material", "NOTE_BLOCK");
    }

    public String getGuiTitle() {
        return colorize(getConfig().getString("gui.title", "&6&lNBS 音乐列表"));
    }

    public String getSettingsTitle() {
        return colorize(getConfig().getString("gui.settings-title", "&e&l播放设置"));
    }

    public String getEmptySlotName() {
        return colorize(getConfig().getString("gui.empty-slot-name", "&7暂无内容"));
    }

    public String getMessage(String path) {
        String prefix = colorize(getConfig().getString("messages.prefix", "&6[NBSMusic]&r "));
        String value = colorize(getConfig().getString("messages." + path, ""));
        return prefix + value;
    }

    public String getMessage(String path, Object... args) {
        return String.format(getMessage(path), args);
    }

    public String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}
