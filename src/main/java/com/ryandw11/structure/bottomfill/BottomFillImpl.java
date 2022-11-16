package com.ryandw11.structure.bottomfill;

import com.ryandw11.structure.structure.Structure;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.Location;

/**
 * The interface for bottom fill implementations.
 *
 * <p>Use the {@link BottomFillProvider} to get the correct implementation and
 * register a new one.</p>
 */
public interface BottomFillImpl {
    /**
     * Called by the plugin when a bottom fill should be performed.
     *
     * <p>This will only be called if the BottomFill option is enabled.</p>
     *
     * @param structure     The structure that was spawned.
     * @param spawnLocation The spawn (paste) location of the structure.
     * @param minLoc        The minimum location of the structure in the world to paste onto.
     * @param maxLoc        The maximum location of the structure in the world to paste onto.
     * @deprecated Use
     * {@link #performFill(com.ryandw11.structure.structure.Structure, org.bukkit.Location, org.bukkit.Location,
     * org.bukkit.Location, com.sk89q.worldedit.math.transform.AffineTransform)} instead. This method does not factor in
     * a random rotation.
     */
    @Deprecated
    void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc);

    /**
     * Called by the plugin when a bottom fill should be performed.
     *
     * <p>This will only be called if the BottomFill option is enabled.</p>
     *
     * @param structure     The structure that was spawned.
     * @param spawnLocation The spawn (paste) location of the structure.
     * @param minLoc        The minimum location of the structure in the world to paste onto.
     * @param maxLoc        The maximum location of the structure in the world to paste onto.
     * @param transform     The affine transformation applied on this structure.
     */
    void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc, AffineTransform transform);
}
