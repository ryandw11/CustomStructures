package com.ryandw11.structure.structure.properties;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles the block level limit of the StructureLimitations property.
 */
public class BlockLevelLimit {
    private String mode;

    private int x1, z1, x2, z2;

    /**
     * Create a block level limit.
     *
     * @param mode The mode.
     * @param x1   The first x.
     * @param z1   The first z.
     * @param x2   The second x.
     * @param z2   The second z.
     */
    public BlockLevelLimit(String mode, int x1, int z1, int x2, int z2) {
        this.mode = mode;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    /**
     * Create a BlockLevelLimit that has no mode.
     */
    public BlockLevelLimit() {
        mode = "NONE";
    }

    /**
     * Create the BlockLevelLimit from a config file.
     *
     * @param fileConfiguration The configuration file.
     */
    public BlockLevelLimit(FileConfiguration fileConfiguration) {
        if (!fileConfiguration.contains("StructureLimitations.BlockLevelLimit")) {
            mode = "NONE";
            return;
        }

        this.mode = fileConfiguration.getString("StructureLimitations.BlockLevelLimit.mode");
        this.x1 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerOne.x");
        this.z1 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerOne.z");
        this.x2 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerTwo.x");
        this.z2 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerTwo.z");
    }

    /**
     * If Block Level Limit is enabled.
     *
     * @return
     */
    public boolean isEnabled() {
        return !mode.equalsIgnoreCase("none");
    }

    /**
     * Get the mode of the block level limit.
     *
     * @return The mode.
     */
    public String getMode() {
        return mode;
    }

    /**
     * Get the first x.
     *
     * @return The first x.
     */
    public int getX1() {
        return x1;
    }

    /**
     * Get the second x.
     *
     * @return The second x.
     */
    public int getX2() {
        return x2;
    }

    /**
     * Get the first z.
     *
     * @return The first z.
     */
    public int getZ1() {
        return z1;
    }

    /**
     * Get the second z.
     *
     * @return The second z.
     */
    public int getZ2() {
        return z2;
    }
}
