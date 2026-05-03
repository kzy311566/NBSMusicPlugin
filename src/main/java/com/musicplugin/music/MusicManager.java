package com.musicplugin.music;

import com.musicplugin.NBSMusicPlugin;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {

    private final NBSMusicPlugin plugin;
    private final Map<String, MusicTrack> cachedTracksByKey;
    private final List<MusicTrack> cachedTracks;

    public MusicManager(NBSMusicPlugin plugin) {
        this.plugin = plugin;
        this.cachedTracksByKey = new ConcurrentHashMap<String, MusicTrack>();
        this.cachedTracks = new ArrayList<MusicTrack>();
    }

    public synchronized void loadAllMusic() {
        cachedTracksByKey.clear();
        cachedTracks.clear();

        File musicFolder = plugin.getConfigManager().getMusicFolder();
        if (musicFolder == null || !musicFolder.exists()) {
            plugin.getLogger().warning("Music folder does not exist yet.");
            return;
        }

        File[] files = musicFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".nbs");
            }
        });

        if (files == null || files.length == 0) {
            plugin.getLogger().info("No .nbs files found in " + musicFolder.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                Song song = NBSDecoder.parse(file);
                if (song == null) {
                    plugin.getLogger().warning("Failed to decode song file: " + file.getName());
                    continue;
                }

                MusicTrack track = new MusicTrack(file, song);
                cachedTracksByKey.put(track.getCacheKey(), track);
                cachedTracks.add(track);
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not load NBS file " + file.getName() + ": " + exception.getMessage());
            }
        }

        Collections.sort(cachedTracks, new Comparator<MusicTrack>() {
            @Override
            public int compare(MusicTrack first, MusicTrack second) {
                return first.getDisplayName().compareToIgnoreCase(second.getDisplayName());
            }
        });

        plugin.getLogger().info("Loaded " + cachedTracks.size() + " music track(s).");
    }

    public synchronized void refresh() {
        loadAllMusic();
    }

    public synchronized List<MusicTrack> getAllTracks() {
        return new ArrayList<MusicTrack>(cachedTracks);
    }

    public synchronized int getTrackCount() {
        return cachedTracks.size();
    }

    public synchronized MusicTrack getTrackByIndex(int index) {
        if (index < 0 || index >= cachedTracks.size()) {
            return null;
        }
        return cachedTracks.get(index);
    }

    public synchronized MusicTrack findTrack(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.trim().toLowerCase();
        MusicTrack exact = cachedTracksByKey.get(normalized);
        if (exact != null) {
            return exact;
        }

        for (MusicTrack track : cachedTracks) {
            if (track.getDisplayName().equalsIgnoreCase(input)
                    || track.getFileName().equalsIgnoreCase(input)
                    || track.getCacheKey().equalsIgnoreCase(normalized)) {
                return track;
            }
        }

        for (MusicTrack track : cachedTracks) {
            if (track.getDisplayName().toLowerCase().contains(normalized)
                    || track.getFileName().toLowerCase().contains(normalized)) {
                return track;
            }
        }

        return null;
    }

    public synchronized List<String> getTrackNameSuggestions() {
        List<String> suggestions = new ArrayList<String>();
        for (MusicTrack track : cachedTracks) {
            suggestions.add(track.getDisplayName());
        }
        return suggestions;
    }
}
