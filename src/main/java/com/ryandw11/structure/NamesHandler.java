package com.ryandw11.structure;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handles configuration of name generator data.
 *
 * @author Marcel Schoen
 */
public class NamesHandler {

    public static final String PREFIX_NAME = "<name-";
    public static final String PREFIX_LAST_NAME = "<lastName";
    public static final String PREFIX_UNIQUE_NAME = "<uniqueName-";
    private static Random random = new Random();
    private Map<String, NameGenerator> nameGeneratorMap = new HashMap<>();
    private File namesFolder = null;

    /**
     * Processes the name generator configuration
     *
     * @param dataFolder The base plugin data folder.
     */
    public NamesHandler(File dataFolder) {
        this.namesFolder = new File(dataFolder, "names/existing");
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File namesDirectory = new File(dataFolder, "names");
        if(namesDirectory.exists()) {
            if(!this.namesFolder.exists()) {
                this.namesFolder.mkdirs();
            }
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

    public String replaceNamePlaceholders(String text, List<String> lastName) {
        text = replaceNamePlaceholdersWithPrefix(PREFIX_LAST_NAME, text, lastName, false);
        text = replaceNamePlaceholdersWithPrefix(PREFIX_NAME, text, lastName, false);
        text = replaceNamePlaceholdersWithPrefix(PREFIX_UNIQUE_NAME, text, lastName, true);
        return text;
    }

    public String replaceNamePlaceholdersWithPrefix(String prefix, String text, List<String> lastName, boolean mustBeUnique) {
        while(text.contains(prefix)) {
            boolean reUseLastName = prefix == PREFIX_LAST_NAME;
            String prePart = text.substring(0, text.indexOf(prefix));
            String namePart = text.substring(text.indexOf(prefix), text.indexOf(">") + 1);
            String postPart = text.substring(text.indexOf(">") + 1);

            String generatedName = "undefined";
            if(reUseLastName) {
                if(lastName != null && !lastName.isEmpty()) {
                    generatedName = lastName.get(0);
                }
                Bukkit.getLogger().warning("Unable to use last name, as there is none yet. Use <lastName> placeholder only after one of the <name-*> placeholders!");
            } else {
                generatedName = generateName(namePart, mustBeUnique);
            }
            if(lastName != null) {
                if(lastName.isEmpty()) {
                    lastName.add(generatedName);
                } else {
                    lastName.set(0, generatedName);
                }
            }

            text = prePart + generatedName + postPart;
        }
        return text;
    }

    private String generateName(String nameValue, boolean mustBeUnique) {
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

        String generatedName = getName(alias, numberOfSyllables);
        if(mustBeUnique) {
            int ct = 0;
            boolean alreadyExists = new File(namesFolder, generatedName + ".txt").exists();
            while(alreadyExists && ct++ < 100) {
                generatedName = getName(alias, numberOfSyllables);
                if(ct == 25 || ct == 50 || ct == 75) {
                    numberOfSyllables++;
                }
                alreadyExists = new File(namesFolder, generatedName + ".txt").exists();
            }
            try {
                new File(namesFolder, generatedName + ".txt").createNewFile();
            } catch(IOException e) {
                Bukkit.getLogger().warning("Failed to create name file to ensure unique names, for name: " + generatedName + ", Reason: " + e.toString());
                e.printStackTrace();
            }
        }

        return generatedName;
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
