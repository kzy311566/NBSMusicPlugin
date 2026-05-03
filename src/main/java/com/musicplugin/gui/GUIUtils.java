package com.musicplugin.gui;

import com.musicplugin.NBSMusicPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class GUIUtils {

    private GUIUtils() {
    }

    public static Material parseMaterial(String name, Material fallback) {
        if (name == null || name.trim().isEmpty()) {
            return fallback;
        }

        Material material = Material.matchMaterial(name.trim());
        return material == null ? fallback : material;
    }

    public static ItemStack createItem(Material material, String displayName, List<String> lore) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(color(displayName));

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<String>();
                for (String line : lore) {
                    coloredLore.add(color(line));
                }
                itemMeta.setLore(coloredLore);
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    public static ItemStack createItem(Material material, String displayName, String... lore) {
        List<String> loreLines = new ArrayList<String>();
        if (lore != null) {
            for (String line : lore) {
                loreLines.add(line);
            }
        }
        return createItem(material, displayName, loreLines);
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static String pluginColor(NBSMusicPlugin plugin, String text) {
        return plugin.getConfigManager().colorize(text);
    }
}
