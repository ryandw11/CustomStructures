package com.ryandw11.structure.structure.properties;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class StructureYSpawning {

    private boolean top = false;
    private boolean oceanFloor = false;
    private String value;

    public StructureYSpawning(FileConfiguration fc){
        value = fc.getString("StructureLocation.SpawnY");

        assert value != null;
        if(value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if(value.equalsIgnoreCase("top"))
            top = true;
    }

    public StructureYSpawning(String value){
        this.value = value;
        if(value.equalsIgnoreCase("ocean_floor"))
            oceanFloor = true;
        else if(value.equalsIgnoreCase("top"))
            top = true;
    }

    public String getValue(){
        return value;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isOceanFloor(){
        return oceanFloor;
    }

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

                    //Random r = new Random();
                    //return r.nextInt(num2 + 1) + num1;

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
