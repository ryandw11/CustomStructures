package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This handles the SpawnY of a structure.
 */
public class StructureYSpawning {

    private boolean top = false;
    private boolean oceanFloor = false;
    private boolean calculateSpawnYFirst = true;
    private final String value;

    /**
     * Get SpawnY from a configuration file.
     *
     * @param fc The file configuration.
     */
    public StructureYSpawning(FileConfiguration fc) {
        if (!fc.contains("StructureLocation.SpawnY"))
            throw new StructureConfigurationException("The structure must have a YSpawn value!");

        value = fc.getString("StructureLocation.SpawnY");

        assert value != null;
        if (value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if (value.equalsIgnoreCase("top"))
            top = true;

        if (fc.contains("StructureLocation.CalculateSpawnFirst")) {
            calculateSpawnYFirst = fc.getBoolean("StructureLocation.CalculateSpawnFirst");
        }
    }

    /**
     * Set the StructureYSpawning with a value.
     *
     * @param value                The value of SpawnY.
     * @param calculateSpawnYFirst If you want the SpawnY to be calculated before the other checks are completed (ex: block whitelist).
     */
    public StructureYSpawning(String value, boolean calculateSpawnYFirst) {
        this.value = value;
        if (value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if (value.equalsIgnoreCase("top"))
            top = true;
        this.calculateSpawnYFirst = calculateSpawnYFirst;
    }

    /**
     * Get the raw value of SpawnY.
     *
     * @return The raw value of SpawnY.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get if the structure should spawn on the top.
     *
     * @return If the structure should spawn on the top.
     */
    public boolean isTop() {
        return top;
    }

    /**
     * Get if the structure should spawn on the ocean floor.
     *
     * @return If the structure should spawn on the floor.
     */
    public boolean isOceanFloor() {
        return oceanFloor;
    }

    /**
     * Get if the structure should calculate SpawnY first.
     *
     * @return If the structure should calculate SpawnY first.
     */
    public boolean isCalculateSpawnYFirst() {
        return calculateSpawnYFirst;
    }

    /**
     * Get the height from SpawnY value.
     *
     * @param currentHeight The current height (This is usually the highest block y in the chunk).
     *                      <p>AKA: What value top should return.</p>
     *                      <p>If this is -1, than that means the structure is spawning in the void.</p>
     * @return The height according to the rules of SpawnY
     */
    // TODO this needs to be fixed.
    public int getHeight(int currentHeight) {

        // Ensure that the spawnY is configured correctly for the void.
        if(currentHeight == -1){
            if(top) throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                    "absolute spawn y value. Top is not absolute.");
            if(oceanFloor) throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                    "absolute spawn y value. Ocean Floor is not absolute.");
            if(value.startsWith("+")) throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                    "absolute spawn y value. Relative value is not absolute.");
            if(value.startsWith("-")) throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                    "absolute spawn y value. Relative value is not absolute.");
        }

        if (top) return currentHeight;
        // If it is a range
        if (value.contains(";")) {
            //If +[num;num]
            if (value.startsWith("+")) {
                String v = value.replace("[", "").replace("]", "").replace("+", "");
                String[] out = v.split(";");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    Random r = new Random();

                    int a = r.nextInt(num2) + (num1 + 1);
                    return currentHeight + a;

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
            // if -[num;num]
            else if (value.startsWith("-")) {
                String v = value.replace("[", "").replace("]", "").replace("-", "");
                String[] out = v.split(";");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    Random r = new Random();

                    int a = r.nextInt(num2) + (num1 + 1);
                    return currentHeight - a;

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
            // if just [num;num]
            else {
                String v = value.replace("[", "").replace("]", "");
                String[] out = v.split(";");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    return ThreadLocalRandom.current().nextInt(num1, num2 + 1);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    return currentHeight;
                }
            }
        }
        // if +[num]
        else if (value.startsWith("+[")) {
            String v = value.replace("+", "").replace("[", "").replace("]", "");
            try {
                int num = Integer.parseInt(v);
                return currentHeight + num;
            } catch (NumberFormatException ex) {
                return currentHeight;
            }
        }
        // if -[num]
        else if (value.startsWith("-[")) {
            String v = value.replace("-", "").replace("[", "").replace("]", "");

            try {
                int num = Integer.parseInt(v);
                return currentHeight - num;
            } catch (NumberFormatException ex) {
                return currentHeight;
            }
        }
        // if just num
        else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return currentHeight;
            }
        }
    }
}
