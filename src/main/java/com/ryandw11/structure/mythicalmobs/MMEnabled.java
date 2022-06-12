package com.ryandw11.structure.mythicalmobs;

import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;


public class MMEnabled implements MythicalMobHook {

    @Override
    public void spawnMob(String name, Location loc, int count) {
        Optional<MythicMob> mob = MythicProvider.get().getMobManager().getMythicMob(name);
        if (mob.isPresent()) {
            for (int i = 0; i < count; i++)
                mob.get().spawn(BukkitAdapter.adapt(loc), 1);
        } else
            Bukkit.getLogger().warning("Unknown Mythical Mob: " + name);
    }

    @Override
    public void spawnMob(String name, Location loc, double level, int count) {
        Optional<MythicMob> mob = MythicProvider.get().getMobManager().getMythicMob(name);
        if (mob.isPresent()) {
            for (int i = 0; i < count; i++)
                mob.get().spawn(BukkitAdapter.adapt(loc), level);
        } else
            Bukkit.getLogger().warning("Unknown Mythical Mob: " + name);
    }

}
