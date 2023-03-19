package com.ryandw11.structure;

import com.ryandw11.structure.api.structaddon.CustomStructureAddon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for handling addons to the plugin.
 *
 * <p>Use {@link com.ryandw11.structure.api.CustomStructuresAPI#registerCustomAddon(CustomStructureAddon)} to
 * register an addon.</p>
 */
public final class AddonHandler {
    private final List<CustomStructureAddon> addons;

    /**
     * <p>Internal use only. Access the existing instance of this class from {@link CustomStructures#getAddonHandler()}.</p>
     */
    protected AddonHandler() {
        this.addons = new ArrayList<>();
    }

    /**
     * Get the list of addons.
     *
     * @return The list of addons.
     */
    public List<CustomStructureAddon> getCustomStructureAddons() {
        return Collections.unmodifiableList(addons);
    }

    /**
     * Register a custom addon.
     *
     * @param addon The addon to register.
     */
    public void registerAddon(CustomStructureAddon addon) {
        if (addons.contains(addon))
            throw new IllegalArgumentException("Addon is already registered!");
        addons.add(addon);
    }

    /**
     * Handles re-registering items from addons when the plugin is reloaded.
     *
     * <p>Internal Use Only.</p>
     */
    public void handlePluginReload() {
        for (CustomStructureAddon addon : addons) {
            addon.handlePluginReload();
        }
    }

}
