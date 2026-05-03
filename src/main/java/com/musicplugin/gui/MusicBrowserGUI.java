package com.musicplugin.gui;

import com.musicplugin.NBSMusicPlugin;
import com.musicplugin.music.MusicTrack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MusicBrowserGUI {

    public static final int SLOT_PREVIOUS = 45;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_REFRESH = 51;
    public static final int SLOT_NEXT = 53;
    public static final int SLOT_CLOSE = 50;
    public static final int MAX_MUSIC_SLOTS = 45;

    private final NBSMusicPlugin plugin;

    public MusicBrowserGUI(NBSMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        if (plugin.getConfigManager().isAutoRefreshOnGuiOpen()) {
            plugin.getMusicManager().refresh();
        }

        List<MusicTrack> tracks = plugin.getMusicManager().getAllTracks();
        int pageSize = plugin.getConfigManager().getSongsPerPage();
        if (pageSize <= 0 || pageSize > MAX_MUSIC_SLOTS) {
            pageSize = MAX_MUSIC_SLOTS;
        }

        int totalPages = Math.max(1, (int) Math.ceil(tracks.size() / (double) pageSize));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        String title = plugin.getConfigManager().getGuiTitle() + " &7(" + (currentPage + 1) + "/" + totalPages + ")";
        Inventory inventory = Bukkit.createInventory(null, plugin.getConfigManager().getGuiSize(), GUIUtils.color(title));

        fillMusicItems(inventory, tracks, currentPage, pageSize);
        fillControls(inventory, tracks.size(), currentPage, totalPages);

        player.openInventory(inventory);
    }

    private void fillMusicItems(Inventory inventory, List<MusicTrack> tracks, int currentPage, int pageSize) {
        Material musicMaterial = GUIUtils.parseMaterial(plugin.getConfigManager().getMusicItemMaterial(), Material.JUKEBOX);

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, tracks.size());

        for (int slot = 0; slot < MAX_MUSIC_SLOTS; slot++) {
            int trackIndex = startIndex + slot;
            if (trackIndex >= endIndex) {
                break;
            }

            MusicTrack track = tracks.get(trackIndex);
            List<String> lore = new ArrayList<String>();
            lore.add("&7作者: &f" + track.getAuthor());
            lore.add("&7原作者: &f" + track.getOriginalAuthor());
            lore.add("&7时长: &f" + track.getFormattedDuration());
            lore.add("&7速度: &f" + track.getTempo());
            lore.add("&7层数: &f" + track.getLayerCount());
            lore.add("&7文件名: &f" + track.getFileName());

            if (!track.getDescription().isEmpty()) {
                lore.add("&7描述: &f" + track.getDescription());
            }

            lore.add("&8");
            lore.add("&e左键点击进入播放设置");

            inventory.setItem(slot, GUIUtils.createItem(
                    musicMaterial,
                    "&a" + track.getDisplayName(),
                    lore
            ));
        }
    }

    private void fillControls(Inventory inventory, int totalTracks, int currentPage, int totalPages) {
        Material infoMaterial = GUIUtils.parseMaterial(plugin.getConfigManager().getInfoItemMaterial(), Material.PAPER);
        Material controlMaterial = GUIUtils.parseMaterial(plugin.getConfigManager().getControlItemMaterial(), Material.NOTE_BLOCK);

        inventory.setItem(SLOT_PREVIOUS, GUIUtils.createItem(
                controlMaterial,
                currentPage > 0 ? "&a上一页" : "&7上一页",
                "&7当前页: &f" + (currentPage + 1),
                currentPage > 0 ? "&e点击前往上一页" : "&c已经是第一页"
        ));

        inventory.setItem(SLOT_INFO, GUIUtils.createItem(
                infoMaterial,
                "&6音乐列表信息",
                "&7音乐数量: &f" + totalTracks,
                "&7当前页数: &f" + (currentPage + 1) + "/" + totalPages,
                "&7每页显示: &f" + Math.min(plugin.getConfigManager().getSongsPerPage(), MAX_MUSIC_SLOTS)
        ));

        inventory.setItem(SLOT_CLOSE, GUIUtils.createItem(
                Material.BARRIER,
                "&c关闭界面",
                "&7点击关闭当前菜单"
        ));

        inventory.setItem(SLOT_REFRESH, GUIUtils.createItem(
                controlMaterial,
                "&b刷新缓存",
                "&7重新扫描 music 目录",
                "&e点击立即刷新音乐列表"
        ));

        inventory.setItem(SLOT_NEXT, GUIUtils.createItem(
                controlMaterial,
                currentPage + 1 < totalPages ? "&a下一页" : "&7下一页",
                "&7当前页: &f" + (currentPage + 1),
                currentPage + 1 < totalPages ? "&e点击前往下一页" : "&c已经是最后一页"
        ));
    }

    public boolean isBrowserTitle(String title) {
        if (title == null) {
            return false;
        }

        String baseTitle = GUIUtils.color(plugin.getConfigManager().getGuiTitle());
        return title.startsWith(baseTitle);
    }

    public int extractPage(String title) {
        if (title == null) {
            return 0;
        }

        int left = title.lastIndexOf('(');
        int slash = title.lastIndexOf('/');
        if (left == -1 || slash == -1 || slash <= left) {
            return 0;
        }

        try {
            String current = title.substring(left + 1, slash).replaceAll("[^0-9]", "");
            int page = Integer.parseInt(current);
            return Math.max(0, page - 1);
        } catch (Exception ignored) {
            return 0;
        }
    }
}
