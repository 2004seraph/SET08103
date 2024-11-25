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

    // This cache spans different instances of the same underlying zone
    // so creating the key for the ASIA continent on one instance will make its
    // cache available to other continent instances of ASIA.
    protected static final HashMap<String, List<IZone>> cacheMap = new HashMap<>(5);

    protected static <T> List<IZone> wrapIZone(List<T> concreteIZones) {
        return concreteIZones.stream().map(c -> (IZone)c).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected static <T extends IZone> List<T> unwrapIZone(List<IZone> concreteIZones) {
        return concreteIZones.stream().map(c -> (T)c).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        if ((other == null) || (getClass() != other.getClass()))
            return false;

        AbstractZone otherZone = (AbstractZone)other;
        if (otherZone instanceof IFieldEnum)
            return Objects.equals(((IFieldEnum<?>) otherZone).getValue(), ((IFieldEnum<?>)this).getValue());
        else if (otherZone instanceof IEntity)
            return Objects.equals(((IEntity) otherZone).getPrimaryKey(), ((IEntity) this).getPrimaryKey());
        else
            return Objects.equals(this, other);
    }

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
}
