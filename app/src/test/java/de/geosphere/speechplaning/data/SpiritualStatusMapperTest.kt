package de.geosphere.speechplaning.data

import android.content.Context
import de.geosphere.speechplaning.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.test.assertEquals

class SpiritualStatusMapperTest {

    @MockK
    private lateinit var mockContext: Context

    private lateinit var mapper: SpiritualStatusMapper

    // Diese Strings simulieren die Rückgabewerte aus context.getString()
    private val elderString = "Ältester"
    private val servantString = "Dienstamtgehilfe"
    private val unknownString = "Unbekannt"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Definiere das Verhalten des gemockten Contexts
        every { mockContext.getString(R.string.spiritual_status_elder) } returns elderString
        every { mockContext.getString(R.string.spiritual_status_ministerial_servant) } returns servantString
        every { mockContext.getString(R.string.spiritual_status_unknown) } returns unknownString

        // Initialisiere den Mapper, dessen lazy properties die Mocks verwenden werden
        mapper = SpiritualStatusMapper(mockContext)
    }

    @ParameterizedTest
    @EnumSource(SpiritualStatus::class)
    fun `mapToString returns correct localized string for each status`(status: SpiritualStatus) {
        // Arrange: Erwarteten String basierend auf dem Enum-Wert ermitteln
        val expectedString = when (status) {
            SpiritualStatus.ELDER -> elderString
            SpiritualStatus.MINISTERIAL_SERVANT -> servantString
            SpiritualStatus.UNKNOWN -> unknownString
        }

        // Act: Die Mapper-Funktion aufrufen
        val actualString = mapper.mapToString(status)

        // Assert: Überprüfen, ob das Ergebnis korrekt ist
        assertEquals(expectedString, actualString)
    }

    @Test
    fun `mapToStatus returns correct enum for each localized string`() {
        // Assert für jeden definierten String
        assertEquals(SpiritualStatus.ELDER, mapper.mapToStatus(elderString))
        assertEquals(SpiritualStatus.MINISTERIAL_SERVANT, mapper.mapToStatus(servantString))
        assertEquals(SpiritualStatus.UNKNOWN, mapper.mapToStatus(unknownString))
    }

    @Test
    fun `mapToStatus returns UNKNOWN for an invalid string`() {
        // Arrange
        val invalidStatusString = "NOT_A_VALID_STATUS"

        // Act
        val result = mapper.mapToStatus(invalidStatusString)

        // Assert
        assertEquals(SpiritualStatus.UNKNOWN, result, "Should return UNKNOWN for a string that is not in the map")
    }

    @Test
    fun `mapToString should return UNKNOWN string when a status is missing from the map`() {
        // Arrange
        // Simuliert, dass ein regulärer Eintrag in der internen Map fehlt.
        // Wir nutzen Reflection, da die Map private ist.
        val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val delegate = field.get(mapper) as Lazy<Map<SpiritualStatus, String>>
        val originalMap = delegate.value

        val incompleteMap = originalMap.toMutableMap()
        incompleteMap.remove(SpiritualStatus.MINISTERIAL_SERVANT) // Entferne einen Eintrag

        // Create a new lazy with the incomplete map
        val newLazy = lazy { incompleteMap as Map<SpiritualStatus, String> }
        field.set(mapper, newLazy)

        val expectedFallbackString = unknownString // Der Fallback-Wert für UNKNOWN

        // Act
        // Versuche, den nun "fehlenden" Status zu mappen.
        val actualString = mapper.mapToString(SpiritualStatus.MINISTERIAL_SERVANT)

        // Assert
        // Das Ergebnis sollte der Fallback-String für UNKNOWN sein.
        assertEquals(
            expectedFallbackString,
            actualString,
            "Sollte auf den UNKNOWN-String zurückfallen, wenn ein Mapping fehlt.",
        )
    }

    @Test
    fun `mapToString returns empty string when UNKNOWN itself is missing from the map`() {
        // Arrange
        // Simuliert den sehr unwahrscheinlichen Fall, dass sogar der UNKNOWN-Eintrag fehlt.
        val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val delegate = field.get(mapper) as Lazy<Map<SpiritualStatus, String>>
        val originalMap = delegate.value

        val incompleteMap = originalMap.toMutableMap()
        // Entferne sowohl einen normalen Eintrag als auch den UNKNOWN-Fallback
        incompleteMap.remove(SpiritualStatus.ELDER)
        incompleteMap.remove(SpiritualStatus.UNKNOWN) // Der entscheidende Schritt für diesen Test

        // Create a new lazy with the incomplete map
        val newLazy = lazy { incompleteMap as Map<SpiritualStatus, String> }
        field.set(mapper, newLazy)

        // Act
        // Versuche, den fehlenden Status zu mappen. Der Fallback sollte nun auch fehlschlagen.
        val result = mapper.mapToString(SpiritualStatus.ELDER)

        // Assert
        // Das Ergebnis sollte ein leerer String sein, da .orEmpty() auf einem null-Receiver aufgerufen wird.
        assertEquals(
            "",
            result,
            "Sollte einen leeren String zurückgeben, wenn der UNKNOWN-Fallback ebenfalls fehlt.",
        )
    }
}
