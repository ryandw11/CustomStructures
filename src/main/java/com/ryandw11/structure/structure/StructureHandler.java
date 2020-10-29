package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;

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

    public StructureHandler(List<String> stringStructs, CustomStructures cs){
        structures = new ArrayList<>();
        names = new ArrayList<>();
        cs.getLogger().info("Loading structures from files.");
        for(String s : stringStructs){
            File struct = new File(cs.getDataFolder() + File.separator + "structures" + File.separator + s.replace(".yml", "") + ".yml");
            if(!struct.exists()){
                cs.getLogger().warning("Structure file: " + s + ".yml does not exist! Did you make a new structure file in the Structure folder?");
                cs.getLogger().warning("For more information please check to wiki.");
                continue;
            }
            Structure tempStruct = new StructureBuilder(s.replace(".yml", ""), struct).build();
            if(tempStruct == null)
                continue;
            structures.add(tempStruct);
            names.add(tempStruct.getName());
        }
    }

    /**
     * Get the list of structures.
     * <p>This list is read only and cannot be modified.</p>
     * @return The list of structures.
     */
    public List<Structure> getStructures(){
        return Collections.unmodifiableList(structures);
    }

    /**
     * Get structure by name
     * @param name The name
     * @return The structure. (Returns null if the structure is not found).
     */
    public Structure getStructure(String name){
        List<Structure> result = structures.stream().filter(struct -> struct.getName().equals(name)).collect(Collectors.toList());
        if(result.isEmpty())
            return null;
        return result.get(0);
    }

    /**
     * Get the structure by a number.
     * @param i The number
     * @return The structure.
     */
    public Structure getStructure(int i){
        return structures.get(i);
    }

    /**
     * Get the names of the structures.
     * @return The names of the structures.
     */
    public List<String> getStructureNames(){
        return names;
    }
}
