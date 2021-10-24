package com.ryandw11.structure;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles configuration of name generator data.
 *
 * @author Marcel Schoen
 */
public class NamesHandler {

    private static Random random = new Random();
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

    public String replaceNamePlaceholders(String text) {
        while(text.contains("<name-")) {
            String prePart = text.substring(0, text.indexOf("<name-"));
            String namePart = text.substring(text.indexOf("<name-"), text.indexOf(">") + 1);
            String postPart = text.substring(text.indexOf(">") + 1);

            text = prePart + generateName(namePart) + postPart;
        }
        return text;
    }

    private String generateName(String nameValue) {
        String alias = nameValue.substring(nameValue.indexOf("-") + 1, nameValue.length() - 1);
        if(alias.contains(":")) {
            alias = alias.substring(0, alias.indexOf(":"));
        }
        int minNumberOfSyllables = 2;
        int maxNumberOfSyllables = 0;
        if(nameValue.contains(":")) {
            String numberArgument = nameValue.substring(nameValue.indexOf(":") + 1, nameValue.length() - 1);
            if(numberArgument.contains("-")) {
                minNumberOfSyllables = Integer.parseInt(numberArgument.substring(0, numberArgument.indexOf("-")));
                maxNumberOfSyllables = Integer.parseInt(numberArgument.substring(numberArgument.indexOf("-") + 1));
            } else {
                minNumberOfSyllables = Integer.parseInt(numberArgument);
            }
        }
        int numberOfSyllables = minNumberOfSyllables;
        if(maxNumberOfSyllables > minNumberOfSyllables) {
            numberOfSyllables = random.nextInt(maxNumberOfSyllables - minNumberOfSyllables) + minNumberOfSyllables;
        }
        return getName(alias, numberOfSyllables);
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
