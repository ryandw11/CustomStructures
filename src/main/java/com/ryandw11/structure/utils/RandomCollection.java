package com.ryandw11.structure.utils;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * A collection where the next value can be obtained at Random.
 *
 * <p>The way this works is that each item has a weight. Everytime an item is
 * added to the collection, the weight is added to the total weight value of the collection.
 * When the {@link #next()} method is called is calculates the probability of each item by
 * the weight/total_weight.</p>
 *
 * <p>So if one item has a weight of 10 and another of 20, the total weight is 30. The first item has a
 * 33% chance of being selected while the second item has a 66% of spawning. If a third item is added with a weight
 * of 5, these would be the probability table:</p>
 * <table>
 *     <tr>
 *         <th>Item One</th>
 *         <th>Item Two</th>
 *         <th>Item Three</th>
 *         <th>Total Weight</th>
 *     </tr>
 *     <tr>
 *         <td>10/35 = 28.6%</td>
 *         <td>20/35 = 57.7%</td>
 *         <td>5/35 = 16.7%</td>
 *         <td>35</td>
 *     </tr>
 * </table>
 *
 * @param <E> The type of collection.
 */
public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    /**
     * Construct the random collection with a new random.
     */
    public RandomCollection() {
        this(new Random());
    }

    /**
     * Construct random collection with an existing random.
     *
     * @param random The random.
     */
    public RandomCollection(Random random) {
        this.random = random;
    }

    /**
     * Add a value to the RandomCollection.
     *
     * <p>View the class description for a detailed explanation of how the
     * probability of this collection works.</p>
     *
     * @param weight The weight of the item.
     * @param result The item to add.
     * @return The instance of this collection.
     */
    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0)
            return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    /**
     * Get the next value randomly based upon defined probabilities.
     *
     * <p>View the class description for a detailed explanation of how the
     * probability of this collection works.</p>
     *
     * @return The next value.
     */
    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }

    /**
     * Check if the collection is empty.
     *
     * @return If the collection is empty.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Get the internal map.
     *
     * @return The internal map.
     */
    public Map<Double, E> getMap() {
        return map;
    }
}