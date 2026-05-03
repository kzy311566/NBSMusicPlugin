package com.musicplugin.gui;

import com.musicplugin.NBSMusicPlugin;
import com.musicplugin.gui.PlaySettingsGUI.Session;
import com.musicplugin.gui.PlaySettingsGUI.TargetType;
import com.musicplugin.music.MusicTrack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GUIListener implements Listener {

    private final NBSMusicPlugin plugin;
    private final MusicBrowserGUI browserGUI;
    private final PlaySettingsGUI settingsGUI;
    private final Map<UUID, Boolean> awaitingTargetInput;

    public GUIListener(NBSMusicPlugin plugin) {
        this.plugin = plugin;
        this.browserGUI = new MusicBrowserGUI(plugin);
        this.settingsGUI = new PlaySettingsGUI(plugin);
        this.awaitingTargetInput = new ConcurrentHashMap<UUID, Boolean>();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        InventoryView view = event.getView();
        String title = view.getTitle();
        Player player = (Player) event.getWhoClicked();

        if (browserGUI.isBrowserTitle(title)) {
            handleBrowserClick(event, player, title);
            return;
        }

        if (settingsGUI.isSettingsTitle(title)) {
            handleSettingsClick(event, player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (awaitingTargetInput.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + GUIUtils.color("&e请在聊天栏输入目标玩家名，输入 &ccancel &e取消。"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (!awaitingTargetInput.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        String input = event.getMessage() == null ? "" : event.getMessage().trim();
        awaitingTargetInput.remove(player.getUniqueId());

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(plugin.getConfigManager().colorize("&e已取消指定玩家输入。"));
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    settingsGUI.reopen(player);
                }
            });
            return;
        }

        final Session session = settingsGUI.getOrCreateSession(player, null);
        session.setTargetType(TargetType.PLAYER);
        session.setTargetPlayerName(input);

        player.sendMessage(plugin.getConfigManager().colorize("&a已设置目标玩家为: &f" + input));
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                settingsGUI.reopen(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getMusicPlayerManager().stopForPlayer(player);
        settingsGUI.clearSession(player);
        awaitingTargetInput.remove(player.getUniqueId());
    }

    private void handleBrowserClick(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        int slot = event.getRawSlot();
        int currentPage = browserGUI.extractPage(title);

        if (slot == MusicBrowserGUI.SLOT_PREVIOUS) {
            browserGUI.open(player, Math.max(0, currentPage - 1));
            return;
        }

        if (slot == MusicBrowserGUI.SLOT_NEXT) {
            browserGUI.open(player, currentPage + 1);
            return;
        }

        if (slot == MusicBrowserGUI.SLOT_REFRESH) {
            plugin.getMusicManager().refresh();
            browserGUI.open(player, currentPage);
            return;
        }

        if (slot == MusicBrowserGUI.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }

        if (slot < 0 || slot >= MusicBrowserGUI.MAX_MUSIC_SLOTS) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == null) {
            return;
        }

        int pageSize = plugin.getConfigManager().getSongsPerPage();
        if (pageSize <= 0 || pageSize > MusicBrowserGUI.MAX_MUSIC_SLOTS) {
            pageSize = MusicBrowserGUI.MAX_MUSIC_SLOTS;
        }

        int index = currentPage * pageSize + slot;
        MusicTrack track = plugin.getMusicManager().getTrackByIndex(index);
        if (track == null) {
            return;
        }

        settingsGUI.open(player, track);
    }

    private void handleSettingsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        Session session = settingsGUI.getSession(player);
        if (session == null || session.getTrack() == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot == PlaySettingsGUI.SLOT_TARGET_SELF) {
            session.setTargetType(TargetType.SELF);
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_TARGET_PLAYER) {
            session.setTargetType(TargetType.PLAYER);
            session.setTargetPlayerName(player.getName());
            awaitingTargetInput.put(player.getUniqueId(), Boolean.TRUE);
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().colorize("&e请在聊天栏输入目标玩家名，输入 &ccancel &e取消。"));
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_TARGET_WORLD) {
            session.setTargetType(TargetType.WORLD);
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_TARGET_GLOBAL) {
            session.setTargetType(TargetType.GLOBAL);
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_TOGGLE_3D) {
            if (session.getTargetType() == TargetType.SELF || session.getTargetType() == TargetType.PLAYER) {
                session.setEnable3d(!session.isEnable3d());
            }
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_RANGE_MINUS) {
            int min = plugin.getConfigManager().getMin3dRange();
            session.setRange(Math.max(min, session.getRange() - 4));
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_RANGE_PLUS) {
            int max = plugin.getConfigManager().getMax3dRange();
            session.setRange(Math.min(max, session.getRange() + 4));
            settingsGUI.reopen(player);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_PLAY) {
            playFromSession(player, session);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_STOP) {
            stopFromSession(player, session);
            return;
        }

        if (slot == PlaySettingsGUI.SLOT_BACK) {
            browserGUI.open(player, 0);
        }
    }

    private void playFromSession(Player player, Session session) {
        MusicTrack track = session.getTrack();
        if (track == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("music-not-found"));
            return;
        }

        if (session.getTargetType() == TargetType.SELF) {
            plugin.getMusicPlayerManager().playToSelf(player, track, session.isEnable3d(), session.getRange());
            player.sendMessage(plugin.getConfigManager().getMessage("playback-started", track.getDisplayName()));
            return;
        }

        if (session.getTargetType() == TargetType.PLAYER) {
            String targetName = session.getTargetPlayerName();
            if (targetName == null || targetName.trim().isEmpty()) {
                player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return;
            }

            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return;
            }

            plugin.getMusicPlayerManager().playToTarget(player, target, track, session.isEnable3d(), session.getRange());
            target.sendMessage(plugin.getConfigManager().getMessage("playback-started", track.getDisplayName()));
            return;
        }

        if (session.getTargetType() == TargetType.WORLD) {
            World world = player.getWorld();
            plugin.getMusicPlayerManager().playToWorld(world, track);
            player.sendMessage(plugin.getConfigManager().getMessage("world-playback-started", world.getName(), track.getDisplayName()));
            return;
        }

        plugin.getMusicPlayerManager().playToAll(track);
        player.sendMessage(plugin.getConfigManager().getMessage("global-playback-started", track.getDisplayName()));
    }

    private void stopFromSession(Player player, Session session) {
        if (session.getTargetType() == TargetType.SELF) {
            plugin.getMusicPlayerManager().stopForPlayer(player);
            player.sendMessage(plugin.getConfigManager().getMessage("playback-stopped"));
            return;
        }

        if (session.getTargetType() == TargetType.PLAYER) {
            String targetName = session.getTargetPlayerName();
            Player target = targetName == null ? null : Bukkit.getPlayerExact(targetName);
            if (target != null) {
                plugin.getMusicPlayerManager().stopForPlayer(target);
            }
            player.sendMessage(plugin.getConfigManager().getMessage("playback-stopped"));
            return;
        }

        if (session.getTargetType() == TargetType.WORLD) {
            plugin.getMusicPlayerManager().stopWorld(player.getWorld());
            player.sendMessage(plugin.getConfigManager().getMessage("playback-stopped"));
            return;
        }

        plugin.getMusicPlayerManager().stopScoped("global");
        player.sendMessage(plugin.getConfigManager().getMessage("playback-stopped"));
    }
}
