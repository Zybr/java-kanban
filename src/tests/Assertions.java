package tests;

public class Assertions {
    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected != null) {
            if (!expected.equals(actual)) {
                throw new Error("Expected = " + expected + "; Actual = " + actual);
            }
        } else {
            throw new Error("Expected = " + null + "; Actual = " + actual);
        }

        System.out.println(
                "Assertion:\t\t\t" + expected + " = " + actual
                        + ((message != null) ? "\t[ " + message + " ]" : "")
        );
    }

    public static void assertTrue(Object actual) {
        assertTrue(actual, null);
    }

    public static void assertTrue(Object actual, String message) {
        assertEquals(true, actual, message);
    }

    public static void assertNull(Object actual) {
        assertNull(actual, null);
    }

    public static void assertNull(Object actual, String message) {
        assertEquals(null, actual, message);
    }
}
