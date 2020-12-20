package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.StructureConfigurationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the active structures.
 */
public class StructureHandler {

    private List<Structure> structures;
    private List<String> names;

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
}
