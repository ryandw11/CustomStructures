package com.ryandw11.structure.bottomfill;

import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;

/**
 * The interface for bottom fill implementations.
 *
 * <p>Use the {@link BottomFillProvider} to get the correct implementation and register a new one.</p>
 */
public interface BottomFillImpl {
    /**
     * Called by the plugin when a bottom fill should be performed.
     *
     * <p>This will only be called if the BottomFill option is enabled.</p>
     *
     * @param structure The structure that was spawned.
     * @param spawnLocation The spawn location.
     * @param minLoc The minimum location.
     * @param maxLoc The maximum location.
     */
    void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc);
}
