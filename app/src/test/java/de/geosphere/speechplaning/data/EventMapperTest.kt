package de.geosphere.speechplaning.data

import android.content.Context
import android.content.res.Resources
import de.geosphere.speechplaning.R
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

@OptIn(ExperimentalKotest::class)
class EventMapperTest : BehaviorSpec({

    val observerString = "observer"
    val observerVisitString = "observer-visit"
    val conventionString = "convention"
    val memorialString = "memorial"
    val specialLectureString = "special-lecture"
    val miscellaneousString = "miscellaneous"
    val unknownString = "unknown"

    context("a fully configured mapper") {
        val mockContext = mockk<Context>()
        // Definiere das Verhalten des gemockten Contexts für alle Event-Typen
        every { mockContext.getString(R.string.event_circuit_assembly_with_circuit_overseer) } returns observerString
        every { mockContext.getString(R.string.event_circuit_overseer_congregation_visit) } returns observerVisitString
        every { mockContext.getString(R.string.event_convention) } returns conventionString
        every { mockContext.getString(R.string.event_memorial) } returns memorialString
        every { mockContext.getString(R.string.event_special_lecture) } returns specialLectureString
        every { mockContext.getString(R.string.event_miscellaneous) } returns miscellaneousString
        every { mockContext.getString(R.string.event_unknown) } returns unknownString

        val mapper = EventMapper(mockContext)

        context("mapToString returns correct localized string for each status") {
            withData(
                nameFn = { (event, expected) -> "should map ${event.name} to '$expected'" },
                Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER to observerString,
                Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT to observerVisitString,
                Event.CONVENTION to conventionString,
                Event.MEMORIAL to memorialString,
                Event.SPECIAL_LECTURE to specialLectureString,
                Event.MISCELLANEOUS to miscellaneousString,
                Event.UNKNOWN to unknownString
            ) { (status, expectedString) ->
                mapper.mapToString(status) shouldBe expectedString
            }
        }

        given("mapToStatus") {
            `when`("a localized string is provided") {
                then("it should return the correct enum") {
                    mapper.mapToStatus(observerString) shouldBe Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER
                    mapper.mapToStatus(observerVisitString) shouldBe Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT
                    mapper.mapToStatus(conventionString) shouldBe Event.CONVENTION
                    mapper.mapToStatus(memorialString) shouldBe Event.MEMORIAL
                    mapper.mapToStatus(specialLectureString) shouldBe Event.SPECIAL_LECTURE
                    mapper.mapToStatus(miscellaneousString) shouldBe Event.MISCELLANEOUS
                    mapper.mapToStatus(unknownString) shouldBe Event.UNKNOWN
                }
            }

            `when`("an unrecognized string is provided") {
                then("it should return UNKNOWN") {
                    val unrecognizedString = "Gibt es nicht"
                    mapper.mapToStatus(unrecognizedString) shouldBe Event.UNKNOWN
                }
            }
        }
    }

    context("an inconsistently configured mapper") {
        `when`("a status is missing from the context") {
            val incompleteContext = mockk<Context> {
                // Alle Strings mocken, außer 'CONVENTION'
                every { getString(R.string.event_circuit_assembly_with_circuit_overseer) } returns observerString
                every { getString(R.string.event_circuit_overseer_congregation_visit) } returns observerVisitString
                // every { getString(R.string.event_convention) } returns conventionString // <- Ausgelassen
                every { getString(R.string.event_memorial) } returns memorialString
                every { getString(R.string.event_special_lecture) } returns specialLectureString
                every { getString(R.string.event_miscellaneous) } returns miscellaneousString
                every { getString(R.string.event_unknown) } returns unknownString
                // Mock, damit getString eine Exception wirft, wenn ein unbekannter String angefordert wird
                every { getString(R.string.event_convention) } throws Resources.NotFoundException()
            }
            val mapper = EventMapper(incompleteContext)

            then("it should return the UNKNOWN string as fallback") {
                mapper.mapToString(Event.CONVENTION) shouldBe unknownString
            }
        }

        `when`("UNKNOWN itself is missing from the map") {
            val incompleteContext = mockk<Context> {
                // Alle Strings mocken, außer 'CONVENTION' und 'UNKNOWN'
                every { getString(R.string.event_circuit_assembly_with_circuit_overseer) } returns observerString
                every { getString(R.string.event_circuit_overseer_congregation_visit) } returns observerVisitString
                // every { getString(R.string.event_convention) } returns conventionString // <- Ausgelassen
                every { getString(R.string.event_memorial) } returns memorialString
                every { getString(R.string.event_special_lecture) } returns specialLectureString
                every { getString(R.string.event_miscellaneous) } returns miscellaneousString
                // every { getString(R.string.event_unknown) } returns unknownString // <- Ausgelassen

                // Mock, damit getString eine Exception wirft, wenn ein unbekannter String angefordert wird
                every { getString(R.string.event_convention) } throws Resources.NotFoundException()
                every { getString(R.string.event_unknown) } throws Resources.NotFoundException()
            }
            val mapper = EventMapper(incompleteContext)

            then("it should return an empty string as final fallback") {
                mapper.mapToString(Event.CONVENTION) shouldBe ""
            }
        }
    }
})
