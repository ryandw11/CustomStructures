package com.ryandw11.structure.api.structaddon;

import com.ryandw11.structure.CustomStructures;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for extending CustomStructure functionality.
 *
 * <p>Note: This class is not required to use events, but may be useful for your users to know that your
 * plugin interfaces with CustomStructures.</p>
 *
 * <p>Register this addon with the plugin by using {@link com.ryandw11.structure.api.CustomStructuresAPI#registerCustomAddon(CustomStructureAddon)}.</p>
 *
 * @since 1.5.8
 */
public final class CustomStructureAddon {
    private final String name;
    private final List<String> authors;
    private final List<Class<? extends StructureSection>> structureSections;

    /**
     * Create an addon for custom structures.
     * <p>Note: This is not required to use events, but may be useful for your users to know that your
     * plugin interfaces with CustomStructures.</p>
     *
     * @param plugin Your plugin.
     */
    public CustomStructureAddon(Plugin plugin) {
        if (plugin == CustomStructures.getInstance())
            throw new IllegalArgumentException("Cannot add CustomStructures as an addon.");

        this.name = plugin.getName();
        if (name.equalsIgnoreCase("CustomStructures") || name.equalsIgnoreCase("CustomStructure"))
            throw new IllegalArgumentException("Addon name cannot be the same as the plugin.");

        this.structureSections = new ArrayList<>();
        this.authors = plugin.getDescription().getAuthors();
    }

    /**
     * Get the name of the addon.
     * <p>This is automatically taken from the plugin's name.</p>
     *
     * @return The name of the addon.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the authors of the addon.
     *
     * @return The authors of the addon.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Add structure sections to the addon.
     *
     * @param structureSection The structure section to add.
     */
    public void addStructureSection(Class<? extends StructureSection> structureSection) {
        this.structureSections.add(structureSection);
    }

    /**
     * Get the list of structure sections.
     *
     * @return The list of structure sections.
     */
    public List<Class<? extends StructureSection>> getStructureSections() {
        return this.structureSections;
    }
}
