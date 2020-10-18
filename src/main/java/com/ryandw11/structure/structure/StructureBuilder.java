package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.properties.*;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * This class is used to make a brand new Structure. (This class is also used internally to load structures
 * from structure config files).
 * <p>You can create a structure completely via code or load a structure from a yaml file.</p>
 * <p>Example using a yaml file:</p>
 * <code>
 * StructureBuilder builder = new StructureBuilder("MyName", file);<br>
 * Structure struct = builder.build();<br>
 * </code>
 * <p>Example using code:</p>
 * <code>
 * StructureBuilder builder = new StructureBuilder("MyName", file);<br>
 * builder.setStructureLimitations(new StructureLimitations());<br>
 * ...<br>
 * Structure struct = builder.build();<br>
 * </code>
 */
public class StructureBuilder {

    private FileConfiguration config;

    protected String name;
    protected String schematic;
    protected int chanceNumber;
    protected int chanceOutOf;
    protected String compiledSchematic;
    protected boolean isCompiled = false;
    protected StructureLocation structureLocation;
    protected StructureProperties structureProperties;
    protected StructureLimitations structureLimitations;
    protected MaskProperty maskProperty;
    protected SubSchematics subSchematics;
    protected RandomCollection<LootTable> lootTables;

    private boolean invalid;

    /**
     * Build a structure using code.
     *
     * @param name      The name of the structure.
     * @param schematic The schematic of the structure.
     */
    public StructureBuilder(String name, String schematic) {
        this.name = name;
        this.schematic = schematic;
        lootTables = new RandomCollection<>();
    }


