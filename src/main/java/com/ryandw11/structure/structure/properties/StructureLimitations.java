package com.ryandw11.structure.structure.properties;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * This class represents the StructureLimitations configuration section of a structure.
 */
public class StructureLimitations {

    private List<String> whitelistSpawnBlocks;
    private BlockLevelLimit blockLevelLimit;
    private Map<Material, Material> blockReplacement;
    private double replacementBlocksDelay;


    /**
     * Create structure limitations from a file.
     *
     * @param configuration The configuration to create from.
     */
    public StructureLimitations(FileConfiguration configuration) {
        if (!configuration.contains("StructureLimitations.whitelistSpawnBlocks"))
            whitelistSpawnBlocks = new ArrayList<>();
        else
            whitelistSpawnBlocks = configuration.getStringList("StructureLimitations.whitelistSpawnBlocks");

        this.blockLevelLimit = new BlockLevelLimit(configuration);

        replacementBlocksDelay = !configuration.contains("StructureLimitations.replacement_blocks_delay") ? 0
                : configuration.getDouble("StructureLimitations.replacement_blocks_delay");

        blockReplacement = new HashMap<>();
        if (configuration.contains("StructureLimitations.replacement_blocks")) {
            for (String s : Objects.requireNonNull(configuration.getConfigurationSection("StructureLimitations.replacement_blocks")).getKeys(false)) {
                Material firstMaterial = Material.getMaterial(s);
                Material secondMaterial = Material.getMaterial(Objects.requireNonNull(configuration.getString("StructureLimitations.replacement_blocks." + s)));
                blockReplacement.put(firstMaterial, secondMaterial);
            }
        }
    }

    /**
     * Create structure limitations without a config.
     *
     * @param whitelistSpawnBlocks The list of whitelisted spawn blocks.
     * @param blockLevelLimit      The block level limit.
     * @param blockReplacement     The block replacement map.
     */
    public StructureLimitations(List<String> whitelistSpawnBlocks, BlockLevelLimit blockLevelLimit, Map<Material, Material> blockReplacement) {
        this.whitelistSpawnBlocks = whitelistSpawnBlocks;
        this.blockLevelLimit = blockLevelLimit;
        this.blockReplacement = blockReplacement;
    }

    /**
     * Get the whitelisted blocks.
     *
     * @return The whitelisted blocks.
     */
    public List<String> getWhitelistBlocks() {
        return whitelistSpawnBlocks;
    }

    /**
     * Check to see if the whitelist has a block.
     *
     * @param b The block to check
     * @return If the whitelist has the block. (Returns true if there is not whitelist)
     */
    public boolean hasBlock(Block b) {
        if (whitelistSpawnBlocks.isEmpty()) return true;
        for (String block : whitelistSpawnBlocks) {
            if (block.equalsIgnoreCase(b.getType().toString()))
                return true;
        }
        return false;
    }

    /**
     * The block level limit of the structure.
     *
     * @return The block level limit.
     */
    public BlockLevelLimit getBlockLevelLimit() {
        return blockLevelLimit;
    }

    /**
     * Get the block replacement map.
     *
     * @return The block replacement map.
     */
    public Map<Material, Material> getBlockReplacement() {
        return blockReplacement;
    }

    /**
     * Get the replacement block delay.
     *
     * @return The replacement block delay.
     */
    public double getReplacementBlocksDelay() {
        return replacementBlocksDelay;
    }

    /**
     * Set the replacement block delay.
     *
     * @param value The replacement block delay.
     */
    public void setReplacementBlocksDelay(double value) {
        replacementBlocksDelay = value;
    }
}
