package com.ryandw11.structure.loottables;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * The type of a LootTable.
 */
public enum LootTableType {
    CHEST(Material.CHEST),
    FURNACE(Material.FURNACE),
    HOPPER(Material.HOPPER),
    BREWING_STAND(Material.BREWING_STAND),
    BARREL(Material.BARREL),
    TRAPPED_CHEST(Material.TRAPPED_CHEST),
    DROPPER(Material.DROPPER),
    DISPENSER(Material.DISPENSER),
    BLAST_FURNACE("BLAST_FURNACE"),
    SMOKER("SMOKER");

    private Material material;

    /**
     * This is for materials that might not exist in older versions of minecraft.
     *
     * @param materialName The material name.
     */
    LootTableType(String materialName) {
        try {
            this.material = Material.valueOf(materialName);
        } catch (IllegalArgumentException ex) {
            this.material = Material.CHEST;
        }
    }

    LootTableType(Material material) {
        this.material = material;
    }

    /**
     * Get the material equivalent.
     *
     * <p>Note: If the block does not exist in the current version of Minecraft,
     * than a CHEST is returned by default.</p>
     *
     * @return The material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Take a string and get a list of loot table types.
     *
     * @param value The string for the loot table list.
     * @return The list of the loot tables.
     */
    public static List<LootTableType> valueOfList(String value) {
        String[] splitList = value.split(",");
        List<LootTableType> output = new ArrayList<>();
        for (String s : splitList) {
            output.add(valueOf(s));
        }
        return output;
    }

    /**
     * Take a material and get the loot table type.
     *
     * @param material The material.
     * @return The loot table type.
     */
    public static LootTableType valueOf(Material material) {
        for (LootTableType type : values()) {
            if (material == type.getMaterial())
                return type;
        }
        return null;
    }

    /**
     * Check to see if a name exists in the enum.
     *
     * @param value The value.
     * @return If the enum name exists.
     */
    public static boolean exists(String value) {
        for (LootTableType s : values()) {
            if (s.toString().equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

}
