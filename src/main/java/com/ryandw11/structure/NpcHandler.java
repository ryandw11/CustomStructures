package com.ryandw11.structure;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuration of NPCs.
 */
public class NpcHandler {

    private final Map<String, NpcInfo> npcInfoMap = new HashMap<>();

    /**
     * Processes the NPC configuration
     *
     * @param dataFolder The base plugin data folder.
     * @param plugin     The instance to the Custom Structures plugin.
     */
    public NpcHandler(File dataFolder, CustomStructures plugin) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File npcFile = new File(dataFolder, "npcs.yml");
        if (!npcFile.exists()) {
            return;
        }

        try {
            yamlConfiguration.load(new File(dataFolder, "npcs.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("There is a configuration error with: npcs.yml.");
            if (plugin.isDebug()) {
                e.printStackTrace();
            }
        }

        for (String npcKey : yamlConfiguration.getKeys(false)) {
            ConfigurationSection section = yamlConfiguration.getConfigurationSection(npcKey);
            if (section == null) continue;

            NpcInfo npcInfo = new NpcInfo();
            npcInfo.name = getValueWithDefault(section, "name", "");
            npcInfo.skinUrl = getValueWithDefault(section, "skinUrl", null);
            npcInfo.movesAround = getValueWithDefault(section, "movesAround", false);
            npcInfo.looksAtPlayer = getValueWithDefault(section, "looksAtPlayer", false);
            npcInfo.isProtected = getValueWithDefault(section, "isProtected", false);
            npcInfo.commandsSequential = getValueWithDefault(section, "commandsSequential", false);
            npcInfo.entityType = getValueWithDefault(section, "entityType", "VILLAGER");
            List<String> commandsOnCreate = section.getStringList("commandsOnCreate");
            if (!commandsOnCreate.isEmpty()) {
                npcInfo.commandsOnCreate = commandsOnCreate;
            }
            List<String> commandsOnClick = section.getStringList("commandsOnClick");
            if (!commandsOnClick.isEmpty()) {
                npcInfo.commandsOnClick = commandsOnClick;
            }
            npcInfoMap.put(npcKey, npcInfo);
        }
    }

    /**
     * Get a string value with a possible default.
     *
     * @param npc           The configuration section.
     * @param attributeName The name of the attribute.
     * @param defaultValue  The default value.
     * @return The desired attribute.
     */
    private <T> T getValueWithDefault(ConfigurationSection npc, String attributeName, T defaultValue) {
        return npc.contains(attributeName) ? (T) npc.get(attributeName) : defaultValue;
    }

    /**
     * Cleans up the NPC data.
     */
    public void cleanUp() {
        npcInfoMap.clear();
    }

    /**
     * Get the NPC by its name.
     *
     * @param name The name of the NPC.
     * @return The NPC Info.
     */
    public NpcInfo getNPCByName(String name) {
        return npcInfoMap.get(name);
    }

    /**
     * NPC config information holder
     */
    public static class NpcInfo {
        public String name = "";
        public String skinUrl = "";
        public boolean movesAround = false;
        public boolean looksAtPlayer = false;
        public boolean isProtected = false;
        public String entityType = "VILLAGER";
        public List<String> commandsOnCreate = new ArrayList<>();
        public List<String> commandsOnClick = new ArrayList<>();
        public boolean commandsSequential = false;
    }
}
