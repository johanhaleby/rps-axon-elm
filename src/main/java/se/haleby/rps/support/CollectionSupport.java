package se.haleby.rps.support;

import java.util.Collection;

public class CollectionSupport {

    public static <R, T extends Collection<R>> T add(T t, R r) {
        t.add(r);
        return t;
    }
}
