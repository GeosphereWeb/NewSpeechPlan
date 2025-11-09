package de.geosphere.speechplaning.core.ui

import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.core.ui.atoms.SpiritualStatusMapper
import de.geosphere.speechplaning.core.ui.atoms.SpiritualStatusStringProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class SpiritualStatusMapperTest : BehaviorSpec({

    val mockProvider = mockk<SpiritualStatusStringProvider>()

    context("mapToString") {
        Given("a specific spiritual status and a configured provider") {
            val mapper = SpiritualStatusMapper(mockProvider)
            every { mockProvider.getStringForSpiritualStatus(SpiritualStatus.ELDER) } returns "Ältester"

            When("mapToString is called for that spiritual status") {
                val result = mapper.mapToString(SpiritualStatus.ELDER)

                Then("it should return the string from the provider") {
                    result shouldBe "Ältester"
                }
            }
        }
    }

    context("mapToStatus") {
        Given("a set of unique spiritual status strings configured in the provider") {
            // Setup für die Map im Mapper. Die Initialisierung des Mappers muss NACH dem Setup erfolgen.
            every { mockProvider.getStringForSpiritualStatus(SpiritualStatus.ELDER) } returns "Ältester"
            every { mockProvider.getStringForSpiritualStatus(SpiritualStatus.MINISTERIAL_SERVANT) } returns
                "Dienstamtgehilfe"
            every { mockProvider.getStringForSpiritualStatus(SpiritualStatus.UNKNOWN) } returns "Unbekannt"
            // Initialisiere den Mapper hier, damit die 'lazy' Map die Mocks verwendet.
            val mapper = SpiritualStatusMapper(mockProvider)

            When("mapToStatus is called with the string for ELDER") {
                val result = mapper.mapToStatus("Ältester")

                Then("it should map to the ELDER enum") {
                    result shouldBe SpiritualStatus.ELDER
                }
            }

            When("mapToStatus is called with the string for MINISTERIAL_SERVANT") {
                val result = mapper.mapToStatus("Dienstamtgehilfe")

                Then("it should map to the MINISTERIAL_SERVANT enum") {
                    result shouldBe SpiritualStatus.MINISTERIAL_SERVANT
                }
            }

            When("mapToStatus is called with the string for UNKNOWN") {
                val result = mapper.mapToStatus("Unbekannt")

                Then("it should map to the UNKNOWN enum") {
                    result shouldBe SpiritualStatus.UNKNOWN
                }
            }
        }
    }
})
