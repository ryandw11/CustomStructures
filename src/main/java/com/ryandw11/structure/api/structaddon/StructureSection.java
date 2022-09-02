package com.ryandw11.structure.api.structaddon;

import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.StructureProperty;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * This interface is used to add custom configuration sections to the plugin.
 *
 * <p>StructureSections require a {@link CustomStructureAddon} class to be registered with
 * the API using the {@link com.ryandw11.structure.api.CustomStructuresAPI#registerCustomAddon(CustomStructureAddon)} method.</p>
 *
 * <p>Note: Your implementation must have a public default constructor with no parameters. That default
 * constructor should initialize your section with the default values. A second constructor
 * for developers using your custom section is optional.</p>
 *
 * <p>The {@link #getName()} method must have a YAML friendly name. Ensure that your name is unique and does not
 * interfere with any other sections. It is recommended that you do the following naming convention to make it easy for
 * users:</p>
 * <code>{AddonName}MyCoolSection</code>
 */
public interface StructureSection extends StructureProperty {
    /**
     * This is the name of your custom section.
     *
     * @return The name of your custom section.
     */
    String getName();

    /**
     * This method is called when a structure is loaded from the plugin's structure folder.
     * <p>This method is not called when another plugin adds your section via the {@link com.ryandw11.structure.structure.StructureBuilder}.
     * So it is important that your default constructor initializes your section with the default values.</p>
     *
     * @param configurationSection The configuration section. (This is null if the section does not exist in the structure
     *                             configuration file).
     */
    void setupSection(@Nullable ConfigurationSection configurationSection);

    /**
     * This method informs the plugin if the structure can spawn.
     *
     * @param structure  The structure that is being spawned. (Note: it is good practice to not rely on other
     *                   data from the structure to make a decision.)
     * @param spawnBlock The spawn block.
     * @param chunk      The spawn chunk.
     * @return If the structure can spawn. (True means it can, false means it can't).
     */
    boolean checkStructureConditions(Structure structure, Block spawnBlock, Chunk chunk);
}
