package uk.ac.napier.SET08103.model;

/**
 * Helper class for the model package
 */
public final class Model {
    private Model() { }

    /**
     * Takes a list of String arguments and concatenates them, purely for code
     * readability since you won't have all those ugly concatenations in your queries
     * @param strings varargs
     * @return a string with spaces automatically inserted between each argument
     */
    public static String buildStatement(final String ... strings) {
        final StringBuilder query = new StringBuilder();
        for (String s : strings)
            query.append(s).append(" ");

        return query.toString();
    }
}
