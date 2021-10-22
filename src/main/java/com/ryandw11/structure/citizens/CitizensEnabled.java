package com.ryandw11.structure.citizens;

import com.google.gson.Gson;
import com.ryandw11.structure.NpcHandler;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
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

import static com.ryandw11.structure.CustomStructures.CITIZENS_PLUGIN;

/**
 * Spawns a Citizen NPC at the location of the sign.
 *
 * @author Marcel Schoen
 */
public class CitizensEnabled implements CitizensNpcHook {

	// Use in-memory cache to avoid downloading the same skin data again and again
	private static Map<String, Map<String, Object>> skinDataCache = new HashMap<>();

	@Override
	public void spawnNpc(NpcHandler npcHandler, String alias, Location loc) {
		Object pluginObj = npcHandler.getPluginManager().getPlugin(CITIZENS_PLUGIN);
		if(pluginObj != null && pluginObj instanceof Citizens) {
			Citizens citizensPlugin = (Citizens)pluginObj;

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
					npc.setProtected(info.isProtected);
					npc.setUseMinecraftAI(info.movesAround);
					if(!info.commands.isEmpty()) {

					}
					///npc.addTrait(OgreProperties.class);
					if(!info.skinUrl.isEmpty()) {
						downloadSkin(npc, info.skinUrl);
					}
					npc.setBukkitEntityType(EntityType.valueOf(info.entityType));
				} else {
					Bukkit.getLogger().warning("> Failed to spawn NPC '" + alias + "', reason unknown / no errors detected.");
				}
			} else {
				Bukkit.getLogger().warning("> Failed to spawn NPC '" + alias + "', no configuration found.");
			}
		} else {
			Bukkit.getLogger().warning("> Failed to spawn NPC '" + alias + "', unable to use Citizens plugin.");
		}
	}

	private static void downloadSkin(NPC npc, String url) {
		Map<String, Object> skinData = skinDataCache.get(url);
		if(skinData == null) {
			skinData = readJsonSkinData("https://gamepedia.cursecdn.com/minecraft_gamepedia/3/37/Steve_skin.png");
		}
		if(skinData != null) {
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
	private static Map<String, Object> readJsonSkinData(String url) {
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

	// FOR MANUAL TESTING ONLY - REMOVE LATER
	public static void main(String[] args) {
		Map<String, Object> stuff = readJsonSkinData("https://gamepedia.cursecdn.com/minecraft_gamepedia/3/37/Steve_skin.png");
		System.out.println("Result map: " + stuff);

		Map<String, Object> data = (Map<String, Object>)stuff.get("data");
		System.out.println("Result data: " + data);
	}
}
