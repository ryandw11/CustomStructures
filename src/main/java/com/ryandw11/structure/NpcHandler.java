package com.ryandw11.structure;

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
                        alias = (String) npc.get("alias");
                        npcInfo.name = (String) npc.get("name");
                        npcInfo.skinUrl = (String) npc.get("skinUrl");
                        npcInfo.movesAround = (Boolean) npc.get("movesAround");
                        npcInfo.makesSounds = (Boolean) npc.get("makesSounds");
                        npcInfo.looksAtPlayer = (Boolean) npc.get("looksAtPlayer");
                        npcInfo.isProtected = (Boolean) npc.get("isProtected");
                        npcInfo.entityType = (String)npc.get("entityType");
                        List<String> commands = (List<String>)npc.get("commands");
                        if(commands != null && !commands.isEmpty()) {
                            npcInfo.commands = commands;
                        }
                        npcInfoMap.put(alias, npcInfo);
                        Bukkit.getLogger().info("> NCP '" + alias + "': " + npcInfo);
                    } catch(Exception e) {
                        Bukkit.getLogger().warning("> Failed to process NPC '" + alias + "':" + e.toString());
                    }
                }

            } catch (Exception e) {
                Bukkit.getLogger().severe("NPC configuration error: " + e.toString());
            }
        }
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

    public class NpcInfo {

        public String name = "";
        public String skinUrl = "";
        public boolean movesAround = false;
        public boolean makesSounds = false;
        public boolean looksAtPlayer = false;
        public boolean isProtected = false;
        public String entityType = "VILLAGER";

        public List<String> commands = new ArrayList<>();

        public String toString() {
            return "[name:" + name + "][isProtected:" + isProtected + "][movesAround:" + movesAround + "][makesSounds:" + makesSounds
                    + "][looksAtPlayer:" + looksAtPlayer + "][skinUrl:" + skinUrl + "]";
        }

    }
}
