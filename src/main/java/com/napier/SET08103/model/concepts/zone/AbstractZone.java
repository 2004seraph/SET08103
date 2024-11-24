package com.napier.SET08103.model.concepts.zone;

import com.napier.SET08103.model.db.IEntity;
import com.napier.SET08103.model.db.IFieldEnum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public abstract class AbstractZone {
//    private static final HashMap<String, List<IZone>> cacheMap = new HashMap<>(5);

    @Override
    public boolean equals(Object other) {
        if ((other == null) || (getClass() != other.getClass()))
            return false;

        AbstractZone otherZone = (AbstractZone)other;
        if (otherZone instanceof IFieldEnum)
            return Objects.equals(((IFieldEnum<?>) otherZone).getValue(), ((IFieldEnum<?>)this).getValue());
        else
            return Objects.equals(((IEntity) otherZone).getPrimaryKey(), ((IEntity) this).getPrimaryKey());
    }
}
