package com.ryandw11.structure.structure.properties.schematics;

import org.bukkit.configuration.ConfigurationSection;

/**
 * This class holds a single SubSchematic.
 */
public class SubSchematic {

    private String file;
    private boolean placeAir = false;

    /**
     * Create a sub schematic from a configuration section.
     *
     * @param section The configuration section.
     */
    public SubSchematic(ConfigurationSection section) {
        if (!section.contains("file"))
            throw new RuntimeException("Format Error: " + section.getName() + " does not contain a file!");
        file = section.getString("file");
        if (section.contains("PlaceAir"))
            placeAir = section.getBoolean("PlaceAir");
    }

    /**
     * Create a sub schematic.
     *
     * @param file     The file.
     *                 <p>Note: The plugin does not check to make sure this file is valid!</p>
     * @param placeAir If the structure should place air.
     */
    public SubSchematic(String file, boolean placeAir) {
        this.file = file;
        this.placeAir = placeAir;
    }

    /**
     * Set if the sub schematic should place air.
     *
     * @param placeAir If the sub schematic should place air.
     */
    public void setPlaceAir(boolean placeAir) {
        this.placeAir = placeAir;
    }

    /**
     * Get if the sub schematic should place air.
     *
     * @return If the the sub schematic should place air.
     */
    public boolean isPlacingAir() {
        return placeAir;
    }

    /**
     * Set the file of the sub schematic.
     *
     * @param file The file of the sub schematic.
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Get the file of the sub schematic.
     *
     * @return THe file of the sub schematic.
     */
    public String getFile() {
        return file;
    }
}
