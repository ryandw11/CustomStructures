package com.ryandw11.structure.structure.properties.schematics;

import org.bukkit.configuration.ConfigurationSection;

public class SubSchematic {

    private String file = "";
    private boolean placeAir = false;
    public SubSchematic(ConfigurationSection section){
        if(!section.contains("file"))
            throw new RuntimeException("Format Error: " + section.getName() + " does not contain a file!");
        file = section.getString("file");
        if(section.contains("PlaceAir"))
            placeAir = section.getBoolean("PlaceAir");
    }

    public void setPlaceAir(boolean placeAir){
        this.placeAir = placeAir;
    }

    public boolean isPlacingAir(){
        return placeAir;
    }

    public void setFile(String file){
        this.file = file;
    }

    public String getFile(){
        return file;
    }
}
