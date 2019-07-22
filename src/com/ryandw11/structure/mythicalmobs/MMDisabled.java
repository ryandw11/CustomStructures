package com.ryandw11.structure.mythicalmobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class MMDisabled implements MythicalMobHook{

	@Override
	public void spawnMob(String name, Location loc) {
		Bukkit.getLogger().info("A schematic tried to spawn a MythicalMob! This server does not have the plugin installed!");
		return;
	}

}
