package de.geosphere.speechplaning.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EventTest : ShouldSpec({

    init {
        should("return all constants in order from entries") {
            val expectedValues = listOf(
                Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
                Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT,
                Event.CONVENTION,
                Event.MEMORIAL,
                Event.SPECIAL_LECTURE,
                Event.MISCELLANEOUS,
                Event.UNKNOWN,
            )
            val actualValues = Event.entries
            actualValues shouldBe expectedValues
            actualValues.size shouldBe 7
        }

        should("return correct enum for valid strings from valueOf") {
            Event.valueOf("CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER") shouldBe Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER
            Event.valueOf("CIRCUIT_OVERSEER_CONGREGATION_VISIT") shouldBe Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT
            Event.valueOf("CONVENTION") shouldBe Event.CONVENTION
            Event.valueOf("MEMORIAL") shouldBe Event.MEMORIAL
            Event.valueOf("SPECIAL_LECTURE") shouldBe Event.SPECIAL_LECTURE
            Event.valueOf("MISCELLANEOUS") shouldBe Event.MISCELLANEOUS
            Event.valueOf("UNKNOWN") shouldBe Event.UNKNOWN
        }

        should("throw IllegalArgumentException for invalid string in valueOf") {
            shouldThrow<IllegalArgumentException> {
                Event.valueOf("NON_EXISTENT_VALUE")
            }
        }
    }
})
