package com.github.domdelion.gemstonecoordsfinder.commands;

import com.github.domdelion.gemstonecoordsfinder.structs.GemstoneScanner;
import com.github.domdelion.gemstonecoordsfinder.structs.GemstoneType;
import com.github.domdelion.gemstonecoordsfinder.utils.Storage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;

public class StartScanCommand implements CommandExecutor {
    private GemstoneScanner scanner;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            return false;
        }

        String worldString = args[0];
        final World world = Bukkit.getWorld(worldString);
        if (world == null) {
            sender.sendMessage("World could not be found");
            return false;
        }

        if (scanner == null) {
            scanner = new GemstoneScanner(world, gemstoneTypeSetMap -> {
                this.scanner = null;
                Bukkit.broadcast(Component.text("Gemstone Scan Complete. Result: "));
                Storage.saveGemstone(gemstoneTypeSetMap);
                for (GemstoneType value : GemstoneType.values()) {
                    Bukkit.broadcast(Component.text("%s: %d".formatted(value.name(), gemstoneTypeSetMap.getOrDefault(value, new HashSet<>()).size())));
                }
            });
            scanner.startScan();
            return true;
        }

        if (scanner.isInProgress()) {
            sender.sendMessage("Scanner is already running");
            return true;
        }
        return true;
    }
}
