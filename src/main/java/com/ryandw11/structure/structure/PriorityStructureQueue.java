package com.ryandw11.structure.structure;

import com.ryandw11.structure.structure.properties.StructureYSpawning;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Maintains a priority queue of structures for a spawn location.
 */
public class PriorityStructureQueue {

    private final TreeSet<PriorityStructure> priorityStructureSet;

    /**
     * Create a priority queue of structures for a specific spawn location.
     * <p>{@link Structure#canSpawn(Block, Chunk)} must return true for the specified block
     * and chunk for it to be added to the queue.</p>
     *
     * @param structures The list of structures to attempt and add to the queue.
     * @param block      The block to test the spawn conditions for.
     * @param chunk      The chunk to test the spawn conditions for.
     */
    public PriorityStructureQueue(@NotNull List<Structure> structures, @NotNull Block block, @NotNull Chunk chunk) {
        priorityStructureSet = new TreeSet<>();

        for (Structure structure : structures) {
            StructureYSpawning structureSpawnSettings = structure.getStructureLocation().getSpawnSettings();

            // Get the highest block according to the settings for the structure.
            Block structureBlock = structureSpawnSettings.getHighestBlock(block.getLocation());
            if (structureBlock.getType() == Material.VOID_AIR) {
                structureBlock = null;
            }

            if (structure.canSpawn(structureBlock, chunk)) {
                priorityStructureSet.add(new PriorityStructure(structure));
            }
        }
    }

    /**
     * Check if there is a next structure to retrieve from the priority queue.
     *
     * @return If there is a next structure to retrieve.
     */
    public boolean hasNextStructure() {
        return !priorityStructureSet.isEmpty();
    }

    /**
     * Get the next structure from the priority queue.
     *
     * @return The next structure. Null if queue empty.
     */
    @Nullable
    public Structure getNextStructure() {
        if (priorityStructureSet.isEmpty()) {
            return null;
        }

        return Objects.requireNonNull(priorityStructureSet.pollFirst()).getStructure();
    }

    /**
     * The class that is used to store a structure and probability data.
     *
     * <p>Note: this class has a natural ordering that is inconsistent with equals.</p>
     */
    private static class PriorityStructure implements Comparable<PriorityStructure> {
        private final Structure structure;
        private final double probability;

        public PriorityStructure(@NotNull Structure structure) {
            this.structure = structure;
            this.probability = (double) structure.getProbabilityNumerator() / (double) structure.getProbabilityDenominator();
        }

        /**
         * Get a structure contained within this object.
         *
         * @return The structure contained within this object.
         */
        @NotNull
        public Structure getStructure() {
            return structure;
        }

        @Override
        public int compareTo(@NotNull PriorityStructureQueue.PriorityStructure pStructure) {
            if (structure.getPriority() == pStructure.structure.getPriority()) {
                return probability < pStructure.probability ? -1 : 1;
            }
            return structure.getPriority() < pStructure.structure.getPriority() ? -1 : 1;
        }
    }
}
