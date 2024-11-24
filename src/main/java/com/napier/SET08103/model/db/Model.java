package com.napier.SET08103.model.db;

public final class Model {
    private Model() { }

    public static String buildStatement(String ... strings) {
        StringBuilder query = new StringBuilder();
        for (String s : strings)
            query.append(s).append(" ");

        return query.toString();
    }
}
