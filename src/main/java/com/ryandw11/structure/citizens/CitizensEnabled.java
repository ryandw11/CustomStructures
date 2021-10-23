package com.ryandw11.structure.citizens;

import com.google.gson.Gson;
import com.ryandw11.structure.NpcHandler;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Spawns a Citizen NPC at the location of the sign.
 *
 * @author Marcel Schoen
 */
public class CitizensEnabled implements CitizensNpcHook {

	// Use in-memory cache to avoid downloading the same skin data again and again
	private static Map<String, Map<String, Object>> skinDataCache = new HashMap<>();

	// Marker for failed skin downloads to not attempt again (could lag server)
	private static final Map<String, Object> INVALID = new HashMap<>();

	@Override
	public void spawnNpc(NpcHandler npcHandler, String alias, Location loc) {
		NpcHandler.NpcInfo info = npcHandler.getNpcInfoByAlias(alias);
		if (info != null) {
			EntityType type = EntityType.VILLAGER;
			try {
				type = EntityType.valueOf(info.entityType);
			} catch(Exception ex) {
				Bukkit.getLogger().warning("> UNSUPPORTED ENTITY TYPE '" + info.entityType + "'! Spawning a villager instead.");
			}
			NPC npc = CitizensAPI.getNPCRegistry().createNPC(type, info.name);
			int npcId = npc.getId();
			if(npc != null) {
				if(!npc.isSpawned()) {
					npc.spawn(loc);
				}

				// Should NPC be protected (invulnerable)
				npc.setProtected(info.isProtected);

				// Should NPC move around
				npc.setUseMinecraftAI(info.movesAround);

				// Make NPC look at player when they come close
				LookClose lookClose = npc.getOrAddTrait(LookClose.class);
				lookClose.lookClose(info.looksAtPlayer);

				// Run commands to be executed when NPC is created
				if(!info.commandsOnCreate.isEmpty()) {
					Bukkit.getLogger().info("> Executing " + info.commandsOnCreate.size() + " commands now...");
					for(String command : info.commandsOnCreate) {
						command = command.trim();
						command = command.replace("<npcid>", String.valueOf(npcId));
						// The [PLAYER] prefix is not supported here for obvious reasons
						if(command.toUpperCase().startsWith("[PLAYER]")) {
							// cut off the [PLAYER] prefix
							command = command.substring(8);
							Bukkit.getLogger().warning("> Ignoring [PLAYER] prefix for 'commandsOnCreate' commands!");
						}
						Bukkit.getLogger().info("> Executing command for newly created NPC: '" + command + "'");
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
					}
				}

				// Add commands to be executed when player clicks NPC
				if(!info.commandsOnClick.isEmpty()) {
					for(String command : info.commandsOnClick) {
						command = command.trim();
						command = command.replace("<npcid>", String.valueOf(npcId));
						boolean isPlayerCommand = command.toUpperCase().startsWith("[PLAYER]");
						if(isPlayerCommand) {
							// cut off the [PLAYER] prefix
							command = command.substring(8);
						}
						CommandTrait commandTrait = npc.getOrAddTrait(CommandTrait.class);
						CommandTrait.NPCCommandBuilder cmdBuilder = new CommandTrait.NPCCommandBuilder(command, CommandTrait.Hand.RIGHT);
						cmdBuilder.op(true);
						if(isPlayerCommand) {
							cmdBuilder.player(true);
						}
						commandTrait.addCommand(cmdBuilder);
						commandTrait.setExecutionMode(info.commandsSequential ? CommandTrait.ExecutionMode.SEQUENTIAL : CommandTrait.ExecutionMode.LINEAR);
						Bukkit.getLogger().info("> Set command for NPC: '" + command + "'");
					}
				}

				// Change skin of NPC
				if(type == EntityType.PLAYER && info.skinUrl != null && !info.skinUrl.isEmpty()) {
					changeSkin(npc, info.skinUrl);
				}
				npc.setBukkitEntityType(EntityType.valueOf(info.entityType));
			} else {
				Bukkit.getLogger().warning("> Failed to spawn NPC '" + alias + "', reason unknown / no errors detected.");
			}
		} else {
			Bukkit.getLogger().warning("> Failed to spawn NPC '" + alias + "', no configuration found.");
		}
	}

	/**
	 * Attempts to change the skin of the given NPC. This may
	 * fail if the skin download from the given URL fails. In
	 * that case, the NPC will just remain unchanged.
	 *
	 * @param npc The NPC for which to change the skin.
	 * @param url The skin download URL.
	 */
	private static void changeSkin(NPC npc, String url) {
		Map<String, Object> skinData = skinDataCache.get(url);
		if(skinData == null) {
			skinData = downloadFromMineskinOrg(url);
			if(skinData == null) {
				// Failed to download - mark as invalid to not try again
				skinDataCache.put(url, INVALID);
			}
		}
		if(skinData != null && skinData != INVALID) {
			skinDataCache.put(url, skinData);
			try {
				Map<String, Object> data = (Map<String, Object>) skinData.get("data");
				String uuid = (String) data.get("uuid");
				Map<String, Object> texture = (Map<String, Object>) data.get("texture");
				String textureEncoded = (String) texture.get("value");
				String signature = (String) texture.get("signature");
				SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);
				trait.setSkinPersistent(uuid, signature, textureEncoded);
			} catch(Exception ex) {
				// In case of any NPE's caused by invalid data
				Bukkit.getLogger().warning("> Failed to set skin, probably invalid / corrupt skin data. Reason: " + ex.toString());
			}
		} else {
			Bukkit.getLogger().warning("> Failed to retrieve skin.");
		}
	}

	/**
	 * Downloads a Minecraft skin through the "api.mineskin.org" service.
	 *
	 * @param url The actual skin URL.
	 * @return The skin information map (may be null!)
	 */
	private static Map<String, Object> downloadFromMineskinOrg(String url) {
		DataOutputStream out = null;
		BufferedReader reader = null;
		try {
			URL target = new URL("https://api.mineskin.org/generate/url");
			HttpURLConnection con = (HttpURLConnection) target.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setConnectTimeout(1000);
			con.setReadTimeout(30000);
			out = new DataOutputStream(con.getOutputStream());
			out.writeBytes("url=" + URLEncoder.encode(url, "UTF-8"));
			out.close();
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			Map<String, Object> skinInfo = new Gson().fromJson(reader, Map.class);
			con.disconnect();
			return skinInfo;
		} catch (Throwable t) {
			Bukkit.getLogger().warning("> Failed to download skin: " + url + ", reason: " + t.toString());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
}
