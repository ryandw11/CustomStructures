package com.ryandw11.structure.threading;

import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CheckStructureList extends BukkitRunnable {

    public static final int MAX_STORED_STRUCTURES = 100;

    private final StructureHandler handler;

    public CheckStructureList(StructureHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        synchronized (handler.getSpawnedStructures()) {
            Set<Pair<Location, Long>> locationsToRemove = new HashSet<>();
            for (Map.Entry<Pair<Location, Long>, Structure> entry : handler.getSpawnedStructures().entrySet()) {
                if (System.currentTimeMillis() - entry.getKey().getRight() > 2.592e+8) {
                    locationsToRemove.add(entry.getKey());
                } else if (handler.getSpawnedStructures().size() - locationsToRemove.size() > MAX_STORED_STRUCTURES)
                    locationsToRemove.add(entry.getKey());
            }
            handler.getSpawnedStructures().keySet().removeAll(locationsToRemove);
        }
    }
}
