package com.ryandw11.structure.structure.properties;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This handles the SpawnY of a structure.
 */
public class StructureYSpawning {

    private boolean top = false;
    private boolean oceanFloor = false;
    private String value;

    /**
     * Get SpawnY from a configuration file.
     * @param fc The file configuration.
     */
    public StructureYSpawning(FileConfiguration fc){
        value = fc.getString("StructureLocation.SpawnY");

        assert value != null;
        if(value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if(value.equalsIgnoreCase("top"))
            top = true;
    }

    /**
     * Set the StructureYSpawning with a value.
     * @param value The value of SpawnY.
     */
    public StructureYSpawning(String value){
        this.value = value;
        if(value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if(value.equalsIgnoreCase("top"))
            top = true;
    }

    /**
     * Get the raw value of SpawnY.
     * @return The raw value of SpawnY.
     */
    public String getValue(){
        return value;
    }

    /**
     * Get if the structure should spawn on the top.
     * @return If the structure should spawn on the top.
     */
    public boolean isTop() {
        return top;
    }

    /**
     * Get if the structure should spawn on the ocean floor.
     * @return If the structure should spawn on the floor.
     */
    public boolean isOceanFloor(){
        return oceanFloor;
    }

    /**
     * Get the height from SpawnY value.
     * @param currentHeight The current height (This is usually the highest block y in the chunk).
     *                      <p>AKA: What value top should return.</p>
     * @return The height according to the rules of SpawnY
     */
    public int getHeight (int currentHeight) {
        if(top) return currentHeight;
        if(value.contains("[")) {
            //If +[num-num]
            if(value.startsWith("+")) {
                String v = value.replace("[", "").replace("]", "").replace("+", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    Random r = new Random();

                    int a = r.nextInt(num2) + (num1 + 1);
                    return currentHeight + a;

                }catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
            // if -[num-num]
            else if(value.startsWith("-")) {
                String v = value.replace("[", "").replace("]", "").replace("-", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    Random r = new Random();

                    int a = r.nextInt(num2) + (num1 + 1);
                    return currentHeight - a;

                }catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
            // if just [num-num]
            else {
                String v = value.replace("[", "").replace("]", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    return ThreadLocalRandom.current().nextInt(num1, num2 + 1);

                }catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
        }
        // if +num
        else if(value.startsWith("+")) {
            String v = value.replace("+", "");
            try {
                int num = Integer.parseInt(v);
                return currentHeight + num;
            }catch(NumberFormatException ex) {
                return currentHeight;
            }
        }
        // if -num
        else if(value.startsWith("-")) {
            String v = value.replace("-", "");

            try {
                int num = Integer.parseInt(v);
                return currentHeight - num;
            }catch(NumberFormatException ex) {
                return currentHeight;
            }
        }
        // if just num
        else {
            try {
                return Integer.parseInt(value);
            } catch(NumberFormatException ex) {
                return currentHeight;
            }
        }
    }
}
