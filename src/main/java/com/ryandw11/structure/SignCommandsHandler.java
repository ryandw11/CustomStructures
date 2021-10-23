package com.ryandw11.structure;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuration of sign commands.
 *
 * @author Marcel Schoen
 */
public class SignCommandsHandler {

    private Map<String, CommandGroupInfo> signCommandsInfoMap = new HashMap<>();

    /**
     * Processes the sign commands configuration
     *
     * @param dataFolder The base plugin data folder.
     * @param isDebug True if debug output is enabled.
     */
    public SignCommandsHandler(File dataFolder, boolean isDebug) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File signCommandsFile = new File(dataFolder, "signcommands.yml");
        if(signCommandsFile.exists()) {
            try {
                yamlConfiguration.load(signCommandsFile);

                List<Map<?, ?>> commandGroups = yamlConfiguration.getMapList("SignCommands");
                if(isDebug) Bukkit.getLogger().info("Number of command groups configured: " + commandGroups.size());

                for(Map<?, ?> commandGroup : commandGroups) {
                    String alias = "?";
                    try {
                        CommandGroupInfo commandGroupInfo = new CommandGroupInfo();
                        alias = (String) commandGroup.get("alias");
                        if(alias != null) {
                            List<String> commands = (List<String>)commandGroup.get("commands");
                            if(commands != null && !commands.isEmpty()) {
                                commandGroupInfo.commands = commands;
                            }
                            signCommandsInfoMap.put(alias, commandGroupInfo);
                            if(isDebug) Bukkit.getLogger().info("Sign command group '" + alias + "': " + commandGroupInfo);
                        } else {
                            Bukkit.getLogger().info("Sign command group configuration error, no 'alias' configured!");
                        }
                    } catch(Exception e) {
                        Bukkit.getLogger().warning("Failed to process sign command group '" + alias + "':" + e.toString());
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                Bukkit.getLogger().severe("Sign command group configuration error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Cleans up the NPC data.
     */
    public void cleanUp() {
        signCommandsInfoMap.clear();
    }

    public SignCommandsHandler.CommandGroupInfo getCommandGroupInfoByAlias(String alias) {
        return signCommandsInfoMap.get(alias);
    }

    /**
     * Commands group config information holder
     */
    public class CommandGroupInfo {
        public List<String> commands = new ArrayList<>();

        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
