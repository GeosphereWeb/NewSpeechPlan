package de.geosphere.speechplaning.ui.atoms

import de.geosphere.speechplaning.core.ui.atoms.OrderName
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

/**
 * Order name test.
 *
 * @constructor Create empty Order name test
 */
class OrderNameTest : ShouldSpec({

    should("have enum constants declared in the correct order") {
        val expectedOrder = listOf("FIRSTNAME_LASTNAME", "LASTNAME_FIRSTNAME")
        val actualOrder = OrderName.entries.map { it.name }
        actualOrder shouldBe expectedOrder
    }

    should("contain the correct number of constants") {
        OrderName.entries.size shouldBe 2
    }

    should("return the correct enum constant from valueOf") {
        OrderName.valueOf("FIRSTNAME_LASTNAME") shouldBe OrderName.FIRSTNAME_LASTNAME
        OrderName.valueOf("LASTNAME_FIRSTNAME") shouldBe OrderName.LASTNAME_FIRSTNAME
    }
})
