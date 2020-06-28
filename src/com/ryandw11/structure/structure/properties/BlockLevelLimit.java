package com.ryandw11.structure.structure.properties;

import org.bukkit.configuration.file.FileConfiguration;

public class BlockLevelLimit {
    private String mode;

    private int x1, z1, x2, z2;

    public BlockLevelLimit(String mode, int x1, int z1, int x2, int z2){
        this.mode = mode;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    public BlockLevelLimit(){
        mode = "NONE";
    }

    public BlockLevelLimit(FileConfiguration fileConfiguration){
        if(!fileConfiguration.contains("StructureLimitations.BlockLevelLimit")){
            mode = "NONE";
            return;
        }

        this.mode = fileConfiguration.getString("StructureLimitations.BlockLevelLimit.mode");
        this.x1 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerOne.x");
        this.z1 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerOne.z");
        this.x2 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerTwo.x");
        this.z2 = fileConfiguration.getInt("StructureLimitations.BlockLevelLimit.cornerTwo.z");
    }

    public boolean isEnabled(){
        return mode.equalsIgnoreCase("none");
    }

    public String getMode(){
        return mode;
    }

    public int getX1(){
        return x1;
    }

    public int getX2(){
        return x2;
    }

    public int getZ1(){
        return z1;
    }

    public int getZ2(){
        return z2;
    }
}
