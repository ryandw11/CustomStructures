package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the BottomSpaceFill configuration section.
 *
 * <p>This option allows for the structure to have blocks placed under it when a portion is floating.</p>
 */
public class BottomSpaceFill {
    private final Map<Biome, Material> blockMap;
    private Material defaultMaterial;
    private boolean enabled = false;

    /**
     * Used by the StructureBuilder when there is a structure configuration file.
     *
     * @param configuration The file configuration for the structure config.
     */
    public BottomSpaceFill(FileConfiguration configuration) {
        blockMap = new HashMap<>();

        if (!configuration.contains("BottomSpaceFill")) {
            return;
        }

        ConfigurationSection fillSection = Objects.requireNonNull(configuration.getConfigurationSection("BottomSpaceFill"));

        enabled = true;

        for (String keyGroup : fillSection.getKeys(false)) {
            String[] keys = keyGroup.split(",");
            Material fillMaterial;
            try {
                fillMaterial = Material.valueOf(Objects.requireNonNull(fillSection.getString(keyGroup)).toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new StructureConfigurationException("Unknown fill material " + fillSection.getString(keyGroup) + " in BottomSpaceFill configuration section.");
            }

            for (String key : keys) {
                if (key.equalsIgnoreCase("default")) {
                    defaultMaterial = fillMaterial;
                    continue;
                }

                Biome biome;
                try {
                    biome = Biome.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new StructureConfigurationException("Unknown biome " + key + " in BottomSpaceFill configuration section.");
                }
                blockMap.put(biome, fillMaterial);
            }
        }
    }

    /**
     * Construct a bottom space fill configuration that is disabled.
     *
     * <p>You should use this if you don't want your structures to have bottom filling.</p>
     */
    public BottomSpaceFill() {
        enabled = false;
        this.blockMap = new HashMap<>();
    }

    /**
     * Construct a bottom space fill configuration with the desired value.
     *
     * @param defaultMaterial The default material to be used if a biome is not specified. (Null if you don't want any).
     * @param blockFillMap    The block fill map for the structure.
     */
    public BottomSpaceFill(@Nullable Material defaultMaterial, @NotNull Map<Biome, Material> blockFillMap) {
        enabled = true;
        this.defaultMaterial = defaultMaterial;
        this.blockMap = Objects.requireNonNull(blockFillMap);
    }

    /**
     * Check if the structure has bottom space filling enabled.
     *
     * @return If the structure has bottom space filling enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the fill material for any biome not explicitly stated. (If null, then no fill is performed for non-specified biomes).
     *
     * @param material The material to use for the fill.
     */
    public void setDefaultFillMaterial(@Nullable Material material) {
        this.defaultMaterial = material;
    }

    /**
     * The fill material for any biome not explicitly stated. (If none, then no fill is performed for non-specified biomes).
     *
     * @return The fill material for any biome not explicitly stated.
     */
    public Optional<Material> getDefaultFillMaterial() {
        return Optional.ofNullable(defaultMaterial);
    }

    /**
     * Get the fill block map.
     *
     * @return The fill block map.
     */
    @NotNull
    public Map<Biome, Material> getFillBlockMap() {
        return this.blockMap;
    }

    /**
     * Get the fill material for a specific biome.
     *
     * @param biome The biome to get the fill material for.
     * @return An optional containing the Material. (If none, then no fill exists for that biome).
     */
    public Optional<Material> getFillMaterial(Biome biome) {
        if (blockMap.containsKey(biome))
            return Optional.of(blockMap.get(biome));
        return Optional.ofNullable(defaultMaterial);
    }
}
