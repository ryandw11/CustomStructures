package com.ryandw11.structure.mythicalmobs;

import org.bukkit.Location;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.MobExecutor;

public class MMEnabled implements MythicalMobHook {

	@Override
	public void spawnMob(String name, Location loc) {
		MobExecutor mobManager = MythicBukkit.inst().getMobManager();
		mobManager.spawnMob(name, loc);
	}

	@Override
	public void spawnMob(String name, Location loc, int level) {
		MobExecutor mobManager = MythicBukkit.inst().getMobManager();
		mobManager.spawnMob(name, loc, level);
	}

}
