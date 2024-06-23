package com.ryandw11.structure.ignoreblocks;

import org.bukkit.Material;

import java.util.List;

/**
 * Ignore blocks for 1.20, 1.21
 */
// TODO: Maybe use built in list defined by data packs? Or have the option
public class IgnoreBlocks_1_20 implements IgnoreBlocks {

    private final List<Material> ignoreBlocks = List.of(
            // General BLocks
            Material.SNOW,
            // Grasses
            Material.GRASS,
            Material.TALL_GRASS,
            Material.FERN,
            Material.LARGE_FERN,
            Material.DEAD_BUSH,
            Material.TALL_SEAGRASS,
            Material.SEAGRASS,
            Material.KELP_PLANT,
            Material.SEA_PICKLE,
            Material.CRIMSON_ROOTS,
            Material.WARPED_ROOTS,
            Material.NETHER_SPROUTS,
            Material.WEEPING_VINES_PLANT,
            Material.TWISTING_VINES_PLANT,
            Material.BAMBOO,
            Material.CACTUS,
            // Tree Items
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK,
            Material.MUSHROOM_STEM,
            Material.VINE,
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.SHROOMLIGHT,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            // Flowers
            Material.CORNFLOWER,
            Material.SUNFLOWER,
            Material.POPPY,
            Material.DANDELION,
            Material.ROSE_BUSH,
            Material.WITHER_ROSE,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.LILY_OF_THE_VALLEY,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            // Coral
            Material.TUBE_CORAL,
            Material.BRAIN_CORAL,
            Material.BUBBLE_CORAL,
            Material.FIRE_CORAL,
            Material.HORN_CORAL,
            Material.TUBE_CORAL_FAN,
            Material.BRAIN_CORAL_FAN,
            Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN,
            Material.HORN_CORAL_FAN,
            Material.TUBE_CORAL_WALL_FAN,
            Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN,
            Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            // New 1.17 Materials
            Material.GLOW_LICHEN,
            Material.MOSS_CARPET,
            Material.SMALL_DRIPLEAF,
            Material.BIG_DRIPLEAF,
            Material.BIG_DRIPLEAF_STEM,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.FLOWERING_AZALEA,
            Material.AZALEA,
            Material.SPORE_BLOSSOM,
            Material.HANGING_ROOTS,
            Material.POINTED_DRIPSTONE,
            Material.SMALL_AMETHYST_BUD,
            Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD,
            Material.AMETHYST_CLUSTER,
            // New 1.19 Materials
            Material.MANGROVE_ROOTS,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.MANGROVE_LOG,
            Material.MANGROVE_LEAVES,
            Material.MANGROVE_PROPAGULE,
            Material.SCULK_VEIN,
            // New 1.20 Materials
            Material.CHERRY_LOG,
            Material.CHERRY_LEAVES,
            Material.CHERRY_SAPLING,
            Material.TORCHFLOWER,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP,
            Material.PITCHER_PLANT

    );

    @Override
    public List<Material> getBlocks() {
        return ignoreBlocks;
    }
}
