package com.napier.SET08103.model.db;

/**
 * Represents an object that actually has a primary key field in the database,
 * or a record.
 * Intended for use in SQL string query building.
 */
public interface IEntity {
    String getPrimaryKey();
}
