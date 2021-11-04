package com.ryandw11.structure.api.structaddon;

import org.jetbrains.annotations.NotNull;

public interface StructureSectionProvider {
    /**
     * Creates StructureSection
     * @return The StructureSection provided by this provider
     */
    @NotNull
    StructureSection createSection();
}
