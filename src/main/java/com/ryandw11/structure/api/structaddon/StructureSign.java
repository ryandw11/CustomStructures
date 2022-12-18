package com.ryandw11.structure.api.structaddon;

import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.utils.NumberStylizer;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Define a custom Structure Sign by extending this class.
 *
 * <p>You can access the arguments by using the get...Argument methods.
 * Check if an argument exists by using {@link #hasArgument(int)}. The argument numbers correspond to the lines on a sign.
 * So argument 0 is the second line of the sign. That means there will always be 3 arguments available at maximum.
 * You are allowed to have an argument on line 2 and line 4 (skipping line 3). That just means {@link #hasArgument(int)}
 * will return false when you input 1 and true for 0 or 2.</p>
 * <br>
 * <code>
 * [SignName] <br>
 * Argument #0 <br>
 * Argument #1 <br>
 * Argument #2 <br>
 * </code>
 * <br>
 * <p>Do not add a constructor to your implementation.</p>
 *
 * <p>See {@link com.ryandw11.structure.schematic.structuresigns.MobSign} for an implementation example.</p>
 */
public abstract class StructureSign {
    private String[] arguments;
    private double signRotation;
    private double structureRotation;
    private Location structureMinimumLocation;
    private Location structureMaximumLocation;

    /**
     * <p>Do not overload this constructor.</p>
     */
    public StructureSign() {
    }

    /**
     * Initializes the structure signs with the required arguments.
     *
     * <p>Internal Use Only.</p>
     *
     * @param arguments                The string array of arguments.
     * @param signRotation             The rotation of the sign.
     * @param structureRotation        The rotation of the structure.
     * @param structureMinimumLocation The minimum location of structure schematic.
     * @param structureMaximumLocation The maximum location of the structure schematic.
     */
    public final void initialize(String[] arguments, double signRotation, double structureRotation, Location structureMinimumLocation, Location structureMaximumLocation) {
        this.arguments = arguments;
        this.signRotation = signRotation;
        this.structureRotation = structureRotation;
        this.structureMinimumLocation = structureMinimumLocation;
        this.structureMaximumLocation = structureMaximumLocation;
    }

    /**
     * The method that is called when a structure is spawned.
     *
     * <p>This method should be implemented by your implementation. Use the get...Argument methods to grab the
     * arguments on the signs.</p>
     *
     * @param location  The location of the sign.
     * @param structure The structure that was spawned.
     * @return True if the sign should be removed, false if it should stay.
     */
    public abstract boolean onStructureSpawn(@NotNull Location location, @NotNull Structure structure);

    /**
     * Get the rotation of the sign (in radians).
     *
     * @return The rotation of the sign in radians.
     */
    public final double getSignRotation() {
        return signRotation;
    }

    /**
     * Get the rotation of the structure (in radians).
     *
     * @return The rotation of the structure in radians.
     */
    public final double getStructureRotation() {
        return structureRotation;
    }

    /**
     * Get the minimum corner of the structure schematic.
     *
     * @return The minimum corner of the structure schematic.
     */
    public final Location getStructureMinimumLocation() {
        return structureMinimumLocation;
    }

    /**
     * Get the maximum corner of the structure schematic.
     *
     * @return The maximum corner of the structure schematic.
     */
    public final Location getStructureMaximumLocation() {
        return structureMaximumLocation;
    }

    /**
     * Check if a certain argument exists.
     *
     * @param argNumber The argument number. (0 - 2)
     * @return If the argument exists.
     */
    public final boolean hasArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return !arguments[argNumber].isEmpty();
    }

    /**
     * Get an argument as a String.
     *
     * @param argNumber The argument number. (0 - 2)
     * @return The argument as a String.
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final String getStringArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return arguments[argNumber];
    }

    /**
     * Get an argument as an Integer.
     *
     * @param argNumber The argument number. (0 - 2)
     * @return The argument as an Integer.
     * @throws NumberFormatException     If the argument is not a valid integer. (Use {@link #getIntArgument(int, int)}
     *                                   to avoid this exception).
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final int getIntArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);

        return Integer.parseInt(arguments[argNumber]);
    }

    /**
     * Get an argument as an Integer, but with a default value.
     *
     * @param argNumber    The argument number. (0 - 2)
     * @param defaultValue The default integer to be returned if the argument is not a valid integer.
     * @return The argument as an Integer.
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final int getIntArgument(int argNumber, int defaultValue) {
        try {
            return getIntArgument(argNumber);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    /**
     * Get an argument as a Double.
     *
     * @param argNumber The argument number. (0 - 2)
     * @return The argument as a Double.
     * @throws NumberFormatException     If the argument is not a valid Double.
     *                                   (Use {@link #getDoubleArgument(int, double)} to avoid this exception).
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final double getDoubleArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return Double.parseDouble(arguments[argNumber]);
    }

    /**
     * Get an argument as a Double, but with a default value.
     *
     * @param argNumber    The argument number. (0 - 2)
     * @param defaultValue The default value to be returned if the argument is not a valid Double.
     * @return The argument as a Double.
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final double getDoubleArgument(int argNumber, double defaultValue) {
        try {
            return getDoubleArgument(argNumber);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    /**
     * Get an argument as a Lower and Upper bound integer range.
     * <p>Example: <code>[5;10]</code></p>
     *
     * @param argNumber The argument number. (0 - 2).
     * @return The argument as a Lower and Upper bound integer range. (Left is lower, right is upper).
     * @throws NumberFormatException     If the argument is not formatted correctly.
     *                                   (Use {@link #getRangedIntArgument(int, int, int)} to avoid this exception.)
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final Pair<Integer, Integer> getRangedIntArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return NumberStylizer.parseRangedInput(arguments[argNumber]);
    }

    /**
     * Get an argument as a Lower and Upper bound integer range, but with a default range.
     * <p>Example: <code>[5;10]</code></p>
     *
     * @param argNumber         The argument number. (0 - 2)
     * @param defaultLowerValue The lower value of the default range.
     * @param defaultUpperValue The upper value of the default range.
     * @return The argument as a Lower and Upper bound integer range.
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final Pair<Integer, Integer> getRangedIntArgument(int argNumber, int defaultLowerValue, int defaultUpperValue) {
        try {
            return getRangedIntArgument(argNumber);
        } catch (NumberFormatException exception) {
            return Pair.of(defaultLowerValue, defaultUpperValue);
        }
    }

    /**
     * Get a random number from within a ranged integer argument.
     * <p>Example: <code>[5;10]</code>, might return 6.</p>
     *
     * @param argNumber The argument number. (0 - 2)
     * @return A random number from within a ranged integer argument.
     * @throws NumberFormatException     If the argument is not a valid integer range.
     *                                   (Use {@link #calculateRangedIntArgument(int, int)} to avoid this exception.)
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final int calculateRangedIntArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return NumberStylizer.retrieveRangedInput(arguments[argNumber]);
    }

    /**
     * Get a random number from within a ranged integer argument, but with a default value.
     * <p>Example: <code>[5;10]</code>, might return 6.</p>
     * <p><code>[10;4]</code> will return the default value since it is not a valid range.</p>
     *
     * @param argNumber    The argument number. (0 - 2)
     * @param defaultValue The default value to be used when the range is not valid.
     * @return A random number from within a ranged integer argument.
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final int calculateRangedIntArgument(int argNumber, int defaultValue) {
        try {
            return calculateRangedIntArgument(argNumber);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    /**
     * Get a stylized integer from an argument.
     * <p>This is the same as {@link #calculateRangedIntArgument(int)}, but it also accepts a single integer
     * on top of a range.</p>
     * <p>Valid Inputs:</p>
     * <code>
     * [5;10]<br>
     * [-4;3]<br>
     * 20<br>
     * -5<br>
     * </code>
     *
     * @param argNumber The argument number. (0 - 2)
     * @return The number represented by the stylized integer. (Defaults to 1 if not a valid format).
     * @throws IndexOutOfBoundsException If argNumber is not within the range of 0 to 2.
     */
    public final int getStylizedIntArgument(int argNumber) {
        Objects.checkIndex(argNumber, arguments.length);
        return NumberStylizer.getStylizedInt(arguments[argNumber]);
    }
}
