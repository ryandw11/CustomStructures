package com.ryandw11.structure.structure.properties;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * This class represents the StructureLimitations configuration section of a structure.
 */
public class StructureLimitations {

    private int worldHeightRestriction;
    private int iterationLimit;
    private final List<String> whitelistSpawnBlocks;
    private final List<String> blacklistSpawnBlocks;
    private final BlockLevelLimit blockLevelLimit;
    private final Map<Material, Material> blockReplacement;
    private double replacementBlocksDelay;


    /**
     * Create structure limitations from a file.
     *
     * @param configuration The configuration to create from.
     */
    public StructureLimitations(FileConfiguration configuration) {
        if (!configuration.contains("StructureLimitations.IterationLimit"))
            iterationLimit = 2;
        else
            iterationLimit = configuration.getInt("StructureLimitations.IterationLimit");

        if (!configuration.contains("StructureLimitations.WorldHeightRestriction"))
            worldHeightRestriction = -1;
        else
            worldHeightRestriction = Math.max(0, configuration.getInt("StructureLimitations.WorldHeightRestriction"));

        if (!configuration.contains("StructureLimitations.WhitelistSpawnBlocks"))
            whitelistSpawnBlocks = new ArrayList<>();
        else
            whitelistSpawnBlocks = configuration.getStringList("StructureLimitations.WhitelistSpawnBlocks");

        if (!configuration.contains("StructureLimitations.BlacklistSpawnBlocks"))
            blacklistSpawnBlocks = new ArrayList<>();
        else
            blacklistSpawnBlocks = configuration.getStringList("StructureLimitations.BlacklistSpawnBlocks");

        this.blockLevelLimit = new BlockLevelLimit(configuration);

        replacementBlocksDelay = !configuration.contains("StructureLimitations.ReplaceBlockDelay") ? 0
                : configuration.getDouble("StructureLimitations.ReplaceBlockDelay");

        blockReplacement = new HashMap<>();
        if (configuration.contains("StructureLimitations.ReplaceBlocks")) {
            for (String s : Objects.requireNonNull(configuration.getConfigurationSection("StructureLimitations.ReplaceBlocks")).getKeys(false)) {
                Material firstMaterial = Material.getMaterial(s);
                Material secondMaterial = Material.getMaterial(Objects.requireNonNull(configuration.getString("StructureLimitations.ReplaceBlocks." + s)));
                blockReplacement.put(firstMaterial, secondMaterial);
            }
        }
    }

    /**
     * Create structure limitations without a config.
     *
     * @param whitelistSpawnBlocks The list of whitelisted spawn blocks.
     * @param blacklistSpawnBlocks The list of blacklisted spawn blocks.
     * @param blockLevelLimit      The block level limit.
     * @param blockReplacement     The block replacement map.
     */
    public StructureLimitations(List<String> whitelistSpawnBlocks, List<String> blacklistSpawnBlocks, BlockLevelLimit blockLevelLimit, Map<Material, Material> blockReplacement) {
        this.iterationLimit = 2;
        this.worldHeightRestriction = -1;
        this.whitelistSpawnBlocks = whitelistSpawnBlocks;
        this.blacklistSpawnBlocks = blacklistSpawnBlocks;
        this.blockLevelLimit = blockLevelLimit;
        this.blockReplacement = blockReplacement;
    }

    /**
     * Set the iteration limit for the structure.
     *
     * @param iterationLimit The iteration limit.
     */
    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    /**
     * Get the iteration limit for the structure.
     *
     * @return The iteration limit.
     */
    public int getIterationLimit() {
        return this.iterationLimit;
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
     * Get the blacklisted blocks.
     *
     * @return The blacklisted blocks.
     */
    public List<String> getBlacklistBlocks() {
        return blacklistSpawnBlocks;
    }

    /**
     * Check to see if the whitelist has a block.
     *
     * @param b The block to check
     * @return If the whitelist has the block. (Returns true if there is no whitelist)
     */
    public boolean hasWhitelistBlock(Block b) {
        if (whitelistSpawnBlocks.isEmpty()) return true;
        for (String block : whitelistSpawnBlocks) {
            if (block.equalsIgnoreCase(b.getType().toString()))
                return true;
        }
        return false;
    }

    /**
     * Check to see if the blacklist has a block.
     *
     * @param b The block to check.
     * @return If the blacklist has the block. (Returns false if there is no blacklist)
     */
    public boolean hasBlacklistBlock(Block b) {
        if (blacklistSpawnBlocks.isEmpty()) return false;
        for (String block : blacklistSpawnBlocks) {
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

    /**
     * Get the World Height Restriction.
     *
     * @return The world height restriction.
     */
    public int getWorldHeightRestriction() {
        return worldHeightRestriction;
    }

    /**
     * Set the World Height Restriction property.
     *
     * @param structureHeight The structure height.
     */
    public void setWorldHeightRestriction(int structureHeight) {
        this.worldHeightRestriction = Math.max(-1, structureHeight);
    }
}
