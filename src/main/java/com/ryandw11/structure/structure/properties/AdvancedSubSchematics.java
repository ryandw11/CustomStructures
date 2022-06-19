package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.properties.schematics.SubSchematic;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The advanced SubSchematic feature.
 */
public class AdvancedSubSchematics {

    private boolean enabled;
    private final Map<String, RandomCollection<SubSchematic>> schematicCategories;

    /**
     * Get the sub schematics from a configuration file.
     *
     * @param configuration The configuration file.
     * @param plugin        The instance of custom structures. (CustomStructures.getInstance()).
     */
    public AdvancedSubSchematics(@NotNull FileConfiguration configuration, @NotNull CustomStructures plugin) {
        schematicCategories = new HashMap<>();
        if (!configuration.contains("AdvancedSubSchematics")) {
            enabled = false;
            return;
        }

        ConfigurationSection section = configuration.getConfigurationSection("AdvancedSubSchematics");
        assert section != null;

        for (String category : section.getKeys(false)) {
            RandomCollection<SubSchematic> schematics = new RandomCollection<>();
            try {
                for (String schemName : Objects.requireNonNull(section.getConfigurationSection(category)).getKeys(false)) {
                    SubSchematic schem = new SubSchematic(
                            Objects.requireNonNull(section.getConfigurationSection(String.format("%s.%s", category, schemName))),
                            true);
                    schematics.add(schem.getWeight(), schem);
                }
            } catch (RuntimeException ex) {
                enabled = false;
                plugin.getLogger().warning("Unable to enable AdvancedSubStructures on structure " + configuration.getName() + ".");
                plugin.getLogger().warning("The following error occurred:");
                plugin.getLogger().warning(ex.getMessage());
                if (plugin.isDebug())
                    ex.printStackTrace();
            }
            schematicCategories.put(category, schematics);
        }
        enabled = true;
    }

    /**
     * Construct the AdvancedSubSchematic feature programmatically.
     *
     * @param enabled If this feature should be enabled.
     */
    public AdvancedSubSchematics(boolean enabled) {
        this.enabled = enabled;
        this.schematicCategories = new HashMap<>();
    }

    /**
     * If sub schematics are enabled.
     *
     * @return If sub schematics are enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Only enable this via code if you are certain the formatting is right with the schematic list.
     * The plugin does no additional checks after the construction of the object.
     *
     * @param enabled If you want the feature to be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the list of sub schematics.
     *
     * @return The list of sub schematics.
     */
    public Map<String, RandomCollection<SubSchematic>> getSchematicCategories() {
        return schematicCategories;
    }

    /**
     * Get a category.
     *
     * @param name The name of the category to get.
     * @return The category.
     */
    public RandomCollection<SubSchematic> getCategory(String name) {
        return schematicCategories.get(name);
    }

    /**
     * Check if a category exists.
     *
     * @param name The name of the category to check.
     * @return If the category exists.
     */
    public boolean containsCategory(String name) {
        return schematicCategories.containsKey(name);
    }

    /**
     * Get a list with the names of every category.
     *
     * @return The list with the names of every category.
     */
    public List<String> getCategoryNames() {
        return new ArrayList<>(schematicCategories.keySet());
    }

    /**
     * Add a category to the advanced sub schematic section.
     *
     * @param name       The name of the category to add.
     * @param schematics The random collection of sub-schematics.
     */
    public void addCategory(String name, RandomCollection<SubSchematic> schematics) {
        schematicCategories.put(name, schematics);
    }

    /**
     * Add a sub-schematic to a category.
     *
     * @param categoryName The category the sub-schematic should belong to.
     * @param subSchematic The sub-schematic with weight specified.
     * @throws IllegalArgumentException If the specified category does not exist or the sub-schematic weight is zero.
     */
    public void addSchematicToCategory(String categoryName, SubSchematic subSchematic) {
        if (!schematicCategories.containsKey(categoryName))
            throw new IllegalArgumentException("Category does not exist.");
        if (subSchematic.getWeight() == 0)
            throw new IllegalArgumentException("Sub-Schematic weight cannot be zero.");
        schematicCategories.get(categoryName).add(subSchematic.getWeight(), subSchematic);
    }


}
