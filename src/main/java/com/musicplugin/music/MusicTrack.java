package com.musicplugin.music;

import com.xxmicloxx.NoteBlockAPI.model.Song;

import java.io.File;

public class MusicTrack {

    private final File file;
    private final String fileName;
    private final String displayName;
    private final String author;
    private final String originalAuthor;
    private final String description;
    private final float tempo;
    private final int length;
    private final int layerCount;
    private final int durationSeconds;
    private final Song song;

    public MusicTrack(File file, Song song) {
        this.file = file;
        this.fileName = file.getName();
        this.song = song;
        this.displayName = sanitize(song.getTitle(), stripExtension(file.getName()));
        this.author = sanitize(song.getAuthor(), "Unknown");
        this.originalAuthor = sanitize(song.getOriginalAuthor(), "Unknown");
        this.description = sanitize(song.getDescription(), "");
        this.tempo = song.getSpeed();
        this.length = song.getLength();
        this.layerCount = song.getLayerHashMap() == null ? 0 : song.getLayerHashMap().size();
        this.durationSeconds = calculateDuration(song);
    }

    private int calculateDuration(Song song) {
        if (song.getSpeed() <= 0) {
            return 0;
        }

        double seconds = song.getLength() / song.getSpeed() / 20.0D;
        return (int) Math.ceil(seconds);
    }

    private String sanitize(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index >= 0 ? name.substring(0, index) : name;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginalAuthor() {
        return originalAuthor;
    }

    public String getDescription() {
        return description;
    }

    public float getTempo() {
        return tempo;
    }

    public int getLength() {
        return length;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Song getSong() {
        return song;
    }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getCacheKey() {
        return stripExtension(fileName).toLowerCase();
    }
}
