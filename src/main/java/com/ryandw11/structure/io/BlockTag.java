package com.ryandw11.structure.io;

import me.ryandw11.ods.Tag;
import me.ryandw11.ods.tags.IntTag;
import me.ryandw11.ods.tags.ObjectTag;
import me.ryandw11.ods.tags.StringTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;

/**
 * Stores information about a block in the ODS file format.
 */
public class BlockTag extends ObjectTag {
    public BlockTag(String name, List<Tag<?>> value) {
        super(name, value);
    }

    public BlockTag(String name) {
        super(name);
    }

    public BlockTag(Material type, Location location){
        super(location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ());
        this.addTag(new StringTag("type", type.name()));
        this.addTag(new IntTag("x", location.getBlockX()));
        this.addTag(new IntTag("y", location.getBlockY()));
        this.addTag(new IntTag("z", location.getBlockZ()));
    }

    public BlockTag(ObjectTag objTag){
        super(objTag.getTag("x").getValue() + ";" + objTag.getTag("y").getValue() + ";" + objTag.getTag("z"));
        this.addTag(objTag.getTag("type"));
        this.addTag(objTag.getTag("x"));
        this.addTag(objTag.getTag("y"));
        this.addTag(objTag.getTag("z"));
    }

    public Location getLocation(){
        IntTag x = (IntTag) this.getTag("x");
        IntTag y = (IntTag) this.getTag("y");
        IntTag z = (IntTag) this.getTag("z");
        return new Location(null, x.getValue(), y.getValue(), z.getValue());
    }

    public Location getLocation(World w){
        IntTag x = (IntTag) this.getTag("x");
        IntTag y = (IntTag) this.getTag("y");
        IntTag z = (IntTag) this.getTag("z");
        return new Location(w, x.getValue(), y.getValue(), z.getValue());
    }

    public Material getType(){
        StringTag tag = (StringTag) this.getTag("type");
        return Material.valueOf(tag.getValue());
    }
}
