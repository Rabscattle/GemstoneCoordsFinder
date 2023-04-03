package com.github.rabscattle.gemstonecoordsfinder;

import com.github.rabscattle.gemstonecoordsfinder.commands.StartScanCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GemstoneCoordsFinder extends JavaPlugin {

    private static GemstoneCoordsFinder instance;

    public static GemstoneCoordsFinder getInstance() {
        return instance;
    }


    private boolean debugMode;
    private int blocksPerSecond;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        blocksPerSecond = getConfig().getInt("blocks-per-second", 30_000);
        debugMode = getConfig().getBoolean("debug-enabled", false);

        getCommand("start-scan").setExecutor(new StartScanCommand());

    }

    public int getBlocksPerSecond() {
        return blocksPerSecond;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
