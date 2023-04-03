package com.github.rabscattle.gemstonecoordsfinder.utils;

import com.github.rabscattle.gemstonecoordsfinder.GemstoneCoordsFinder;
import com.github.rabscattle.gemstonecoordsfinder.structs.GemstoneType;
import com.github.rabscattle.gemstonecoordsfinder.structs.Waypoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

public class Storage {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void saveGemstone(Map<GemstoneType, Set<Waypoint>> gemstoneTypeSetMap) {
        final GemstoneCoordsFinder plugin = GemstoneCoordsFinder.getInstance();

        final File dataFolder = plugin.getDataFolder();
        final File saveFolder = createSaveFolder(dataFolder);
        gemstoneTypeSetMap.forEach((gemstoneType, waypoints) -> {
            saveGemstone(saveFolder, gemstoneType, waypoints);
        });
        Bukkit.broadcast(Component.text("Saved Gemstones to %s".formatted(saveFolder.getPath())));
    }

    private static void saveGemstone(File saveFolder, GemstoneType gemstoneType, Set<Waypoint> waypoints) {
        final File file = new File(saveFolder, "%s.json".formatted(gemstoneType.name()));
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(waypoints, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File createSaveFolder(File dataFolder) {
        final File file = new File(dataFolder, "/Gemstones-%s".formatted(getCurrentTimeAsString()));
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static String getCurrentTimeAsString() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-HH-mm-ss");
        return currentDateTime.format(formatter);
    }

}