    /**
     * Build a structure using a yaml configuration file.
     * <p>No further editing of this class is required if you use this method.</p>
     * <p>Errors are outputted to the console. If an error occurs {@link #build()} will return null.</p>
     *
     * @param name The name of the structure.
     * @param file The file to read from.
     */
    public StructureBuilder(String name, File file) {
        if (!file.exists())
            throw new RuntimeException("Cannot build structure: That file does not exist!");
        config = YamlConfiguration.loadConfiguration(file);

        invalid = false;

        this.name = name;

        if (!checkValidity())
            return;

        schematic = config.getString("schematic");
        chanceNumber = config.getInt("Chance.Number");
        chanceOutOf = config.getInt("Chance.OutOf");

        if (config.contains("compiled_schematic")) {
            isCompiled = new File(CustomStructures.getInstance().getDataFolder() + "/schematics/" +
                    Objects.requireNonNull(config.getString("compiled_schematic"))).exists();
            if (!isCompiled)
                CustomStructures.getInstance().getLogger().severe("Invalid compiled schematic file for: " + config.getName());
            else
                compiledSchematic = config.getString("compiled_schematic");
        }

        structureLocation = new StructureLocation(this, config);
        structureProperties = new StructureProperties(config);
        structureLimitations = new StructureLimitations(config);
        maskProperty = new MaskProperty(config);
        subSchematics = new SubSchematics(config, CustomStructures.getInstance());
        lootTables = new RandomCollection<>();
        if (config.contains("LootTables")) {
            ConfigurationSection lootableConfig = config.getConfigurationSection("LootTables");
            assert lootableConfig != null;
            for (String lootTable : lootableConfig.getKeys(false)) {
                int weight = lootableConfig.getInt(lootTable);
                lootTables.add(weight, CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTable));
            }
        }
    }

    private boolean checkValidity() {
        if (!config.contains("schematic")) {
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Schematic is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }

        if (!config.contains("Chance.Number")) {
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Chance.Number is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }
        if (!config.contains("Chance.OutOf")) {
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Chance.OutOf is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }
        return true;
    }

    /**
     * Set the chance of the structure.
     *
     * @param number The number chance.
     * @param outOf  The out of chance.
     */
    public void setChance(int number, int outOf) {
        this.chanceNumber = number;
        this.chanceOutOf = outOf;
    }

    /**
     * Set the compiled schematic.
     * <p>This will automatically set isCompiled to true if the file is found.</p>
     *
     * @param cschem The compiled schematic name. (Include the .cschem)
     *               <p>This file MUST be in the schematics folder.</p>
     *               <p>An IllegalArgumentException is thrown when the file is not found.</p>
     */
    public void setCompiledSchematic(String cschem) {
        if (!new File(CustomStructures.getInstance().getDataFolder() + "/schematics/" + cschem).exists())
            throw new IllegalArgumentException("Compiled Schem File not found!");
        this.compiledSchematic = cschem;
        this.isCompiled = true;
    }

    /**
     * Set the structure limitations.
     *
     * @param limitations The structure limitations.
     */
    public void setStructureLimitations(StructureLimitations limitations) {
        this.structureLimitations = limitations;
    }

    /**
     * Set the structure properties.
     *
     * @param properties The structure properties.
     */
    public void setStructureProperties(StructureProperties properties) {
        this.structureProperties = properties;
    }

    /**
     * Set the structure location.
     *
     * @param location The structure location.
     */
    public void setStructureLocation(StructureLocation location) {
        this.structureLocation = location;
    }

    /**
     * Set the mask property.
     *
     * @param mask The mask property.
     */
    public void setMaskProperty(MaskProperty mask) {
        this.maskProperty = mask;
    }

    /**
     * Set the loot tables from a configuration section.
     *
     * @param lootableConfig The loot table configuration section.
     */
    public void setLootTables(ConfigurationSection lootableConfig) {
        lootTables = new RandomCollection<>();
        assert lootableConfig != null;
        for (String lootTable : lootableConfig.getKeys(false)) {
            int weight = lootableConfig.getInt(lootTable);
            lootTables.add(weight, CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTable));
        }
    }

    /**
     * Set the loot tables using a collection of LootTable.
     *
     * @param lootTables The collection of LootTables.
     */
    public void setLootTables(RandomCollection<LootTable> lootTables) {
        this.lootTables = lootTables;
    }

    /**
     * Set this structure to be invalid.
     */
    public void setInvalid() {
        invalid = true;
    }

    /**
     * Build the structure.
     * <p>Note: This does not check to see if all values are set. If any of the properties are not set
     * than a NullPointerException will occur.</p>
     *
     * @return The structure. (Null if the structure is invalid).
     */
    public Structure build() {
        if (invalid)
            return null;
        return new Structure(this);
    }

    /**
     * Save the structure as a structure configuration file.
     * <p>This automatically saves the file in the structures folder.</p>
     *
     * @param file The file to save.
     * @throws IOException If an IO Exception occurs.
     */
    public void save(File file) throws IOException {
        file.createNewFile();
        config = YamlConfiguration.loadConfiguration(file);
        config.set("schematic", schematic);
        config.set("Chance.Number", chanceNumber);
        config.set("Chance.OutOf", chanceOutOf);

        config.set("StructureLocation.Worlds", structureLocation.getWorlds());
        config.set("StructureLocation.SpawnY", structureLocation.getSpawnSettings().getValue());
        config.set("StructureLocation.Biome", structureLocation.getBiomes());

        config.set("StructureProperties.PlaceAir", structureProperties.canPlaceAir());
        config.set("StructureProperties.randomRotation", structureProperties.isRandomRotation());
        config.set("StructureProperties.ignorePlants", structureProperties.isIgnoringPlants());
        config.set("StructureProperties.spawnInWater", structureProperties.canSpawnInWater());
        config.set("StructureProperties.spawnInLavaLakes", structureProperties.canSpawnInLavaLakes());

        config.set("StructureLimitations.whitelistSpawnBlocks", structureLimitations.getWhitelistBlocks());

        if (isCompiled)
            config.set("compiled_schematic", compiledSchematic);

        for (Map.Entry<Double, LootTable> entry : lootTables.getMap().entrySet()) {
            config.set("LootTables." + entry.getValue().getName(), entry.getKey());
        }
        config.save(file);
    }

}
