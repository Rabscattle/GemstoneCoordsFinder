package com.github.rabscattle.gemstonecoordsfinder.structs;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum GemstoneType {
    RUBY(Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE),
    AMETHYST(Material.PURPLE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS_PANE),
    JADE(Material.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS_PANE),
    SAPPHIRE(Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS_PANE),
    AMBER(Material.ORANGE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS_PANE),
    TOPAZ(Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE),
    JASPER(Material.PINK_STAINED_GLASS, Material.PINK_STAINED_GLASS_PANE),
    OPAL(Material.WHITE_STAINED_GLASS, Material.WHITE_STAINED_GLASS_PANE);

    private final Set<Material> materials;

    GemstoneType(Material... materials) {
        this.materials = new HashSet<>(Arrays.asList(materials));
    }

    public static GemstoneType ofMaterial(Material input) {
        for (GemstoneType value : values()) {
            if (value.materials.contains(input))
                return value;
        }

        return null;
    }
}
