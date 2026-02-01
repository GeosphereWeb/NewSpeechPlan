package de.geosphere.speechplaning.core.ui

import android.content.Context
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.core.ui.provider.EventMapper
import de.geosphere.speechplaning.theme.R
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class EventMapperTest : BehaviorSpec({

    val mockContext = mockk<Context>()
    val stringProvider = AppEventStringProvider(mockContext)

    context("mapToString") {
        Given("a specific event and a configured provider") {
            every { mockContext.getString(R.string.event_convention) } returns "Regionaler Kongress"
            every { mockContext.getString(R.string.event_memorial) } returns "Gedächtnismahl"

            val mapper = EventMapper(stringProvider)

            When("mapToString is called for CONVENTION") {
                val result = mapper.mapToString(Event.CONVENTION)

                Then("it should return the string from the provider") {
                    result shouldBe "Regionaler Kongress"
                }
            }

            When("mapToString is called for MEMORIAL") {
                val result = mapper.mapToString(Event.MEMORIAL)

                Then("it should return the correct string") {
                    result shouldBe "Gedächtnismahl"
                }
            }
        }
    }

    context("mapToStatus") {
        Given("a set of unique event strings configured in the provider") {
            // Setup Mocks for all event types from AppEventStringProvider
            every { mockContext.getString(R.string.event_congregation) } returns "Versammlung"
            every { mockContext.getString(R.string.event_convention) } returns "Regionaler Kongress"
            every { mockContext.getString(R.string.event_memorial) } returns "Gedächtnismahl"
            every { mockContext.getString(R.string.event_circuit_assembly) } returns "Kreiskongress"
            every { mockContext.getString(R.string.event_circuit_overseer_congregation_visit) } returns
                "Besuch des Kreisaufsehers"
            every { mockContext.getString(R.string.event_special_lecture) } returns "Besonderer Vortrag"
            every { mockContext.getString(R.string.event_miscellaneous) } returns "Sonstiges"
            every { mockContext.getString(R.string.event_branch_convention) } returns "Zweigkongress"
            every { mockContext.getString(R.string.event_stream) } returns "Livestream"
            every { mockContext.getString(R.string.event_special_convention) } returns "Besonderer Kongress"
            every { mockContext.getString(R.string.event_unknown) } returns "Unbekannt"

            // Initialize mapper after setting up mocks
            val mapper = EventMapper(stringProvider)

            When("mapToStatus is called with the string for CONGREGATION") {
                val result = mapper.mapToStatus("Versammlung")
                Then("it should map to the CONGREGATION enum") {
                    result shouldBe Event.CONGREGATION
                }
            }

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

            When("mapToStatus is called with the string for CIRCUIT_ASSEMBLY") {
                val result = mapper.mapToStatus("Kreiskongress")
                Then("it should map to the CIRCUIT_ASSEMBLY enum") {
                    result shouldBe Event.CIRCUIT_ASSEMBLY
                }
            }

            When("mapToStatus is called with the string for CIRCUIT_OVERSEER_CONGREGATION_VISIT") {
                val result = mapper.mapToStatus("Besuch des Kreisaufsehers")
                Then("it should map to the CIRCUIT_OVERSEER_CONGREGATION_VISIT enum") {
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

            When("mapToStatus is called with the string for BRANCH_CONVENTION") {
                val result = mapper.mapToStatus("Zweigkongress")
                Then("it should map to the BRANCH_CONVENTION enum") {
                    result shouldBe Event.BRANCH_CONVENTION
                }
            }

            When("mapToStatus is called with the string for STREAM") {
                val result = mapper.mapToStatus("Livestream")
                Then("it should map to the STREAM enum") {
                    result shouldBe Event.STREAM
                }
            }

            When("mapToStatus is called with the string for SPECIAL_CONVENTION") {
                val result = mapper.mapToStatus("Besonderer Kongress")
                Then("it should map to the SPECIAL_CONVENTION enum") {
                    result shouldBe Event.SPECIAL_CONVENTION
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
