package reciter.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class EnvUtilsTest {

    @Test
    public void returnsValueWhenSet() {
        assertEquals("http://pubmed", EnvUtils.require("PUBMED_SERVICE", "http://pubmed"));
    }

    @Test
    public void blankValueIsRejected() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> EnvUtils.require("PUBMED_SERVICE", "  "));
        assertEquals("Required environment variable PUBMED_SERVICE is not set", e.getMessage());
        assertThrows(IllegalStateException.class, () -> EnvUtils.require("PUBMED_SERVICE", ""));
    }

    @Test
    public void throwsClearMessageWhenNotSet() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> EnvUtils.require("SCOPUS_SERVICE", null));
        assertEquals("Required environment variable SCOPUS_SERVICE is not set", e.getMessage());
    }
}
