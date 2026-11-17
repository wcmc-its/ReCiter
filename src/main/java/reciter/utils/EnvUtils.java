package reciter.utils;

public final class EnvUtils {

    private EnvUtils() {}

    /**
     * Returns the value of the environment variable, or {@code null} when it is not set.
     * Use for variables that are optional.
     */
    public static String get(String name) {
        return System.getenv(name);
    }

    /**
     * Returns {@code value} unchanged, or fails with a clear message when the environment
     * variable it was read from is not set (instead of a bare NullPointerException).
     */
    public static String require(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }

}
