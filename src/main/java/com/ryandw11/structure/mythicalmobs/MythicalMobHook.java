package com.ryandw11.structure.mythicalmobs;

import org.bukkit.Location;

public interface MythicalMobHook {
	void spawnMob(String name, Location loc, int count);
	void spawnMob(String name, Location loc, double level, int count);
}
