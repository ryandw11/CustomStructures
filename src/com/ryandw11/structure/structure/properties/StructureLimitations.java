package com.ryandw11.structure.structure.properties;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class StructureLimitations {

    private List<String> whitelistSpawnBlocks;

    public StructureLimitations(FileConfiguration configuration){
        if(!configuration.contains("StructureLimitations.whitelistSpawnBlocks"))
            whitelistSpawnBlocks = new ArrayList<>();
        else
            whitelistSpawnBlocks = configuration.getStringList("StructureLimitations.whitelistSpawnBlocks");
    }

    public StructureLimitations(List<String> whitelistSpawnBlocks){
        this.whitelistSpawnBlocks = whitelistSpawnBlocks;
    }

    public List<String> getWhitelistBlocks(){
        return whitelistSpawnBlocks;
    }

    public boolean hasBlock(Block b){
        if(whitelistSpawnBlocks.isEmpty()) return true;
        for(String block : whitelistSpawnBlocks){
            if(block.equalsIgnoreCase(b.getType().toString()))
                return true;
        }
        return false;
    }
}
