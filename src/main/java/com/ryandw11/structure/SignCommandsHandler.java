package com.ryandw11.structure;

import org.apache.commons.lang.builder.ToStringBuilder;
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
     */
    public SignCommandsHandler(File dataFolder) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File signCommandsFile = new File(dataFolder, "signcommands.yml");
        if(signCommandsFile.exists()) {
            try {
                yamlConfiguration.load(signCommandsFile);

                List<Map<?, ?>> commandGroups = yamlConfiguration.getMapList("SignCommands");
                Bukkit.getLogger().info("Number of command groups configured: " + commandGroups.size());

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
                            Bukkit.getLogger().info("> Command group '" + alias + "': " + commandGroupInfo);
                        } else {
                            Bukkit.getLogger().info("> Command group configuration error, no 'alias' configured!");
                        }
                    } catch(Exception e) {
                        Bukkit.getLogger().warning("> Failed to process command group '" + alias + "':" + e.toString());
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                Bukkit.getLogger().severe("Command group configuration error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Cleans up the NPC data.
     */
    public void cleanUp() {
        Bukkit.getLogger().info("Clear commands groups table.");
        signCommandsInfoMap.clear();
    }

    public CommandGroupInfo getNpcInfoByAlias(String alias) {
        return signCommandsInfoMap.get(alias);
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
            return new ToStringBuilder(this).toString();
        }
    }
}
