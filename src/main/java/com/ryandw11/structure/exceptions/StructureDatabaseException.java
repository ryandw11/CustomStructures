package com.ryandw11.structure.exceptions;

/**
 * This exception occurs when a database error occurs relating to the structure database.
 */
public class StructureDatabaseException extends RuntimeException {
    public StructureDatabaseException(String message) {
        super(message);
    }
}
