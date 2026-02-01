package de.geosphere.speechplaning.core.model.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class EventTest : BehaviorSpec({

    given("the Event enum") {
        `when`("accessing its entries") {
            then("it should contain all defined events") {
                val expectedValues = listOf(
                    Event.CONGREGATION,
                    Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT,
                    Event.CIRCUIT_ASSEMBLY,
                    Event.CONVENTION,
                    Event.MEMORIAL,
                    Event.SPECIAL_LECTURE,
                    Event.MISCELLANEOUS,
                    Event.BRANCH_CONVENTION,
                    Event.STREAM,
                    Event.SPECIAL_CONVENTION,
                    Event.UNKNOWN,
                )
                Event.entries shouldContainExactlyInAnyOrder expectedValues
            }
        }

        `when`("using valueOf with valid strings") {
            withData(
                nameFn = { (stringValue, enumValue) -> "it should return ${enumValue.name} for string '$stringValue'" },
                "CONGREGATION" to Event.CONGREGATION,
                "CIRCUIT_OVERSEER_CONGREGATION_VISIT" to Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT,
                "CIRCUIT_ASSEMBLY" to Event.CIRCUIT_ASSEMBLY,
                "CONVENTION" to Event.CONVENTION,
                "MEMORIAL" to Event.MEMORIAL,
                "SPECIAL_LECTURE" to Event.SPECIAL_LECTURE,
                "MISCELLANEOUS" to Event.MISCELLANEOUS,
                "BRANCH_CONVENTION" to Event.BRANCH_CONVENTION,
                "STREAM" to Event.STREAM,
                "SPECIAL_CONVENTION" to Event.SPECIAL_CONVENTION,
                "UNKNOWN" to Event.UNKNOWN
            ) { (stringValue, enumValue) ->
                Event.valueOf(stringValue) shouldBe enumValue
            }
        }

        `when`("using valueOf with an invalid string") {
            then("it should throw an IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    Event.valueOf("NON_EXISTENT_VALUE")
                }
            }
        }
    }
})
