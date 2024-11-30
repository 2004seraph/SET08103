package uk.ac.napier.SET08103.model.concepts.zone;

import uk.ac.napier.SET08103.model.db.IEntity;
import uk.ac.napier.SET08103.model.db.IFieldEnum;

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
     * <p>
     * This also means that if one calls Asia.getCities(), it will preload the cache
     * with all the intermediate zones required to retrieve such data.
     */
    protected static final HashMap<String, List<IZone>> cacheMap = new HashMap<>(5);

    /**
     * Flattens a list of all descendant zones of the given distance in levels from this zone instance
     * <p>
     * For example:
     * districtInstance.getInnerZones(1, conn) -> List<City>,
     * continentInstance.getInnerZones(3, conn) -> List<District>
     * continentInstance.getInnerZones(4, conn) -> List<City>
     * @param traverseDown How many tree levels down from the current one to traverse
     * @param conn Database connection
     * @return A List of all zone instances from that level
     */
    public List<IZone> getInnerZones(final int traverseDown, final Connection conn) throws SQLException {
        if (traverseDown < 1)
            throw new RuntimeException("Invalid depth");

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
    public boolean equals(final Object other) {
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

    @Override
    public int compareTo(final IZone o) {
        return this.toString().compareTo(o.toString());
    }
}
