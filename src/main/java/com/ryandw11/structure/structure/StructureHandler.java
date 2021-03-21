package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.CustomStructuresAPI;
import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.io.StructureFileReader;
import com.ryandw11.structure.threading.CheckStructureList;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This handler manages the list of active structures.
 *
 * <p>You can access this handler from {@link CustomStructuresAPI#getStructureHandler()} or {@link CustomStructures#getStructureHandler()}.</p>
 *
 * <p><b>Note:</b> Do not store a long term instance of this class as it can be nulled when the /cstruct reload command is done.</p>
 */
public class StructureHandler {

    private final SortedMap<Pair<Location, Long>, Structure> spawnedStructures = new TreeMap<>(
            Comparator.comparingDouble(o -> o.getLeft().distance(new Location(o.getLeft().getWorld(), 0, 0, 0)))
    );

    private final List<Structure> structures;
    private final List<String> names;
    private final CheckStructureList checkStructureList;
    private StructureFileReader structureFileReader;

    /**
     * Constructor for the structure handler.
     * <p>This is for internal use only. Use {@link CustomStructuresAPI#getStructureHandler()} or {@link CustomStructures#getStructureHandler()} instead.</p>
     *
     * @param stringStructs The list of structures.
     * @param cs            The plugin.
     */
    public StructureHandler(List<String> stringStructs, CustomStructures cs) {
        structures = new ArrayList<>();
        names = new ArrayList<>();
        cs.getLogger().info("Loading structures from files.");
        for (String s : stringStructs) {
            File struct = new File(cs.getDataFolder() + File.separator + "structures" + File.separator + s.replace(".yml", "") + ".yml");
            if (!struct.exists()) {
                cs.getLogger().warning("Structure file: " + s + ".yml does not exist! Did you make a new structure file in the Structure folder?");
                cs.getLogger().warning("For more information please check to wiki.");
                continue;
            }
            try {
                Structure tempStruct = new StructureBuilder(s.replace(".yml", ""), struct).build();
                structures.add(tempStruct);
                names.add(tempStruct.getName());
            } catch (StructureConfigurationException ex) {
                cs.getLogger().warning("The structure '" + s + "' has an invalid configuration file:");
                cs.getLogger().warning(ex.getMessage());
            } catch (Exception ex) {
                cs.getLogger().severe("An unexpected error has occurred when trying to load the structure: " + s + ".");
                cs.getLogger().severe("Please ensure that your configuration file is valid!");
                if (cs.isDebug()) {
                    ex.printStackTrace();
                } else {
                    cs.getLogger().severe("Please enable debug mode to see the full error.");
                }
            }
        }

        checkStructureList = new CheckStructureList(this);
        // Run every 5 minutes.
        checkStructureList.runTaskTimerAsynchronously(cs, 20, 6000);

        if (cs.getConfig().getBoolean("logStructures")) {
            structureFileReader = new StructureFileReader(cs);
            structureFileReader.runTaskTimerAsynchronously(cs, 20, 300);
        }
    }

    /**
     * Get the list of structures.
     * <p>This list is read only and cannot be modified.</p>
     *
     * @return The list of structures.
     */
    public List<Structure> getStructures() {
        return Collections.unmodifiableList(structures);
    }

    /**
     * Get structure by name
     *
     * @param name The name
     * @return The structure. (Returns null if the structure is not found).
     */
    public Structure getStructure(String name) {
        List<Structure> result = structures.stream().filter(struct -> struct.getName().equals(name)).collect(Collectors.toList());
        if (result.isEmpty())
            return null;
        return result.get(0);
    }

    /**
     * Get the structure by a number.
     *
     * @param i The number
     * @return The structure.
     */
    public Structure getStructure(int i) {
        return structures.get(i);
    }

    /**
     * Get the names of the structures.
     *
     * @return The names of the structures.
     */
    public List<String> getStructureNames() {
        return names;
    }

    /**
     * Get the Map of spawned structures.
     * <p>Note: This map is not synchronized by default and can be modified on a different thread.</p>
     *
     * @return The list of spawned structures.
     */
    public SortedMap<Pair<Location, Long>, Structure> getSpawnedStructures() {
        return spawnedStructures;
    }

    /**
     * Add a structure to the list of spawned structures.
     *
     * @param loc    The location.
     * @param struct The structure.
     */
    public void putSpawnedStructure(Location loc, Structure struct) {
        synchronized (spawnedStructures) {
            if (structureFileReader != null) {
                structureFileReader.addStructure(loc, struct);
            }
            this.spawnedStructures.put(Pair.of(loc, System.currentTimeMillis()), struct);
        }
    }

    /**
     * Calculate if the structure is far enough away from other structures.
     *
     * @param struct The structure to calculate that for.
     * @param location The location that the structure is spawning.
     * @return If the distance is valid according to its config.
     */
    public boolean validDistance(Structure struct, Location location) {
        double closest = Double.MAX_VALUE;
        synchronized (spawnedStructures) {
            for (Map.Entry<Pair<Location, Long>, Structure> entry : spawnedStructures.entrySet()) {
                if (entry.getKey().getLeft().distance(location) < closest)
                    closest = entry.getKey().getLeft().distance(location);
            }
        }
        return struct.getStructureLocation().getDistanceFromOthers() < closest;
    }

    /**
     * Get the structure file reader.
     * <p>This feature must be enabled via the config.</p>
     *
     * @return An Optional of the StructureFileReader.
     */
    public Optional<StructureFileReader> getStructureFileReader() {
        return Optional.ofNullable(structureFileReader);
    }

    /**
     * Shutdown internal processes.
     */
    public void cleanup() {
        checkStructureList.cancel();
        if (structureFileReader != null)
            structureFileReader.cancel();
        spawnedStructures.clear();
    }
}
