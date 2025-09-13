package de.geosphere.speechplaning.data

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SpiritualStatusTest {

    @Test
    fun `test values() method returns all constants in order`() {
        // 1. Define the expected order and content
        val expectedValues = arrayOf(
            SpiritualStatus.UNKNOWN,
            SpiritualStatus.MINISTERIAL_SERVANT,
            SpiritualStatus.ELDER,
        )

        // 2. Call the `values()` method to get the actual values
        val actualValues = SpiritualStatus.entries.toTypedArray()

        // 3. Use `assertArrayEquals` to verify the content and order
        // This forces the evaluation of the entire array and its creation.
        assertArrayEquals(
            expectedValues,
            actualValues,
            "The values() array should contain all enum constants in declaration order"
        )

        // 4. Additionally, assert the size to be explicit
        assertEquals(3, actualValues.size, "There should be exactly 3 filter options")
    }

    /**
     * Verifies that the `valueOf()` method correctly converts a string
     * to its corresponding enum constant. This covers the `valueOf` branch.
     */
    @Test
    fun `test valueOf() returns correct enum for valid strings`() {
        assertEquals(SpiritualStatus.UNKNOWN, SpiritualStatus.valueOf("UNKNOWN"))
        assertEquals(SpiritualStatus.MINISTERIAL_SERVANT, SpiritualStatus.valueOf("MINISTERIAL_SERVANT"))
        assertEquals(SpiritualStatus.ELDER, SpiritualStatus.valueOf("ELDER"))
    }

    /**
     * Ensures that calling `valueOf()` with an invalid string name
     * throws an IllegalArgumentException, which is the expected behavior
     * for the failure path.
     */
    @Test
    fun `test valueOf() throws IllegalArgumentException for invalid string`() {
        // Junit 5 style for expecting exceptions
        assertThrows<IllegalArgumentException> {
            // This call must throw an exception to pass the test
            SpiritualStatus.valueOf("NON_EXISTENT_VALUE")
        }
    }
}
