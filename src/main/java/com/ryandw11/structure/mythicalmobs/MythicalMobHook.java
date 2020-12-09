package com.ryandw11.structure.mythicalmobs;

import org.bukkit.Location;

public interface MythicalMobHook {
	void spawnMob(String name, Location loc);
	void spawnMob(String name, Location loc, int level);
}
