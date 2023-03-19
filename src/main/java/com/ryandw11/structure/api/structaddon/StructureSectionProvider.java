package com.ryandw11.structure.api.structaddon;

import org.jetbrains.annotations.NotNull;

/**
 * A provider to dynamically create and serve a structure section.
 *
 * <p>Using this is not recommended.</p>
 */
public interface StructureSectionProvider {
    /**
     * Creates StructureSection
     *
     * @return The StructureSection provided by this provider
     */
    @NotNull
    StructureSection createSection();
}
