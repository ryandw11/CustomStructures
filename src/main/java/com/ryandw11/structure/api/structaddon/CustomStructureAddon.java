package com.ryandw11.structure.api.structaddon;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.loottables.ConfigLootItem;
import com.ryandw11.structure.loottables.LootTable;
import org.bukkit.plugin.Plugin;

import java.util.*;

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
    private final Set<StructureSectionProvider> providerSet = new HashSet<>();
    private final List<Class<? extends StructureSection>> structureSections;
    private final List<LootTable> lootTables;
    private final Map<String, Class<? extends ConfigLootItem>> lootItems;

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
        this.lootTables = new ArrayList<>();
        this.lootItems = new HashMap<>();
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
     * Register StructureSectionProvider to the addon.
     *
     * @param provider The provider to register.
     */
    public void registerStructureSectionProvider(StructureSectionProvider provider) {
        this.providerSet.add(provider);
    }

    /**
     * Unregister StructureSectionProvider from the addon.
     *
     * @param provider The provider to remove.
     */
    public void unregisterStructureSectionProvider(StructureSectionProvider provider) {
        this.providerSet.remove(provider);
    }

    /**
     * Get the list of structure sections.
     *
     * @return The list of structure sections.
     */
    public List<Class<? extends StructureSection>> getStructureSections() {
        return this.structureSections;
    }

    /**
     * Get all provider registered on this addon
     *
     * @return a set of providers.
     */
    public Set<StructureSectionProvider> getProviderSet() {
        return Collections.unmodifiableSet(this.providerSet);
    }

    /**
     * Register a structure sign to be used.
     * <p>This provides the same functionality as
     * {@link com.ryandw11.structure.schematic.StructureSignHandler#registerStructureSign(String, Class)}.</p>
     *
     * @param name          The name of the structure sign.
     * @param structureSign The class of the structure sign.
     * @return If the registration was successful. (False if a sign with that name already exists).
     */
    public boolean registerStructureSign(String name, Class<? extends StructureSign> structureSign) {
        return CustomStructures.getInstance().getStructureSignHandler().registerStructureSign(name, structureSign);
    }

    /**
     * Register a custom LootTable for use.
     *
     * @param lootTable The LootTable to be registered.
     */
    public void registerLootTable(LootTable lootTable) {
        CustomStructures.getInstance().getLootTableHandler().addLootTable(lootTable);
        this.lootTables.add(lootTable);
    }

    /**
     * Register a custom LootItem for use.
     *
     * @param typeName      The type name for the custom LootItem.
     * @param lootItemClass The class for the custom LootItem.
     */
    public void registerLootItem(String typeName, Class<? extends ConfigLootItem> lootItemClass) {
        CustomStructures.getInstance().getLootTableHandler().addLootItem(typeName, lootItemClass);
        this.lootItems.put(typeName, lootItemClass);
    }

    /**
     * Handles Re-registering items from the addon when the plugin is reloaded.
     *
     * <p>Internal Use Only.</p>
     */
    public void handlePluginReload() {
        for (LootTable lootTable : lootTables) {
            CustomStructures.getInstance().getLootTableHandler().addLootTable(lootTable);
        }

        for (Map.Entry<String, Class<? extends ConfigLootItem>> itemEntry : lootItems.entrySet()) {
            CustomStructures.getInstance().getLootTableHandler().addLootItem(itemEntry.getKey(), itemEntry.getValue());
        }
    }
}
