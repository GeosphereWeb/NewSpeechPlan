package de.geosphere.speechplaning.core.ui

import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.EventMapper
import de.geosphere.speechplaning.core.ui.provider.EventStringProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class EventMapperTest : BehaviorSpec({

    val mockProvider = mockk<EventStringProvider>()

    context("mapToString") {
        Given("a specific event and a configured provider") {
            val mapper = EventMapper(mockProvider)
            every { mockProvider.getStringForEvent(Event.CONVENTION) } returns "Regionaler Kongress"

            When("mapToString is called for that event") {
                val result = mapper.mapToString(Event.CONVENTION)

                Then("it should return the string from the provider") {
                    result shouldBe "Regionaler Kongress"
                }
            }
        }
    }

    context("mapToEvent") { // Renamed from mapToStatus for clarity
        Given("a set of unique event strings configured in the provider") {
            // Setup Mocks for all event types
            every { mockProvider.getStringForEvent(Event.CONVENTION) } returns "Regionaler Kongress"
            every { mockProvider.getStringForEvent(Event.MEMORIAL) } returns "Gedächtnismahl"
            every {
                mockProvider.getStringForEvent(Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER)
            } returns "Kreiskongress mit Kreisaufseher"
            every { mockProvider.getStringForEvent(Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT) } returns
                "Besuch des Kreisaufsehers"
            every { mockProvider.getStringForEvent(Event.SPECIAL_LECTURE) } returns "Besonderer Vortrag"
            every { mockProvider.getStringForEvent(Event.MISCELLANEOUS) } returns "Sonstiges"
            every { mockProvider.getStringForEvent(Event.UNKNOWN) } returns "Unbekannt"

            // Initialize mapper after setting up mocks
            val mapper = EventMapper(mockProvider)

            When("mapToStatus is called with the string for CONVENTION") {
                val result = mapper.mapToStatus("Regionaler Kongress")
                Then("it should map to the CONVENTION enum") {
                    result shouldBe Event.CONVENTION
                }
            }

            When("mapToStatus is called with the string for MEMORIAL") {
                val result = mapper.mapToStatus("Gedächtnismahl")
                Then("it should map to the MEMORIAL enum") {
                    result shouldBe Event.MEMORIAL
                }
            }

            When("mapToStatus is called with the string for CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER") {
                val result = mapper.mapToStatus("Kreiskongress mit Kreisaufseher")
                Then("it should map to the correct enum") {
                    result shouldBe Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER
                }
            }

            When("mapToStatus is called with the string for CIRCUIT_OVERSEER_CONGREGATION_VISIT") {
                val result = mapper.mapToStatus("Besuch des Kreisaufsehers")
                Then("it should map to the correct enum") {
                    result shouldBe Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT
                }
            }

            When("mapToStatus is called with the string for SPECIAL_LECTURE") {
                val result = mapper.mapToStatus("Besonderer Vortrag")
                Then("it should map to the SPECIAL_LECTURE enum") {
                    result shouldBe Event.SPECIAL_LECTURE
                }
            }

            When("mapToStatus is called with the string for MISCELLANEOUS") {
                val result = mapper.mapToStatus("Sonstiges")
                Then("it should map to the MISCELLANEOUS enum") {
                    result shouldBe Event.MISCELLANEOUS
                }
            }

            When("mapToStatus is called with the string for UNKNOWN") {
                val result = mapper.mapToStatus("Unbekannt")
                Then("it should map to the UNKNOWN enum") {
                    result shouldBe Event.UNKNOWN
                }
            }
        }
    }
})
