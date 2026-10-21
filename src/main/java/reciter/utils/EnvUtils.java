package reciter.utils;

public final class EnvUtils {

    private EnvUtils() {}

    /**
     * Returns {@code value} unchanged, or fails with a clear message when the environment
     * variable it was read from is not set (instead of a bare NullPointerException).
     */
    public static String require(String name, String value) {
        if (value == null) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }

}
