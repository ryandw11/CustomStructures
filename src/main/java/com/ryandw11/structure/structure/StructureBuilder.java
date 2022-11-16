package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.CustomStructureAddon;
import com.ryandw11.structure.api.structaddon.StructureSection;
import com.ryandw11.structure.api.structaddon.StructureSectionProvider;
import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.structure.properties.*;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    private final CustomStructures plugin;

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
    protected AdvancedSubSchematics advancedSubSchematics;
    protected BottomSpaceFill bottomSpaceFill;
    protected Map<LootTableType, RandomCollection<LootTable>> lootTables;
    protected List<StructureSection> structureSections;
    // Base Rotation in Radians.
    protected double baseRotation;

    /**
     * Build a structure using code.
     *
     * @param name      The name of the structure.
     * @param schematic The schematic of the structure.
     */
    public StructureBuilder(String name, String schematic) {
        this(name, schematic, new ArrayList<>());
    }

    /**
     * Build a structure.
     *
     * @param name      The name of the structure.
     * @param schematic The location of the structure schematic file.
     * @param sections  The list of structure sections.
     */
    public StructureBuilder(String name, String schematic, List<StructureSection> sections) {
        this.plugin = CustomStructures.getInstance();
        this.name = name;
        this.schematic = schematic;
        this.baseRotation = 0;
        lootTables = new HashMap<>();
        this.structureSections = sections;
    }

    /**
     * Build a structure.
     *
     * @param name      The name of the structure.
     * @param schematic The location of the structure schematic file.
     * @param sections  The structure sections to add.
     */
    public StructureBuilder(String name, String schematic, StructureSection... sections) {
        this.plugin = CustomStructures.getInstance();
        this.name = name;
        this.schematic = schematic;
        this.baseRotation = 0;
        lootTables = new HashMap<>();
        this.structureSections = Arrays.asList(sections);
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

        plugin = CustomStructures.getInstance();

        this.name = name;
        this.structureSections = new ArrayList<>();

        checkValidity();

        schematic = config.getString("schematic");
        chanceNumber = config.getInt("Chance.Number");
        chanceOutOf = config.getInt("Chance.OutOf");
        baseRotation = 0;

        if (config.contains("compiled_schematic")) {
            isCompiled = new File(CustomStructures.getInstance().getDataFolder() + "/schematics/" +
                    Objects.requireNonNull(config.getString("compiled_schematic"))).exists();
            if (!isCompiled)
                CustomStructures.getInstance().getLogger().severe("Invalid compiled schematic file for: " + name);
            else
                compiledSchematic = config.getString("compiled_schematic");
        }

        structureLocation = new StructureLocation(this, config);
        structureProperties = new StructureProperties(config);
        structureLimitations = new StructureLimitations(config);
        maskProperty = new MaskProperty(config);
        subSchematics = new SubSchematics(config, CustomStructures.getInstance());
        advancedSubSchematics = new AdvancedSubSchematics(config, CustomStructures.getInstance());
        bottomSpaceFill = new BottomSpaceFill(config);

        lootTables = new HashMap<>();
        if (config.contains("LootTables")) {
            ConfigurationSection lootableConfig = config.getConfigurationSection("LootTables");
            assert lootableConfig != null;
            for (String lootTable : lootableConfig.getKeys(false)) {
                if (!LootTableType.exists(lootTable))
                    continue;
                LootTableType type = LootTableType.valueOf(lootTable.toUpperCase());
                // Loop through the new loot table section.
                for (String lootTableName : Objects.requireNonNull(lootableConfig.getConfigurationSection(lootTable)).getKeys(false)) {
                    int weight = lootableConfig.getInt(lootTable + "." + lootTableName);
                    LootTable table = CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTableName);
                    table.addType(type);
                    if (lootTables.containsKey(type))
                        lootTables.get(type).add(weight, table);
                    else {
                        lootTables.put(type, new RandomCollection<>());
                        lootTables.get(type).add(weight, table);
                    }
                }
            }
        }

        // Go through and setup the sections for the addons.
        for (CustomStructureAddon addon : CustomStructures.getInstance().getAddonHandler().getCustomStructureAddons()) {
            for (StructureSectionProvider provider : addon.getProviderSet()) {
                try {
                    StructureSection section = provider.createSection();
                    if (!config.contains(section.getName())) {
                        section.setupSection(null);
                    } else {
                        section.setupSection(config.getConfigurationSection(section.getName()));
                    }
                    this.structureSections.add(section);
                } catch (StructureConfigurationException ex) {
                    // Handle the structureConfigurationException.
                    throw new StructureConfigurationException(String.format("[%s Addon] %s. This is not" +
                            "an issue with the CustomStructures plugin.", addon.getName(), ex.getMessage()));
                } catch (Throwable ex) {
                    // Inform the user of errors.
                    plugin.getLogger().severe(String.format("An error was encountered in the %s addon! Enable debug for more information.", addon.getName()));
                    plugin.getLogger().severe(ex.getMessage());
                    plugin.getLogger().severe("This is not an issue with CustomStructures! Please contact the addon developer.");
                    if (plugin.isDebug())
                        ex.printStackTrace();
                }
            }

            for (Class<? extends StructureSection> section : addon.getStructureSections()) {
                try {
                    StructureSection constructedSection = section.getConstructor().newInstance();
                    // Check if the section exists in the config file.
                    if (!config.contains(constructedSection.getName())) {
                        constructedSection.setupSection(null);
                    } else {
                        constructedSection.setupSection(config.getConfigurationSection(constructedSection.getName()));
                    }
                    this.structureSections.add(constructedSection);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    // Inform the user of errors.
                    plugin.getLogger().severe(String.format("The section %s for the addon %s" +
                                    "is configured incorrectly. If you are the developer please refer to the API documentation.",
                            section.getName(), addon.getName()));
                    plugin.getLogger().severe("This is not an issue with CustomStructures." +
                            "Report this error to the addon developer!!");
                } catch (StructureConfigurationException ex) {
                    // Handle the structureConfigurationException.
                    throw new StructureConfigurationException(String.format("[%s Addon] %s. This is not" +
                            "an issue with the CustomStructures plugin.", addon.getName(), ex.getMessage()));
                } catch (Exception ex) {
                    // Inform the user of errors.
                    plugin.getLogger().severe(String.format("An error was encountered in the %s addon! Enable debug for more information.", addon.getName()));
                    plugin.getLogger().severe(ex.getMessage());
                    plugin.getLogger().severe("This is not an issue with CustomStructures! Please contact the addon developer.");
                    if (plugin.isDebug())
                        ex.printStackTrace();
                }
            }
        }
    }

    private void checkValidity() {
        if (!config.contains("schematic")) {
            throw new StructureConfigurationException("Invalid structure config: No Schematic found!");
        }
        if (!config.contains("Chance.Number")) {
            throw new StructureConfigurationException("Invalid structure config: `Chance.Number` is required!");
        }
        if (!config.contains("Chance.OutOf")) {
            throw new StructureConfigurationException("Invalid structure config: `Chance.OutOf` is required!");
        }
        if (!config.isInt("Chance.Number") || config.getInt("Chance.Number") < 1) {
            throw new StructureConfigurationException("Invalid structure config: `Chance.Number` must be a number cannot be less than 1!");
        }
        if (!config.isInt("Chance.OutOf") || config.getInt("Chance.OutOf") < 1) {
            throw new StructureConfigurationException("Invalid structure config: `Chance.OutOf` must be a number cannot be less than 1!");
        }
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
     * Set the bottom space fill property.
     *
     * @param bottomSpaceFill The bottom space fill property.
     */
    public void setBottomSpaceFill(BottomSpaceFill bottomSpaceFill) {
        this.bottomSpaceFill = bottomSpaceFill;
    }

    /**
     * Set the (simple) sub-schematic property.
     *
     * @param subSchematics The sub-schematic property.
     */
    public void setSubSchematics(SubSchematics subSchematics) {
        this.subSchematics = subSchematics;
    }

    /**
     * Set the advanced sub-schematic property.
     *
     * @param advancedSubSchematics The advanced sub-schematic property.
     */
    public void setAdvancedSubSchematics(AdvancedSubSchematics advancedSubSchematics) {
        this.advancedSubSchematics = advancedSubSchematics;
    }

    /**
     * Set the loot tables from a configuration section.
     *
     * @param lootableConfig The loot table configuration section.
     */
    public void setLootTables(ConfigurationSection lootableConfig) {
        lootTables = new HashMap<>();
        assert lootableConfig != null;
        for (String lootTable : lootableConfig.getKeys(false)) {
            if (!LootTableType.exists(lootTable))
                continue;
            LootTableType type = LootTableType.valueOf(lootTable.toUpperCase());
            // Loop through the new loot table section.
            for (String lootTableName : Objects.requireNonNull(lootableConfig.getConfigurationSection(lootTable)).getKeys(false)) {
                int weight = lootableConfig.getInt(lootTable + "." + lootTableName);
                LootTable table = CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTableName);
                table.addType(type);
                if (lootTables.containsKey(type))
                    lootTables.get(type).add(weight, table);
                else {
                    lootTables.put(type, new RandomCollection<>());
                    lootTables.get(type).add(weight, table);
                }
            }
        }
    }

    /**
     * Set the loot tables using a collection of LootTable.
     *
     * @param lootTables The collection of LootTables.
     */
    public void setLootTables(Map<LootTableType, RandomCollection<LootTable>> lootTables) {
        this.lootTables = lootTables;
    }

    /**
     * Add a loot table to the structure.
     *
     * @param lootTable The loot table to add.
     * @param weight    The weight.
     */
    public void addLootTable(LootTable lootTable, double weight) {
        for (LootTableType type : lootTable.getTypes()) {
            if (!lootTables.containsKey(type))
                lootTables.put(type, new RandomCollection<>());
            lootTables.get(type).add(weight, lootTable);
        }
    }

    /**
     * Set the base rotation of a structure.
     *
     * <p>This is an API only functionality. It sets what the structure should be rotated by, while still allowing
     * for random rotation is desired.</p>
     *
     * @param baseRotation The base rotation of a structure. (In Radians.)
     */
    public void setBaseRotation(double baseRotation) {
        this.baseRotation = baseRotation;
    }

    /**
     * Add a structure section to the structure builder.
     * <p>Note: {@link StructureSection#setupSection(ConfigurationSection)} is NOT called by this method. You are expected
     * to use a constructor.</p>
     *
     * @param structureSection The structure section to add.
     */
    public void addStructureSection(StructureSection structureSection) {
        this.structureSections.add(structureSection);
    }

    /**
     * Build the structure.
     * <p>Note: This does not check to see if all values are set. If any of the properties are not set
     * than a NullPointerException will occur.</p>
     *
     * @return The structure.
     */
    public Structure build() {
        Objects.requireNonNull(name, "The structure name cannot be null.");
        Objects.requireNonNull(schematic, "The structure schematic cannot be null.");
        Objects.requireNonNull(structureLocation, "The structure location cannot be null.");
        Objects.requireNonNull(structureProperties, "The structure property cannot be null.");
        Objects.requireNonNull(structureLimitations, "The structure limitations cannot be null.");
        Objects.requireNonNull(maskProperty, "The structure mask property cannot be null.");
        Objects.requireNonNull(subSchematics, "The structure sub-schematic property cannot be null.");
        Objects.requireNonNull(advancedSubSchematics, "The structure advanced sub-schematic property cannot be null.");
        Objects.requireNonNull(bottomSpaceFill, "The structure bottom space fill property cannot be null.");
        Objects.requireNonNull(lootTables, "The structure loot tables cannot be null.");
        Objects.requireNonNull(structureSections, "The structure sections list cannot be null.");
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
        config.set("StructureLocation.SpawnYHeightMap", structureLocation.getSpawnSettings().getHeightMap().toString());
        config.set("StructureLocation.Biome", structureLocation.getBiomes());

        config.set("StructureProperties.PlaceAir", structureProperties.canPlaceAir());
        config.set("StructureProperties.randomRotation", structureProperties.isRandomRotation());
        config.set("StructureProperties.ignorePlants", structureProperties.isIgnoringPlants());
        config.set("StructureProperties.spawnInWater", structureProperties.canSpawnInWater());
        config.set("StructureProperties.spawnInLavaLakes", structureProperties.canSpawnInLavaLakes());

        config.set("StructureLimitations.whitelistSpawnBlocks", structureLimitations.getWhitelistBlocks());

        if (isCompiled)
            config.set("compiled_schematic", compiledSchematic);

        for (Map.Entry<LootTableType, RandomCollection<LootTable>> loot : lootTables.entrySet()) {
            for (Map.Entry<Double, LootTable> entry : loot.getValue().getMap().entrySet()) {
                config.set("LootTables." + loot.getKey().toString() + "." + entry.getValue().getName(), entry.getKey());
            }
        }
        config.save(file);
    }

}
