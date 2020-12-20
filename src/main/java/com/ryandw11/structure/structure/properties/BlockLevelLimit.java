package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles the block level limit of the StructureLimitations property.
 * <p>
 * This setting can have multiple modes. The following are valid modes:
 * <code>
 * flat, flat_error
 * </code>
 * flat - The ground must not be air in the cube region while the blocks above the ground must be air or plants.
 * <p>
 * flat_error - Same as flat; however, a certain error is acceptable. So if the error is set to 0.33 than it has a 1/3
 * error allowance. Error is calculated by (error_blocks/total_block) if (total_error > allowed_error) than the structure does
 * not spawn. See logic in {@link com.ryandw11.structure.utils.StructurePicker} for more details.
 */
public class BlockLevelLimit {
    private String mode;

    private int x1, z1, x2, z2;

    private double error = -1;

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

        ConfigurationSection cs = fileConfiguration.getConfigurationSection("StructureLimitations.BlockLevelLimit");

        assert cs != null;
        this.mode = cs.getString("mode");
        this.x1 = cs.getInt("cornerOne.x");
        this.z1 = cs.getInt("cornerOne.z");
        this.x2 = cs.getInt("cornerTwo.x");
        this.z2 = cs.getInt("cornerTwo.z");

        if (cs.contains("error")) {
            error = cs.getDouble("error");
            if(error < 0 || error > 1)
                throw new StructureConfigurationException("`BlockLevelLimit.error` must be greater than 0 and less than 1.");
        }

        assert mode != null;
        if(mode.equalsIgnoreCase("flat_error") && !cs.contains("error")){
            throw new StructureConfigurationException("The BlockLevelLimit mode `flat_error` must contain an error setting!");
        }
    }

    /**
     * If Block Level Limit is enabled.
     *
     * @return If the block level limit is enabled.
     */
    public boolean isEnabled() {
        return !mode.equalsIgnoreCase("none");
    }

    /**
     * Get the error of the level limit.
     * <p>The error configuration is only used by the flat_error mode.
     * This value will be -1 for all other modes.</p>
     *
     * @return The error of the level limit.
     */
    public double getError() {
        return error;
    }

    /**
     * Set the error for the level limit.
     * <p>This is only used by the flat_error mode.</p>
     *
     * @param error The error of the level limit.
     *              <p>This is a percent is decimal form. (0-1) only.</p>
     */
    public void setError(double error) {
        if (error < 0 || error > 1)
            throw new IllegalArgumentException("Error value must be > 0 and < 1!");
        this.error = error;
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
