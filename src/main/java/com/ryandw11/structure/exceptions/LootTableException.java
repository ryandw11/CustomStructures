package com.ryandw11.structure.exceptions;

/**
 * This is thrown when there is an error with a loot table.
 */
public class LootTableException extends RuntimeException {
    public LootTableException(String message){
        super(message);
    }
}
