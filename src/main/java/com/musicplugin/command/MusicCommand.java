package com.musicplugin.command;

import com.musicplugin.NBSMusicPlugin;
import com.musicplugin.gui.MusicBrowserGUI;
import com.musicplugin.music.MusicTrack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicCommand implements CommandExecutor, TabCompleter {

    private final NBSMusicPlugin plugin;
    private final MusicBrowserGUI browserGUI;

    public MusicCommand(NBSMusicPlugin plugin) {
        this.plugin = plugin;
        this.browserGUI = new MusicBrowserGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            if (!sender.hasPermission("nbsmusic.use")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            browserGUI.open((Player) sender, 0);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("nbsmusic.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            plugin.getConfigManager().reload();
            plugin.getMusicManager().refresh();
            sender.sendMessage(plugin.getConfigManager().getMessage("reloaded"));
            return true;
        }

        if (sub.equals("stop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            if (!sender.hasPermission("nbsmusic.use")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            Player player = (Player) sender;
            plugin.getMusicPlayerManager().stopForPlayer(player);
            sender.sendMessage(plugin.getConfigManager().getMessage("playback-stopped"));
            return true;
        }

        if (sub.equals("play")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            if (!sender.hasPermission("nbsmusic.use")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("/music play <音乐名> [玩家名]");
                return true;
            }

            Player player = (Player) sender;

            Player targetPlayer = null;
            String songQuery;

            if (args.length >= 3) {
                Player possiblePlayer = Bukkit.getPlayerExact(args[args.length - 1]);
                if (possiblePlayer != null) {
                    targetPlayer = possiblePlayer;
                    songQuery = joinArgs(args, 1, args.length - 1);
                } else {
                    songQuery = joinArgs(args, 1, args.length);
                }
            } else {
                songQuery = joinArgs(args, 1, args.length);
            }

            MusicTrack track = plugin.getMusicManager().findTrack(songQuery);
            if (track == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("music-not-found"));
                return true;
            }

            if (targetPlayer == null) {
                plugin.getMusicPlayerManager().playToSelf(player, track, plugin.getConfigManager().isDefault3dEnabled(), plugin.getConfigManager().getDefault3dRange());
                sender.sendMessage(plugin.getConfigManager().getMessage("playback-started", track.getDisplayName()));
            } else {
                plugin.getMusicPlayerManager().playToTarget(player, targetPlayer, track, plugin.getConfigManager().isDefault3dEnabled(), plugin.getConfigManager().getDefault3dRange());
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("playback-started", track.getDisplayName()));
            }

            return true;
        }

        if (sub.equals("playall")) {
            if (!sender.hasPermission("nbsmusic.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("/music playall <音乐名>");
                return true;
            }

            String songQuery = joinArgs(args, 1, args.length);
            MusicTrack track = plugin.getMusicManager().findTrack(songQuery);
            if (track == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("music-not-found"));
                return true;
            }

            plugin.getMusicPlayerManager().playToAll(track);
            sender.sendMessage(plugin.getConfigManager().getMessage("global-playback-started", track.getDisplayName()));
            return true;
        }

        if (sub.equals("world")) {
            if (!sender.hasPermission("nbsmusic.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage("/music world <世界名> <音乐名>");
                return true;
            }

            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("world-not-found"));
                return true;
            }

            String songQuery = joinArgs(args, 2, args.length);
            MusicTrack track = plugin.getMusicManager().findTrack(songQuery);
            if (track == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("music-not-found"));
                return true;
            }

            plugin.getMusicPlayerManager().playToWorld(world, track);
            sender.sendMessage(plugin.getConfigManager().getMessage("world-playback-started", world.getName(), track.getDisplayName()));
            return true;
        }

        sender.sendMessage("/music");
        sender.sendMessage("/music play <音乐名> [玩家名]");
        sender.sendMessage("/music playall <音乐名>");
        sender.sendMessage("/music world <世界名> <音乐名>");
        sender.sendMessage("/music stop");
        sender.sendMessage("/music reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(args[0], listOf("play", "playall", "world", "stop", "reload"));
        }

        if (args[0].equalsIgnoreCase("play")) {
            if (args.length >= 2) {
                List<String> suggestions = new ArrayList<String>();
                suggestions.addAll(plugin.getMusicManager().getTrackNameSuggestions());

                if (args.length >= 3) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }

                return filterStartsWith(args[args.length - 1], suggestions);
            }
        }

        if (args[0].equalsIgnoreCase("playall")) {
            if (args.length >= 2) {
                return filterStartsWith(args[args.length - 1], plugin.getMusicManager().getTrackNameSuggestions());
            }
        }

        if (args[0].equalsIgnoreCase("world")) {
            if (args.length == 2) {
                List<String> worlds = new ArrayList<String>();
                for (World world : Bukkit.getWorlds()) {
                    worlds.add(world.getName());
                }
                return filterStartsWith(args[1], worlds);
            }

            if (args.length >= 3) {
                return filterStartsWith(args[args.length - 1], plugin.getMusicManager().getTrackNameSuggestions());
            }
        }

        return Collections.emptyList();
    }

    private String joinArgs(String[] args, int startInclusive, int endExclusive) {
        StringBuilder builder = new StringBuilder();
        for (int i = startInclusive; i < endExclusive; i++) {
            if (i > startInclusive) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString().trim();
    }

    private List<String> filterStartsWith(String input, List<String> values) {
        String lower = input == null ? "" : input.toLowerCase();
        List<String> result = new ArrayList<String>();

        for (String value : values) {
            if (value != null && value.toLowerCase().startsWith(lower)) {
                result.add(value);
            }
        }

        Collections.sort(result);
        return result;
    }

    private List<String> listOf(String... values) {
        List<String> result = new ArrayList<String>();
        if (values != null) {
            Collections.addAll(result, values);
        }
        return result;
    }
}
