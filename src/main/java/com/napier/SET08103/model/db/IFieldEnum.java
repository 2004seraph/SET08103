package com.napier.SET08103.model.db;

import java.util.List;

/**
 * Represents an enum column type from a database.
 * @param <T> A Java enum representing the database enum
 */
public interface IFieldEnum<T> {
    public T getValue();
}
