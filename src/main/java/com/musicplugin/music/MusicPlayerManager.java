package com.musicplugin.music;

import com.musicplugin.NBSMusicPlugin;
import com.xxmicloxx.NoteBlockAPI.songplayer.PositionSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MusicPlayerManager {

    private final NBSMusicPlugin plugin;
    private final Map<UUID, SongPlayer> personalPlayers;
    private final Map<String, SongPlayer> scopedPlayers;

    public MusicPlayerManager(NBSMusicPlugin plugin) {
        this.plugin = plugin;
        this.personalPlayers = new ConcurrentHashMap<UUID, SongPlayer>();
        this.scopedPlayers = new ConcurrentHashMap<String, SongPlayer>();
    }

    public void playToSelf(Player player, MusicTrack track, boolean enable3d, int range) {
        if (player == null || track == null) {
            return;
        }

        stopForPlayer(player);

        SongPlayer songPlayer = createSongPlayer(track, player, enable3d, range);
        songPlayer.addPlayer(player);
        configureSongPlayer(songPlayer);
        songPlayer.setPlaying(true);

        personalPlayers.put(player.getUniqueId(), songPlayer);
    }

    public void playToTarget(Player source, Player target, MusicTrack track, boolean enable3d, int range) {
        if (target == null || track == null) {
            return;
        }

        stopForPlayer(target);

        SongPlayer songPlayer = createSongPlayer(track, target, enable3d, range);
        songPlayer.addPlayer(target);
        configureSongPlayer(songPlayer);
        songPlayer.setPlaying(true);

        personalPlayers.put(target.getUniqueId(), songPlayer);

        if (source != null && !source.getUniqueId().equals(target.getUniqueId())) {
            source.sendMessage(plugin.getConfigManager().getMessage("target-playback-started", target.getName(), track.getDisplayName()));
        }
    }

    public void playToAll(MusicTrack track) {
        if (track == null) {
            return;
        }

        stopScoped("global");

        RadioSongPlayer songPlayer = new RadioSongPlayer(track.getSong());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            songPlayer.addPlayer(onlinePlayer);
        }

        configureSongPlayer(songPlayer);
        songPlayer.setPlaying(true);
        scopedPlayers.put("global", songPlayer);
    }

    public void playToWorld(World world, MusicTrack track) {
        if (world == null || track == null) {
            return;
        }

        String key = "world:" + world.getName().toLowerCase();
        stopScoped(key);

        RadioSongPlayer songPlayer = new RadioSongPlayer(track.getSong());
        for (Player player : world.getPlayers()) {
            songPlayer.addPlayer(player);
        }

        configureSongPlayer(songPlayer);
        songPlayer.setPlaying(true);
        scopedPlayers.put(key, songPlayer);
    }

    public void stopForPlayer(Player player) {
        if (player == null) {
            return;
        }

        SongPlayer existing = personalPlayers.remove(player.getUniqueId());
        if (existing != null) {
            try {
                existing.removePlayer(player);
            } catch (Exception ignored) {
            }

            try {
                existing.setPlaying(false);
            } catch (Exception ignored) {
            }

            try {
                existing.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    public void stopScoped(String key) {
        SongPlayer existing = scopedPlayers.remove(key);
        if (existing != null) {
            try {
                existing.setPlaying(false);
            } catch (Exception ignored) {
            }

            try {
                existing.destroy();
            } catch (Exception ignored) {
            }
        }
    }

    public void stopWorld(World world) {
        if (world == null) {
            return;
        }
        stopScoped("world:" + world.getName().toLowerCase());
    }

    public void stopAll() {
        for (UUID uuid : personalPlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                stopForPlayer(player);
            } else {
                SongPlayer existing = personalPlayers.remove(uuid);
                if (existing != null) {
                    try {
                        existing.setPlaying(false);
                    } catch (Exception ignored) {
                    }
                    try {
                        existing.destroy();
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        for (String key : scopedPlayers.keySet()) {
            stopScoped(key);
        }
    }

    public boolean isPlayingFor(Player player) {
        return player != null && personalPlayers.containsKey(player.getUniqueId());
    }

    private SongPlayer createSongPlayer(MusicTrack track, Player target, boolean enable3d, int range) {
        if (enable3d && target != null) {
            PositionSongPlayer songPlayer = new PositionSongPlayer(track.getSong());
            Location location = target.getLocation();
            songPlayer.setTargetLocation(location);
            songPlayer.setDistance(range);
            return songPlayer;
        }

        return new RadioSongPlayer(track.getSong());
    }

    private void configureSongPlayer(SongPlayer songPlayer) {
        try {
            songPlayer.setAutoDestroy(true);
        } catch (Exception ignored) {
        }

        try {
            songPlayer.setLoop(false);
        } catch (Exception ignored) {
        }

        try {
            songPlayer.setVolume((byte) 100);
        } catch (Exception ignored) {
        }
    }
}
