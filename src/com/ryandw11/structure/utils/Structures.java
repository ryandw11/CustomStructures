package com.ryandw11.structure.utils;

import java.util.ArrayList;
import java.util.List;
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
 * @version 1.3.4-Pre2
 *
 */
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
        FileConfiguration config = plugin.getConfig();
		for(String s : st){
			num = r.nextInt(config.getInt("Schematics." + s + ".Chance.OutOf") - 1) + 1;
			if(num <= config.getInt("Schematics." + s + ".Chance.Number")){
				if(!config.getBoolean("Schematics." + s + ".AllWorlds")){ // Checking to see if the world is correct
					List<String> worlds = config.getStringList("Schematics." + s + ".AllowedWorlds");
					if(!worlds.contains(bl.getWorld().getName()))
						return;
				}
				
				int spawnY = this.getSpawnY(config.getString("Schematics." + s + ".SpawnY"), ch, r, config, s);
			
				bl = ch.getBlock(0, spawnY, 0);
				if(!config.getString("Schematics." + s + ".Biome").equalsIgnoreCase("all")){//Checking biome
					if(!getBiomes(config.getString("Schematics." + s + ".Biome").toLowerCase()).contains(bl.getBiome().toString().toLowerCase()))
						return;
				}
			
				//Now to finally paste the schematic
				SchematicHandeler sh = new SchematicHandeler();
				try {
					sh.schemHandle(bl.getLocation(), config.getString("Schematics." + s + ".Schematic"), config.getBoolean("Schematics." + s + ".PlaceAir"));
				} catch (Exception e) {
					e.printStackTrace();
				}

				return;// return after pasting
			}
		}
		return; // If no schematic is able to spawn.
	
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	protected ArrayList<String> getMyLore(java.util.List<String> list){
		ArrayList<String> ls = new ArrayList<String>();
		for(String lore : list){
			ls.add(ChatColor.translateAlternateColorCodes('&', lore));
		}
		return ls;
	}
	/**
	 * 
	 * @param s
	 * @return
	 */
	protected ArrayList<String> getBiomes(String s){
		String[] biomes = s.split(",");
		ArrayList<String> output = new ArrayList<String>();
		for(String b : biomes)
			output.add(b);
		return output;
		
	}
	/**
	 * 
	 * @param s
	 * @param ch
	 * @param r
	 * @param file
	 * @param schem
	 * @return
	 */
	protected int getSpawnY(String s, Chunk ch, Random r, FileConfiguration file, String schem) {
		int y;
		try {
			y = Integer.parseInt(s);
		}catch(NumberFormatException e) {
			y = r.nextInt(splitTwo(s) - splitOne(s)) + splitOne(s);
		}
		if(y == -1) {
			Block b = ch.getBlock(0, 0, 0);
			y = ch.getWorld().getHighestBlockYAt(b.getX(), b.getZ());
			
		}
		int offset = file.getInt("Schematics." + schem + ".OffSet");
		
		y += offset;
		return y;
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	
	protected int splitOne(String s) {
		String[] st = s.split("-");
		String firstValue = st[0].replace("[", "");
		int finalVal;
		try {
			finalVal = Integer.parseInt(firstValue);
		}catch(NumberFormatException e) {
			finalVal = 25; //Random fail space
		}
		return finalVal;
	}
	/**
	 * 
	 * @param s
	 * @return
	 */
	
	protected int splitTwo(String s) {
		String[] st = s.split("-");
		String firstValue = st[1].replace("]", "");
		int finalVal;
		try {
			finalVal = Integer.parseInt(firstValue);
		}catch(NumberFormatException e) {
			finalVal = 100; //Random fail space
		}
		return finalVal;
	}
	
	
	/*
	 * Checks to see if there is a chest.
	 */
	
//	for(BlockState bst : ch.getTileEntities()){
//		Bukkit.broadcastMessage("test");
//		if(bst.getBlock() instanceof Chest){
//			Chest c = (Chest) bst;
//			if(c.getInventory().getContents().length == 0){
//				
//				if(plugin.getConfig().getString("Schematics." + s + ".SpecialChest") != "none" 
//						&& plugin.getConfig().getString("Schematics." + s + ".SpecialChest") != null){
//					
//					if (plugin.itemf.contains(plugin.getConfig().getString("Schematics." + s + ".SpecialChest"))){ //check to see if it exsists
//						setupChest(c, plugin.itemf, plugin.getConfig().getString("Schematics." + s + ".SpecialChest"));
//					}else{
//						plugin.getLogger().warning("An error in your item.yml has occured. The item " + plugin.getConfig().getString("Schematics." + s + ".SpecialChest") + " does not exist.");
//					}
//				}
//			}
//		}
//	}
	
	
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
	

}
