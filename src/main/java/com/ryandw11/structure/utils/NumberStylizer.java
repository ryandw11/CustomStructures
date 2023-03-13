package com.ryandw11.structure.utils;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This is the system that allows for a random range or exact numbers to be inputted in the config.
 * <p>
 * Valid Inputs:
 * <p>
 * [4;10] --> Picks a random number between 4 and 10.
 * 10     --> The number 10.
 */
public final class NumberStylizer {

    /**
     * Allows for the input of a random range or an exact number.
     * <p>Valid Inputs:</p>
     *
     * <code>
     * [4;10]<br>
     * 10
     * </code>
     *
     * @param input The String input.
     * @return The number. (If invalid 1 is returned).
     */
    public static int getStylizedInt(String input) {
        // [20;25]
        if (input.contains(";")) {
            String v = input.replace("[", "").replace("]", "");
            String[] out = v.split(";");
            try {
                int num1 = Integer.parseInt(out[0]);
                int num2 = Integer.parseInt(out[1]);

                return ThreadLocalRandom.current().nextInt(num1, num2 + 1);

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                return 1;
            }

        }
        // if just num
        else {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                return 1;
            }
        }
    }

    /**
     * Parse an int range into the lower and upper value.
     *
     * <p>Example Input: [4; 10]</p>
     *
     * @param input The String input.
     * @return The Pair containing the lower and upper integers of the range.
     * @throws NumberFormatException If the format is not valid or if the first number
     *                               is greater than the second number.
     */
    public static Pair<Integer, Integer> parseRangedInput(String input) {
        // [20;25]
        if (input.contains(";")) {
            String v = input.replace("[", "").replace("]", "");
            String[] out = v.split(";");
            try {
                int num1 = Integer.parseInt(out[0]);
                int num2 = Integer.parseInt(out[1]);

                if (num1 > num2) {
                    throw new NumberFormatException("Invalid Number Format: Number 1 is greater than Number 2!");
                }

                return Pair.of(num1, num2);

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                throw new NumberFormatException("Invalid Number Format: Input is not a range.");
            }

        } else {
            throw new NumberFormatException("Invalid Number Format: Input is not a range.");
        }
    }

    /**
     * Parse an int range and pick a value at random.
     *
     * <p>Example Input: [4; 10]</p>
     *
     * @param input The String input.
     * @return The picked value.
     * @throws NumberFormatException If the format is not valid or if the first number
     *                               is greater than the second number.
     */
    public static int retrieveRangedInput(String input) {
        // [20;25]
        if (input.contains(";")) {
            String v = input.replace("[", "").replace("]", "");
            String[] out = v.split(";");
            try {
                int num1 = Integer.parseInt(out[0]);
                int num2 = Integer.parseInt(out[1]);

                if (num1 > num2) {
                    throw new NumberFormatException("Invalid Number Format: Number 1 is greater than Number 2!");
                }

                return ThreadLocalRandom.current().nextInt(num1, num2 + 1);

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                throw new NumberFormatException("Invalid Number Format: Input is not a range.");
            }

        } else {
            throw new NumberFormatException("Invalid Number Format: Input is not a range.");
        }
    }

    /**
     * Stylize a SpawnY Value.
     *
     * @param value    The SpawnY Value.
     * @param location The location of the "top" block.
     * @return The location of the top block.
     */
    public static int getStylizedSpawnY(String value, @Nullable Location location) {
        boolean top = value.equalsIgnoreCase("top");

        // Ensure that the spawnY is configured correctly for the void.
        if (location == null) {
            if (top) throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                    "absolute spawn y value. Top is not absolute.");
            if (value.startsWith("+"))
                throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                        "absolute spawn y value. Relative value is not absolute.");
            if (value.startsWith("-"))
                throw new StructureConfigurationException("A structure that can spawn in the void must have an " +
                        "absolute spawn y value. Relative value is not absolute.");
        }

        // Get the highest block at the specified location.
        int currentHeight = -1;
        if (location != null) {
            currentHeight = location.getBlockY();
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

                    if (num1 > num2)
                        throw new StructureConfigurationException("SpawnY Value 1 must be greater than value 2 in '[value1;value2]'.");

                    int randomValue = ThreadLocalRandom.current().nextInt(num1, num2 + 1);
                    return currentHeight + randomValue;

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

                    if (num1 > num2)
                        throw new StructureConfigurationException("SpawnY Value 1 must be greater than value 2 in '[value1;value2]'.");

                    int randomValue = ThreadLocalRandom.current().nextInt(num1, num2 + 1);
                    return currentHeight - randomValue;

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

                    if (num1 > num2)
                        throw new StructureConfigurationException("SpawnY Value 1 must be greater than value 2 in '[value1;value2]'.");

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
