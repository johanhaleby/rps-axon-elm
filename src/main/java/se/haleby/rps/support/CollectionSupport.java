package se.haleby.rps.support;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Supplier;

public class CollectionSupport {

    public static <R, T extends Collection<R>> T add(T t, R r) {
        t.add(r);
        return t;
    }

    public static <R, T extends TreeSet<R>> T replaceLast(Supplier<T> ts, T t, R r) {
        T newT = ts.get();
        newT.addAll(t);
        newT.remove(newT.last());
        newT.add(r);
        return newT;
    }
}
