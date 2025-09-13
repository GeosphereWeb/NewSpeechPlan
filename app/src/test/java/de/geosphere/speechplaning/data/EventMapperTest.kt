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

class EventMapperTest {

    @MockK
    private lateinit var mockContext: Context

    private lateinit var mapper: EventMapper

    // Diese Strings simulieren die Rückgabewerte aus context.getString()
    private val observerString = "observer"
    private val observerVisitString = "observer-visit"
    private val conventionString = "convention"
    private val memorialString = "memorial"
    private val specialLectureString = "special-lecture"
    private val miscellaneousString = "miscellaneous"
    private val unknownString = "unknown"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        // Definiere das Verhalten des gemockten Contexts
        every { mockContext.getString(R.string.event_circuit_assembly_with_circuit_overseer) } returns observerString
        every { mockContext.getString(R.string.event_circuit_overseer_congregation_visit) } returns observerVisitString
        every { mockContext.getString(R.string.event_convention) } returns conventionString
        every { mockContext.getString(R.string.event_memorial) } returns memorialString
        every { mockContext.getString(R.string.event_special_lecture) } returns specialLectureString
        every { mockContext.getString(R.string.event_miscellaneous) } returns miscellaneousString
        every { mockContext.getString(R.string.event_unknown) } returns unknownString

        // Initialisiere den Mapper, dessen lazy properties die Mocks verwenden werden
        mapper = EventMapper(mockContext)
    }

    @ParameterizedTest
    @EnumSource(Event::class)
    fun `mapToString returns correct localized string for each status`(status: Event) {
        // Arrange: Erwarteten String basierend auf dem Enum-Wert ermitteln
        val expectedString = when (status) {
            Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER -> observerString
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT -> observerVisitString
            Event.CONVENTION -> conventionString
            Event.MEMORIAL -> memorialString
            Event.SPECIAL_LECTURE -> specialLectureString
            Event.MISCELLANEOUS -> miscellaneousString
            Event.UNKNOWN -> unknownString
        }

        // Act: Die Mapper-Funktion aufrufen
        val actualString = mapper.mapToString(status)

        // Assert: Überprüfen, ob das Ergebnis korrekt ist
        assertEquals(expectedString, actualString)
    }

    @Test
    fun `mapToStatus returns correct enum for each localized string`() {
        // Assert für jeden definierten String
        assertEquals(Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER, mapper.mapToStatus(observerString))
        assertEquals(Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT, mapper.mapToStatus(observerVisitString))
        assertEquals(Event.CONVENTION, mapper.mapToStatus(conventionString))
        assertEquals(Event.MEMORIAL, mapper.mapToStatus(memorialString))
        assertEquals(Event.SPECIAL_LECTURE, mapper.mapToStatus(specialLectureString))
        assertEquals(Event.MISCELLANEOUS, mapper.mapToStatus(miscellaneousString))
        assertEquals(Event.UNKNOWN, mapper.mapToStatus(unknownString))
    }

    @Test
    fun `mapToStatus returns UNKNOWN for an unrecognized string`() {
        // Arrange: Ein unbekannter String
        val unrecognizedString = "Gibt es nicht"

        // Act: Die Mapper-Funktion aufrufen
        val result = mapper.mapToStatus(unrecognizedString)

        // Assert: Das Ergebnis sollte UNKNOWN sein
        assertEquals(Event.UNKNOWN, result)
    }

    @Test
    fun `mapToString should return UNKNOWN string when a status is missing from the map`() {
        // Arrange
        // Simuliert, dass ein regulärer Eintrag in der internen Map fehlt.
        // Wir nutzen Reflection, da die Map private ist.
        val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val delegate = field.get(mapper) as Lazy<Map<Event, String>>
        val originalMap = delegate.value

        val incompleteMap = originalMap.toMutableMap()
        incompleteMap.remove(Event.CONVENTION) // Entferne einen Eintrag

        // Create a new lazy with the incomplete map
        val newLazy = lazy { incompleteMap as Map<Event, String> }
        field.set(mapper, newLazy)

        val expectedFallbackString = unknownString // Der Fallback-Wert für UNKNOWN

        // Act
        // Versuche, den nun "fehlenden" Status zu mappen.
        val actualString = mapper.mapToString(Event.CONVENTION)

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
        val delegate = field.get(mapper) as Lazy<Map<Event, String>>
        val originalMap = delegate.value

        val incompleteMap = originalMap.toMutableMap()
        // Entferne sowohl einen normalen Eintrag als auch den UNKNOWN-Fallback
        incompleteMap.remove(Event.CONVENTION)
        incompleteMap.remove(Event.UNKNOWN) // Der entscheidende Schritt für diesen Test

        // Create a new lazy with the incomplete map
        val newLazy = lazy { incompleteMap as Map<Event, String> }
        field.set(mapper, newLazy)

        // Act
        // Versuche, den fehlenden Status zu mappen. Der Fallback sollte nun auch fehlschlagen.
        val result = mapper.mapToString(Event.CONVENTION)

        // Assert
        // Das Ergebnis sollte ein leerer String sein, da .orEmpty() auf einem null-Receiver aufgerufen wird.
        assertEquals(
            "",
            result,
            "Sollte einen leeren String zurückgeben, wenn der UNKNOWN-Fallback ebenfalls fehlt.",
        )
    }
}
