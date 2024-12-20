package uk.ac.napier.SET08103.model.db;

/**
 * Represents an enum column type from a database.
 * @param <T> A Java enum representing the database enum
 */
public interface IFieldEnum<T> {
    T getValue();
}
