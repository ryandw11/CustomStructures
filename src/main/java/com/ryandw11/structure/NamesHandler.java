package com.ryandw11.structure;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles configuration of name generator data.
 *
 * @author Marcel Schoen
 */
public class NamesHandler {

    private Map<String, NameGenerator> nameGeneratorMap = new HashMap<>();

    /**
     * Processes the name generator configuration
     *
     * @param dataFolder The base plugin data folder.
     * @param isDebug True if debug output is enabled.
     */
    public NamesHandler(File dataFolder, boolean isDebug) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File namesDirectory = new File(dataFolder, "names");
        if(namesDirectory.exists()) {
            String[] nameFileEntries = namesDirectory.list(new WildcardFileFilter("*.txt"));
            for(String nameFileEntry : nameFileEntries) {
                File nameFile = new File(namesDirectory, nameFileEntry);
                try {
                    NameGenerator nameGenerator = new NameGenerator(nameFile);
                    String alias = nameFileEntry.substring(0, nameFileEntry.indexOf("."));
                    nameGeneratorMap.put(alias, nameGenerator);
                } catch(IOException e) {
                    Bukkit.getLogger().warning("Failed to process language data file: " + nameFileEntry + ", Reason: " + e.toString());
                    e.printStackTrace();
                }
            }
        }
    }

    public String replaceNamePlaceholders(String name, int numberOfSyllables) {
        for(String alias : nameGeneratorMap.keySet()) {
            String placeholder = "<name-" + alias + ">";
            if(name.contains(placeholder)) {
                name = name.replace(placeholder, getName(alias, numberOfSyllables));
            }
        }
        return name;
    }

    private String getName(String alias, int numberOfSyllables) {
        if(nameGeneratorMap.containsKey(alias)) {
            return nameGeneratorMap.get(alias).compose(numberOfSyllables);
        }
        return "undefined";
    }

    /**
     * Cleans up the NPC data.
     */
    public void cleanUp() {
        nameGeneratorMap.clear();
    }
}
