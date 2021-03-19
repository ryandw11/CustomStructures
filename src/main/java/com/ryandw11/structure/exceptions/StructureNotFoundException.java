package com.ryandw11.structure.exceptions;

/**
 * This exception occurs when a structure was not found.
 */
public class StructureNotFoundException extends RuntimeException{
    public StructureNotFoundException(String message) {
        super(message);
    }
}
