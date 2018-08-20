package com.ryandw11.structure.utils;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 
 * @author Ryandw11
 * @version 1.3
 *
 */
@SuppressWarnings("deprecation")
public class Structures {
	private CustomStructures plugin;
	private ArrayList<String> st;
	
	@SuppressWarnings("unchecked")
	public Structures (){
		this.plugin = CustomStructures.plugin;
		st = (ArrayList<String>) plugin.getConfig().get("Schematics.List");
	}
	
	/**
	 * Does all the schematic calculations for the ChunkLoad class.
	 * @param bl Block
	 * @param ch Chunk
	 */
	public void chooseBestStructure(Block bl, Chunk ch){
		Random r = new Random();
		int num;
        FileConfiguration config=plugin.getConfig();
		for(String s : st){
			num = r.nextInt(config.getInt("Schematics." + s + ".Chance.OutOf") - 1) + 1;
			if(num <= config.getInt("Schematics." + s + ".Chance.Number")){
				if(!config.getBoolean("Schematics." + s + ".AllWorlds")){ // Checking to see if the world is correct
					@SuppressWarnings("unchecked")
					ArrayList<String> worlds = (ArrayList<String>) config.get("Schematics." + s + ".AllowedWorlds");
					if(!worlds.contains(bl.getWorld().getName()))
						return;
				}
			
			
			if(!config.getString("Schematics." + s + ".Biome").equalsIgnoreCase("all")){//Checking biome
				if(!getBiomes(config.getString("Schematics." + s + ".Biome").toLowerCase()).contains(bl.getBiome().toString().toLowerCase()))
					return;
			}
			if(config.getInt("Schematics." + s + ".SpawnY") < -1){
				bl = ch.getBlock(0, (bl.getY() + config.getInt("Schematics." + s + ".SpawnY")) , 0);
			}
			else if(config.contains("Schematics." + s + ".SpawnY") && config.getInt("Schematics." + s + ".SpawnY") != -1){
				bl = ch.getBlock(0, config.getInt("Schematics." + s + ".SpawnY"), 0);
			}if(config.contains("Schematics."+s+".SpawnYmin")&&config.contains("Schematics."+s+".SpawnYmin")&&(bl.getY()<config.getInt("Schematics."+s+".SpawnYmin")||bl.getY()>config.getInt("Schematics."+s+".SpawnYmax")))return;
			//Now to finally paste the schematic
			SchematicHandeler sh = new SchematicHandeler();
			try {
				sh.schemHandle(bl.getLocation(), config.getString("Schematics." + s + ".Schematic"), config.getBoolean("Schematics." + s + ".PlaceAir"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * Checks to see if there is a chest.
			 */
			
//			for(BlockState bst : ch.getTileEntities()){
//				Bukkit.broadcastMessage("test");
//				if(bst.getBlock() instanceof Chest){
//					Chest c = (Chest) bst;
//					if(c.getInventory().getContents().length == 0){
//						
//						if(plugin.getConfig().getString("Schematics." + s + ".SpecialChest") != "none" 
//								&& plugin.getConfig().getString("Schematics." + s + ".SpecialChest") != null){
//							
//							if (plugin.itemf.contains(plugin.getConfig().getString("Schematics." + s + ".SpecialChest"))){ //check to see if it exsists
//								setupChest(c, plugin.itemf, plugin.getConfig().getString("Schematics." + s + ".SpecialChest"));
//							}else{
//								plugin.getLogger().warning("An error in your item.yml has occured. The item " + plugin.getConfig().getString("Schematics." + s + ".SpecialChest") + " does not exist.");
//							}
//						}
//					}
//				}
//			}
			return;// return after pasting
		}
		}
		return; // If no schematic is able to spawn.
	
	}
	
//	public void setupChest(Chest ch, FileConfiguration item, String name){
//		ch.setCustomName(ChatColor.translateAlternateColorCodes('&', item.getString(name + ".ChestName")));
//		Random r = new Random();
//		int num;
//		for(String items : item.getStringList(name + ".items")){
//			num = r.nextInt(item.getInt(name + "." + items + ".Chance") - 1) + 1;
//			if(num <= item.getInt(name + "." + items + ".Chance")){ //random chance
//				ItemStack i = new ItemStack(getMaterial(item.getString(name + "." + items + ".Item")), item.getInt(name + "." + items + ".Amount"), (byte) item.getInt(name + "." + items + ".ID"));
//				ItemMeta im = i.getItemMeta();
//				im.setDisplayName(ChatColor.translateAlternateColorCodes('&', item.getString(name + "." + items + ".DisplayName")));
//				ArrayList<String> lores = (ArrayList<String>) item.getStringList(name + "." + items + ".Lore");
//				im.setLore(getMyLore(lores));
//				i.setItemMeta(im);
//				try{
//					ch.getInventory().setItem(ch.getInventory().firstEmpty(), i);
//				}catch(Exception e){
//					
//				}
//			}
//			
//		}
//	}
//	protected Material getMaterial(String it){
//		MinecraftKey mk = new MinecraftKey(it);
//		ItemStack item = CraftItemStack.asNewCraftStack(Item.REGISTRY.get(mk));
//		Material mat = item.getType();
//		return mat;
//	}
	protected ArrayList<String> getMyLore(java.util.List<String> list){
		ArrayList<String> ls = new ArrayList<String>();
		for(String lore : list){
			ls.add(ChatColor.translateAlternateColorCodes('&', lore));
		}
		return ls;
	}
	
	protected ArrayList<String> getBiomes(String s){
		String[] biomes = s.split(",");
		ArrayList<String> output = new ArrayList<String>();
		for(String b : biomes)
			output.add(b);
		return output;
		
	}
	

}
