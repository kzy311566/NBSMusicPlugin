package com.musicplugin;

import com.musicplugin.command.MusicCommand;
import com.musicplugin.config.ConfigManager;
import com.musicplugin.gui.GUIListener;
import com.musicplugin.music.MusicManager;
import com.musicplugin.music.MusicPlayerManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class NBSMusicPlugin extends JavaPlugin {

    private static NBSMusicPlugin instance;

    private ConfigManager configManager;
    private MusicManager musicManager;
    private MusicPlayerManager musicPlayerManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.initialize();

        this.musicManager = new MusicManager(this);
        this.musicManager.loadAllMusic();

        this.musicPlayerManager = new MusicPlayerManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("NBSMusicPlugin has been enabled.");
    }

    @Override
    public void onDisable() {
        if (musicPlayerManager != null) {
            musicPlayerManager.stopAll();
        }

        getLogger().info("NBSMusicPlugin has been disabled.");
    }

    private void registerCommands() {
        MusicCommand musicCommand = new MusicCommand(this);

        PluginCommand command = getCommand("music");
        if (command != null) {
            command.setExecutor(musicCommand);
            command.setTabCompleter(musicCommand);
        } else {
            getLogger().warning("Command 'music' is not defined in plugin.yml.");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }

    public static NBSMusicPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public MusicPlayerManager getMusicPlayerManager() {
        return musicPlayerManager;
    }
}
