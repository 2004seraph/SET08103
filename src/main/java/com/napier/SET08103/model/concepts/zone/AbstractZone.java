package com.napier.SET08103.model.concepts.zone;

import com.napier.SET08103.model.db.IEntity;
import com.napier.SET08103.model.db.IFieldEnum;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractZone implements IZone {

    /**
     * This cache spans different instances of the same underlying zone
     * so creating the key for the ASIA continent on one instance will make its
     * cache available to other continent instances of ASIA.
     *
     * This also means that if one calls Asia.getCities(), it will preload the cache
     * with all the intermediate zones required to retrieve such data.
     */
    protected static final HashMap<String, List<IZone>> cacheMap = new HashMap<>(5);

    /**
     * Converts a List<City or Continent or Country, etc> to a LIst<IZone>
     * @param concreteIZones List of a zone type
     * @return All collapsed to the IZone type
     * @param <T> City, District, Country, Region, Continent, or World
     */
    protected static <T> List<IZone> wrapIZone(List<T> concreteIZones) {
        return concreteIZones.stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    /**
     * Converts a List<IZone> to a given target type (Zone)
     * @param concreteIZones list of IZones
     * @return
     * @param <T> City, District, Country, Region, Continent, or World
     */
    @SuppressWarnings("unchecked")
    protected static <T extends IZone> List<T> unwrapIZone(List<IZone> concreteIZones) {
        return concreteIZones.stream().map(c -> (T)c).collect(Collectors.toList());
    }

    /**
     * Flattens a list of all descendant zones of the given distance in levels from this zone instance
     * 
     * For example:
     * districtInstance.getInnerZones(1, conn) -> List<City>,
     * continentInstance.getInnerZones(3, conn) -> List<District>
     * continentInstance.getInnerZones(4, conn) -> List<City>
     * @param traverseDown How many tree levels down from the current one to traverse
     * @param conn Database connection
     * @return A List of all zone instances from that level
     * @throws SQLException
     */
    public List<IZone> getInnerZones(int traverseDown, Connection conn) throws SQLException {
        if (traverseDown > getZoneLevel().getSizeRank())
            throw new RuntimeException("Zone traversal too deep");

        List<IZone> treeExpansion = getInnerZones(conn);

        for (int i = 0; i < traverseDown - 1; i++) {
            treeExpansion = treeExpansion.stream().flatMap(z -> {
                try {
                    return z.getInnerZones(conn).stream();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }

        return treeExpansion;
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || (getClass() != other.getClass()))
            return false; // City will never == Country, despite both potentially having the same primary key

        AbstractZone otherZone = (AbstractZone)other;
        if (otherZone instanceof IFieldEnum) // compare values if an enum
            return Objects.equals(((IFieldEnum<?>) otherZone).getValue(), ((IFieldEnum<?>)this).getValue());
        else if (otherZone instanceof IEntity) // compare primary keys if an entity
            return Objects.equals(((IEntity) otherZone).getPrimaryKey(), ((IEntity) this).getPrimaryKey());
        else
            return Objects.equals(this, other); // default to standard implementation
    }
}
