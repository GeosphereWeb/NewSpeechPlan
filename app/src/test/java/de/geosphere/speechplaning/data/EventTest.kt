package de.geosphere.speechplaning.data

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EventTest {

    @Test
    fun `test values() method returns all constants in order`() {
        // 1. Define the expected order and content
        val expectedValues = arrayOf(
            Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT,
            Event.CONVENTION,
            Event.MEMORIAL,
            Event.SPECIAL_LECTURE,
            Event.MISCELLANEOUS,
            Event.UNKNOWN,
        )

        // 2. Call the `values()` method to get the actual values
        val actualValues = Event.entries.toTypedArray()

        // 3. Use `assertArrayEquals` to verify the content and order
        // This forces the evaluation of the entire array and its creation.
        assertArrayEquals(
            expectedValues,
            actualValues,
            "The values() array should contain all enum constants in declaration order"
        )

        // 4. Additionally, assert the size to be explicit
        assertEquals(7, actualValues.size, "There should be exactly 7 filter options")
    }

    /**
     * Verifies that the `valueOf()` method correctly converts a string
     * to its corresponding enum constant. This covers the `valueOf` branch.
     */
    @Test
    fun `test valueOf() returns correct enum for valid strings`() {
        assertEquals(
            Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
            Event.valueOf("CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER")
        )
        assertEquals(
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT,
            Event.valueOf("CIRCUIT_OVERSEER_CONGREGATION_VISIT")
        )
        assertEquals(
            Event.CONVENTION,
            Event.valueOf("CONVENTION")
        )
        assertEquals(
            Event.MEMORIAL,
            Event.valueOf("MEMORIAL")
        )
        assertEquals(
            Event.SPECIAL_LECTURE,
            Event.valueOf("SPECIAL_LECTURE")
        )
        assertEquals(
            Event.MISCELLANEOUS,
            Event.valueOf("MISCELLANEOUS")
        )
        assertEquals(
            Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
            Event.valueOf("CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER")
        )
        assertEquals(
            Event.UNKNOWN,
            Event.valueOf("UNKNOWN")
        )
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
