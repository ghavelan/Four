package be.ghavelan.fastfill;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class stores several methods, enums and constants.
 */
public class Utility {

    /**
     * Returns true if the 2 points in argument have the same coordinates.
     *
     * @param A First point.
     * @param B Second point.
     * @return True or false.
     */
    public static boolean isEquals(Point A, Point B) {

        return A.x == B.x && A.y == B.y;

    }

    /**
     * Generates a random integer uniformly distributed between 2 numbers.
     *
     * @param min Minimum.
     * @param max Maximum.
     * @return A random integer between min (inclusive) and max (inclusive).
     */
    public static int randInt(int min, int max, Random rand) {

        /*nextInt select an integer between 0 (inclusive) and its argument (exclusive) --> add 1*/
        return min + rand.nextInt((max - min) + 1);

    }

    /**
     * Generates required random integers uniformly distributed between 0 and integerRange (exclusive).
     *
     * @param integerRange Range of integers.
     * @param required     Number of random integers to chose.
     * @return A set of random integers.
     */
    public static int[] getRandomInRange(int integerRange, int required) {

        ArrayList<Integer> array = new ArrayList<>();
        int[] numbersPicked = new int[required];
        for (int j = 0; j < integerRange; j++) {
            array.add(j);
        }
        Collections.shuffle(array);
        for (int j = 0; j < required; j++) {
            numbersPicked[j] = array.get(j);
        }

        return numbersPicked;

    }

    //Possible orientations for a line
    public enum Orientation {
        HORIZONTAL, VERTICAL, OBLIQUE
    }

}
