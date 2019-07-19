package com.ryandw11.structure.mythicalmobs;

import org.bukkit.Location;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MobManager;

public class MMEnabled implements MythicalMobHook {

	@Override
	public void spawnMob(String name, Location loc) {
		MobManager mobManager = MythicMobs.inst().getMobManager();
		mobManager.spawnMob(name, loc);
	}

}
