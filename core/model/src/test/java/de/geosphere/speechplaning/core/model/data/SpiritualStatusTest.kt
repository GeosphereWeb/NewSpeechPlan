package de.geosphere.speechplaning.core.model.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SpiritualStatusTest : ShouldSpec({

    should("return all constants in order from entries") {
        val expectedValues = listOf(
            SpiritualStatus.UNKNOWN,
            SpiritualStatus.MINISTERIAL_SERVANT,
            SpiritualStatus.ELDER,
        )
        val actualValues = SpiritualStatus.entries

        actualValues shouldBe expectedValues
        actualValues.size shouldBe 3
    }

    should("return correct enum for valid strings from valueOf") {
        SpiritualStatus.valueOf("UNKNOWN") shouldBe SpiritualStatus.UNKNOWN
        SpiritualStatus.valueOf("MINISTERIAL_SERVANT") shouldBe SpiritualStatus.MINISTERIAL_SERVANT
        SpiritualStatus.valueOf("ELDER") shouldBe SpiritualStatus.ELDER
    }

    should("throw IllegalArgumentException for invalid string in valueOf") {
        shouldThrow<IllegalArgumentException> {
            SpiritualStatus.valueOf("NON_EXISTENT_VALUE")
        }
    }
})
