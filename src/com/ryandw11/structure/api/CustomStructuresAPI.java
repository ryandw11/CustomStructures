package com.ryandw11.structure.api;

import com.ryandw11.structure.CustomStructures;

public class CustomStructuresAPI {


	public CustomStructures getMainInstance() {
		return CustomStructures.plugin;
	}
	
	/**
	 * Get the number of structures.
	 * @return The number of structures.
	 */
	public int getNumberOfStructures() {
		return CustomStructures.getInstance().getStructureHandler().getStructures().size();
	}
}
