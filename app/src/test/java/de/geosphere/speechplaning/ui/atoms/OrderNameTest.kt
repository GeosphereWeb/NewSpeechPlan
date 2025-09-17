package de.geosphere.speechplaning.ui.atoms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Order name test.
 *
 * @constructor Create empty Order name test
 */
class OrderNameTest {

    @Test
    fun `enum constants are declared in the correct order`() {
        val expectedOrder = listOf("FIRSTNAME_LASTNAME", "LASTNAME_FIRSTNAME")
        val actualOrder = OrderName.entries.map { it.name }
        assertEquals(expectedOrder, actualOrder)
    }

    @Test
    fun `enum contains the correct number of constants`() {
        assertEquals(2, OrderName.entries.size)
    }

    @Test
    fun `valueOf returns the correct enum constant`() {
        assertEquals(OrderName.FIRSTNAME_LASTNAME, OrderName.valueOf("FIRSTNAME_LASTNAME"))
        assertEquals(OrderName.LASTNAME_FIRSTNAME, OrderName.valueOf("LASTNAME_FIRSTNAME"))
    }
}
