package com.ryandw11.structure.exceptions;

/**
 * An exception that occurs when a structure is configured incorrectly.
 *
 * @since 1.5.6
 */
public class StructureConfigurationException extends RuntimeException {
    public StructureConfigurationException(String message) {
        super(message);
    }
}
