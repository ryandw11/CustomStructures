package com.ryandw11.structure.utils;

/**
 * A utility class for SpawnY conversion for the 1.17 update.
 */
public final class SpawnYConversion {

    /**
     * Convert the SpawnY Value into the new format.
     *
     * @param value The SpawnY value to convert.
     * @return The converted SpawnY value.
     */
    public static String convertSpawnYValue(String value) {
        if (value.equalsIgnoreCase("top")) return "top";
        // Ocean_floor is returned and the actual update to the new format is done in the main updater code.
        if (value.equalsIgnoreCase("ocean_floor")) return "ocean_floor";
        if (value.contains("[")) {
            //If +[num-num]
            if (value.startsWith("+")) {
                String v = value.replace("[", "").replace("]", "").replace("+", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    return String.format("+[%s;%s]", num1, num2);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
                }
            }
            // if -[num-num]
            else if (value.startsWith("-")) {
                String v = value.replace("[", "").replace("]", "").replace("-", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    return String.format("-[%s;%s]", num1, num2);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
                }
            }
            // if just [num-num]
            else {
                String v = value.replace("[", "").replace("]", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);

                    return String.format("[%s;%s]", num1, num2);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
                }
            }
        }
        // if +num
        else if (value.startsWith("+")) {
            String v = value.replace("+", "");
            try {
                return String.format("+[%s]", v);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
            }
        }
        // if -num
        else if (value.startsWith("-")) {
            String v = value.replace("-", "");

            try {
                int num = Integer.parseInt(v);
                return String.format("-[%s]", num);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
            }
        }
        // if just num
        else {
            try {
                return value;
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert SpawnY value since it is invalid!");
            }
        }
    }
}
