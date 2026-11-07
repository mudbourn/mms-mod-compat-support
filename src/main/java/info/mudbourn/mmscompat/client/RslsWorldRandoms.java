package info.mudbourn.mmscompat.client;

import com.google.common.collect.MapMaker;

import java.util.Collections;
import java.util.Set;

/**
 * Weak identity set of level randoms backing RSLS's {@code WorldRandomHolder}.
 */
public final class RslsWorldRandoms {

    private static final Set<Object> RANDOMS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private RslsWorldRandoms() {}

    public static void put(Object random) {
        RANDOMS.add(random);
    }

    public static boolean contains(Object random) {
        return RANDOMS.contains(random);
    }
}
