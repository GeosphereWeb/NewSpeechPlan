package de.geosphere.speechplaning.data

import android.content.Context
import de.geosphere.speechplaning.R
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.resterampe.SpiritualStatusMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class SpiritualStatusMapperTest : BehaviorSpec({

    val mockContext: Context = mockk()
    lateinit var mapper: SpiritualStatusMapper

    // Diese Strings simulieren die Rückgabewerte aus context.getString()
    val elderString = "Ältester"
    val servantString = "Dienstamtgehilfe"
    val unknownString = "Unbekannt"

    beforeTest {
        // Definiere das Verhalten des gemockten Contexts
        every { mockContext.getString(R.string.spiritual_status_elder) } returns elderString
        every { mockContext.getString(R.string.spiritual_status_ministerial_servant) } returns servantString
        every { mockContext.getString(R.string.spiritual_status_unknown) } returns unknownString

        // Initialisiere den Mapper, dessen lazy properties die Mocks verwenden werden
        mapper = SpiritualStatusMapper(mockContext)
    }

    context("mapToString returns correct localized string for each status") {
        withData(
            SpiritualStatus.ELDER to elderString,
            SpiritualStatus.MINISTERIAL_SERVANT to servantString,
            SpiritualStatus.UNKNOWN to unknownString
        ) { (status, expectedString) ->
            mapper.mapToString(status) shouldBe expectedString
        }
    }

    given("mapToStatus") {
        `when`("a localized string is provided") {
            then("it should return the correct enum") {
                mapper.mapToStatus(elderString) shouldBe SpiritualStatus.ELDER
                mapper.mapToStatus(servantString) shouldBe SpiritualStatus.MINISTERIAL_SERVANT
                mapper.mapToStatus(unknownString) shouldBe SpiritualStatus.UNKNOWN
            }
        }

        `when`("an invalid string is provided") {
            then("it should return UNKNOWN") {
                val invalidStatusString = "NOT_A_VALID_STATUS"
                mapper.mapToStatus(invalidStatusString) shouldBe SpiritualStatus.UNKNOWN
            }
        }
    }

    given("mapToString with an inconsistent map") {
        `when`("a status is missing from the map") {
            then("it should return the UNKNOWN string as fallback") {
                val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
                field.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val delegate = field.get(mapper) as Lazy<Map<SpiritualStatus, String>>
                val originalMap = delegate.value

                val incompleteMap = originalMap.toMutableMap()
                incompleteMap.remove(SpiritualStatus.MINISTERIAL_SERVANT) // Entferne einen Eintrag

                val newLazy = lazy { incompleteMap as Map<SpiritualStatus, String> }
                field.set(mapper, newLazy)

                mapper.mapToString(SpiritualStatus.MINISTERIAL_SERVANT) shouldBe unknownString
            }
        }

        `when`("the UNKNOWN status itself is missing from the map") {
            then("it should return an empty string") {
                val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
                field.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                val delegate = field.get(mapper) as Lazy<Map<SpiritualStatus, String>>
                val originalMap = delegate.value

                val incompleteMap = originalMap.toMutableMap()
                incompleteMap.remove(SpiritualStatus.ELDER)
                incompleteMap.remove(SpiritualStatus.UNKNOWN)

                val newLazy = lazy { incompleteMap as Map<SpiritualStatus, String> }
                field.set(mapper, newLazy)

                mapper.mapToString(SpiritualStatus.ELDER) shouldBe ""
            }
        }
    }
})
