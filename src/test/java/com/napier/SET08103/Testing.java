package com.napier.SET08103;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Constants and helper methods relating to unit and integration tests
 */
public final class Testing {
    private Testing() { }

    // Test constants
    public static final String MYSQL_HOST_ENVAR = "MYSQL_HOST";
    public static final String MYSQL_ROOT_PASSWORD_ENVAR = "MYSQL_ROOT_PASSWORD";
    public static final String MYSQL_HOST_ENVAR_DEFAULT = "localhost";
    public static final String MYSQL_ROOT_PASSWORD_DEFAULT = "root";

    // Copied from https://stackoverflow.com/questions/13501142/java-arraylist-how-can-i-tell-if-two-lists-are-equal-order-not-mattering/13501200#13501200
    // Easy way of comparing two lists, insensitive to ordering, but sensitive to element frequency.
    // Useful for database tests (where order shouldn't matter, but there can be duplicate entries
    // depending on the query).
    // I have modified the code to make it work with generic types.
    public static <T extends Comparable<T>> boolean compareLists(List<T> one, List<T> two) {
        if (one == null && two == null) {
            return true;
        }

        if ((one == null && two != null)
                || one != null && two == null
                || one.size() != two.size()) {
            return false;
        }

        //to avoid messing the order of the lists we will use a copy
        //as noted in comments by A. R. S.
        one = new ArrayList<>(one);
        two = new ArrayList<>(two);

        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }
}
