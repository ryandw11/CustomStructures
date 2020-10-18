package com.ryandw11.structure.structure.properties;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles the masks.
 */
public class MaskProperty {
    private List<Mask> masks;
    private MaskUnion unionType;

    public MaskProperty(FileConfiguration configuration) {
        masks = new ArrayList<>();

        if (!configuration.contains("Masks"))
            return;

        ConfigurationSection cs = configuration.getConfigurationSection("Masks");
        assert cs != null;

        if (!cs.contains("enabled") || !cs.getBoolean("enabled"))
            return;
        if (cs.contains("union_type")) {
            unionType = MaskUnion.valueOf(Objects.requireNonNull(cs.getString("union_type")).toUpperCase());
        } else
            unionType = MaskUnion.AND;

        blockTypeMask(cs);
        negateBlockTypeMask(cs);
    }

    /**
     * Get the union type.
     *
     * @return The union type.
     */
    public MaskUnion getUnionType() {
        return unionType;
    }

    /**
     * Set the union type.
     *
     * @param type The union type.
     */
    public void setUnionType(MaskUnion type) {
        this.unionType = type;
    }

    /**
     * Add a mask.
     * <p>These masks are from the WorldEdit API.</p>
     *
     * @param mask The mask to add.
     */
    public void addMask(Mask mask) {
        masks.add(mask);
    }

    /**
     * Get the list of masks
     * <p>Note: The extents of the masks are mutated by the plugin.</p>
     *
     * @return The list of masks.
     */
    public List<Mask> getMasks() {
        return masks;
    }

    /**
     * Get the list of masks with a certain extent.
     * <p>Normal the Clipboard extent is used.</p>
     *
     * @param extent The extent to use.
     * @return The list of masks.
     */
    public List<Mask> getMasks(Extent extent) {
        List<Mask> output = new ArrayList<>(getMasks());
        for (Mask mask : output) {
            ((AbstractExtentMask) mask).setExtent(extent);
        }

        return output;
    }

    private void blockTypeMask(ConfigurationSection cs) {
        if (!cs.contains("BlockTypeMask")) return;
        List<BlockType> blockTypes = new ArrayList<>();
        List<String> blockTypeStrings = cs.getStringList("BlockTypeMask");
        for (String s : blockTypeStrings) {
            blockTypes.add(BlockType.REGISTRY.get(s.toLowerCase()));
        }
        BlockTypeMask blockTypeMask = new BlockTypeMask(new NullExtent(), blockTypes);
        addMask(blockTypeMask);
    }

    private void negateBlockTypeMask(ConfigurationSection cs) {
        if (!cs.contains("NegatedBlockMask")) return;
        List<BlockType> blockTypes = new ArrayList<>(BlockType.REGISTRY.values());
        List<String> blockTypeStrings = cs.getStringList("NegatedBlockMask");
        for (String s : blockTypeStrings) {
            blockTypes.remove(BlockType.REGISTRY.get(s.toLowerCase()));
        }

        BlockTypeMask blockTypeMask = new BlockTypeMask(new NullExtent(), blockTypes);
        addMask(blockTypeMask);
    }

    /**
     * Contains the two options for masks.
     * AND operates as a logical AND
     * while OR operates as a logical OR.
     * <p>See the world edit documentation for more information.</p>
     */
    public enum MaskUnion {
        AND, OR
    }
}
