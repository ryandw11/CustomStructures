package com.ryandw11.structure.exceptions;

/**
 * This exception occurs when the plugin cannot access needed files.
 */
public class StructureReadWriteException extends RuntimeException{
    public StructureReadWriteException(String message) {
        super(message);
    }
}
