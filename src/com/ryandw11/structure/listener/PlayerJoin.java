package com.ryandw11.structure.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ryandw11.structure.CustomStructures;

public class PlayerJoin implements Listener{
	
	@EventHandler
	public void onPlayer(PlayerJoinEvent evt) {
		if(CustomStructures.enabled) return;
		Player p = evt.getPlayer();
		if(!p.isOp()) return;
		p.sendMessage(ChatColor.RED + "[CustomStructures] One of your schematic or lootable files could not be found!");
		p.sendMessage(ChatColor.RED + "[CustomStructures] Please check to see if all of your files are in the proper folders!");
		p.sendMessage(ChatColor.RED + "[CustomStructures] To find out more, see the error in the console.");
		p.sendMessage(ChatColor.RED + "[CustomStructures] If you just installed this plugin, please configure it before use.");
	}

}
