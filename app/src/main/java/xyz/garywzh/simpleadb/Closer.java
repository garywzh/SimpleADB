package xyz.garywzh.simpleadb;

import java.io.Closeable;

/**
 * Created by garywzh on 2016/3/30.
 */
public class Closer {
    public static void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    if (x instanceof Closeable) {
                        ((Closeable) x).close();
                    } else {
                        throw new RuntimeException("cannot close " + x);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}