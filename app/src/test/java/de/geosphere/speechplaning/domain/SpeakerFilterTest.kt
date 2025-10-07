package de.geosphere.speechplaning.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SpeakerFilterTest : ShouldSpec({

    init {
        should("return all constants in order from entries") {
            val expectedValues = listOf(
                SpeakerFilter.ACTIVE,
                SpeakerFilter.INACTIVE,
                SpeakerFilter.ALL
            )
            val actualValues = SpeakerFilter.entries
            actualValues shouldBe expectedValues
            actualValues.size shouldBe 3
        }

        should("return correct enum for valid strings from valueOf") {
            SpeakerFilter.valueOf("ACTIVE") shouldBe SpeakerFilter.ACTIVE
            SpeakerFilter.valueOf("INACTIVE") shouldBe SpeakerFilter.INACTIVE
            SpeakerFilter.valueOf("ALL") shouldBe SpeakerFilter.ALL
        }

        should("throw IllegalArgumentException for invalid string in valueOf") {
            shouldThrow<IllegalArgumentException> {
                SpeakerFilter.valueOf("NON_EXISTENT_VALUE")
            }
        }
    }
})
