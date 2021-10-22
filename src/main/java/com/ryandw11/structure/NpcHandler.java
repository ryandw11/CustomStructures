package com.ryandw11.structure;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuration of NPCs.
 *
 * @author Marcel Schoen
 */
public class NpcHandler {

    private Map<String, NpcInfo> npcInfoMap = new HashMap<>();
    private PluginManager pluginManager;

    public NpcHandler(File dataFolder, PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File npcFile = new File(dataFolder, "npcs.yml");
        if(npcFile.exists()) {
            try {
                yamlConfiguration.load(new File(dataFolder, "npcs.yml"));

                List<Map<?, ?>> npcs = yamlConfiguration.getMapList("CitizenNPCs");
                Bukkit.getLogger().info("Number of NPCs configured: " + npcs.size());

                for(Map<?, ?> npc : npcs) {
                    String alias = "?";
                    try {
                        NpcInfo npcInfo = new NpcInfo();
                        alias = getStringValueWithDefault(npc, "alias", null);
                        if(alias != null) {
                            npcInfo.name = getStringValueWithDefault(npc, "name", "");
                            npcInfo.skinUrl = getStringValueWithDefault(npc, "skinUrl", null);
                            npcInfo.movesAround = getBooleanValueWithDefault(npc, "movesAround");
                            npcInfo.makesSounds = getBooleanValueWithDefault(npc, "makesSounds");
                            npcInfo.looksAtPlayer = getBooleanValueWithDefault(npc, "looksAtPlayer");
                            npcInfo.isProtected = getBooleanValueWithDefault(npc, "isProtected");
                            npcInfo.commandsSequential = getBooleanValueWithDefault(npc, "commandsSequential");
                            npcInfo.entityType = getStringValueWithDefault(npc, "entityType", "VILLAGER");
                            List<String> commands = (List<String>)npc.get("commands");
                            if(commands != null && !commands.isEmpty()) {
                                npcInfo.commands = commands;
                            }
                            npcInfoMap.put(alias, npcInfo);
                            Bukkit.getLogger().info("> NPC '" + alias + "': " + npcInfo);
                        } else {
                            Bukkit.getLogger().info("> NPC configuration error, no 'alias' configured!");
                        }
                    } catch(Exception e) {
                        Bukkit.getLogger().warning("> Failed to process NPC '" + alias + "':" + e.toString());
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                Bukkit.getLogger().severe("NPC configuration error: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private String getStringValueWithDefault(Map<?, ?> npc, String attributeName, String defaultValue) {
        if(npc.containsKey(attributeName)) {
            return (String) npc.get(attributeName);
        }
        return defaultValue;
    }

    private Boolean getBooleanValueWithDefault(Map<?, ?> npc, String attributeName) {
        if(npc.containsKey(attributeName)) {
            return (Boolean) npc.get(attributeName);
        }
        return false;
    }

    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    public void cleanUp() {
        Bukkit.getLogger().info("Clear NPC table.");
        npcInfoMap.clear();
    }

    public NpcInfo getNpcInfoByAlias(String alias) {
        return npcInfoMap.get(alias);
    }

    /**
     * NPC config information holder
     */
    public class NpcInfo {
        public String name = "";
        public String skinUrl = "";
        public boolean movesAround = false;
        public boolean makesSounds = false;
        public boolean looksAtPlayer = false;
        public boolean isProtected = false;
        public String entityType = "VILLAGER";
        public List<String> commands = new ArrayList<>();
        public boolean commandsSequential = false;

        public String toString() {
            return new ToStringBuilder(this).toString();
        }
    }
}
