package de.geosphere.speechplaning.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test class for the SpeakerFilter enum.
 *
 * This test verifies the basic properties of the enum, such as the existence
 * of its constants and the correctness of the `valueOf` method.
 */
class SpeakerFilterTest {

    @Test
    fun `test enum constants exist`() {
        // This test simply ensures that the enum constants can be accessed without error.
        // It primarily serves to satisfy code coverage metrics.
        assertNotNull("ACTIVE constant should exist", SpeakerFilter.ACTIVE)
        assertNotNull("INACTIVE constant should exist", SpeakerFilter.INACTIVE)
        assertNotNull("ALL constant should exist", SpeakerFilter.ALL)
    }

    @Test
    fun `test valueOf returns correct enum constant`() {
        // Verifies that the string representation can be converted back to the enum constant.
        // This is a standard feature provided by the Kotlin compiler for enums.
        assertEquals(SpeakerFilter.ACTIVE, SpeakerFilter.valueOf("ACTIVE"))
        assertEquals(SpeakerFilter.INACTIVE, SpeakerFilter.valueOf("INACTIVE"))
        assertEquals(SpeakerFilter.ALL, SpeakerFilter.valueOf("ALL"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test valueOf throws exception for invalid name`() {
        // Ensures that an invalid string throws an IllegalArgumentException,
        // which is the expected behavior.
        SpeakerFilter.valueOf("NON_EXISTENT_FILTER")
    }

    @Test
    fun `test values array contains all constants`() {
        // Checks if the `values()` array contains all defined enum constants.
        val values = SpeakerFilter.values()
        assertEquals(3, values.size)
        assertEquals(SpeakerFilter.ACTIVE, values[0])
        assertEquals(SpeakerFilter.INACTIVE, values[1])
        assertEquals(SpeakerFilter.ALL, values[2])
    }
}
