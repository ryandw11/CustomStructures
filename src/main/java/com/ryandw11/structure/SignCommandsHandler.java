package com.ryandw11.structure;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuration of sign commands.
 */
public class SignCommandsHandler {

    private final Map<String, List<String>> signCommands = new HashMap<>();

    /**
     * Processes the sign commands configuration
     *
     * @param dataFolder The base plugin data folder.
     * @param plugin     The instance of the Custom Structures plugin.
     */
    public SignCommandsHandler(File dataFolder, CustomStructures plugin) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File signCommandsFile = new File(dataFolder, "signcommands.yml");
        if (!signCommandsFile.exists()) {
            plugin.getLogger().warning("Warning: Cannot find signcommands.yml. This might be a configuration error.");
            return;
        }
        try {
            yamlConfiguration.load(signCommandsFile);
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().severe("Error: Unable to load signcommands.yml file.");
            plugin.getLogger().severe("Please make sure signcommands.yml is configured correctly.");
            if (plugin.isDebug())
                ex.printStackTrace();
        }

        for (String sectionKey : yamlConfiguration.getKeys(false)) {
            List<String> commands = yamlConfiguration.getStringList(sectionKey);
            if (commands.isEmpty()) {
                plugin.getLogger().warning("Sign command " + sectionKey + " has no commands! This may be a configuration error.");
            }
            signCommands.put(sectionKey, commands);
        }
    }

    /**
     * Cleans up the command sign data.
     */
    public void cleanUp() {
        signCommands.clear();
    }

    /**
     * Get a command group from the command map.
     *
     * @param name The name of the command group.
     * @return The list of commands from the group.
     */
    public List<String> getCommands(String name) {
        return signCommands.get(name);
    }
}
