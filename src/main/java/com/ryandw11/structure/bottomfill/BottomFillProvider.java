package com.ryandw11.structure.bottomfill;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the plugin with an implementation of the BottomFill feature.
 */
public final class BottomFillProvider {
    private final static List<BottomFillImpl> providers = new ArrayList<>();

    /**
     * Add a BottomFill implementation to the provider.
     *
     * @param bottomFill The bottom fill implementation.
     * @deprecated Use {@link #addImplementation(BottomFillImpl)} instead.
     */
    @Deprecated
    public static void addProvider(BottomFillImpl bottomFill) {
        providers.add(bottomFill);
    }

    /**
     * Add a BottomFill implementation to the provider.
     *
     * @param bottomFill The bottom fill implementation.
     */
    public static void addImplementation(BottomFillImpl bottomFill) {
        providers.add(bottomFill);
    }

    /**
     * Get a bottom fill implementation.
     * <p>Note: Currently only the first implementation registered will be provided. This may change in the future.</p>
     *
     * @return The first implementation specified. (Or the default if no custom ones were added).
     */
    public static BottomFillImpl provide() {
        if (!providers.isEmpty())
            return providers.get(0);

        return new DefaultBottomFill();
    }

    /**
     * Get a bottom fill implementation
     *
     * @param impl The implementation number.
     * @return The implementation if it exists.
     */
    public static BottomFillImpl provide(int impl) {
        if (impl < providers.size()) {
            return providers.get(impl);
        }

        return new DefaultBottomFill();
    }
}
