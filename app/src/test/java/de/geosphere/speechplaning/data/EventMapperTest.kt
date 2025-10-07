package de.geosphere.speechplaning.data

import android.content.Context
import de.geosphere.speechplaning.R
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class EventMapperTest : BehaviorSpec({

    val mockContext: Context = mockk()

    lateinit var mapper: EventMapper

    // Diese Strings simulieren die Rückgabewerte aus context.getString()
    private val observerString = "observer"
    private val observerVisitString = "observer-visit"
    private val conventionString = "convention"
    private val memorialString = "memorial"
    private val specialLectureString = "special-lecture"
    private val miscellaneousString = "miscellaneous"
    private val unknownString = "unknown"

    beforeEach {
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

    init {
        context("mapToString returns correct localized string for each status") {
            withData(
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

        given("mapToString with inconsistent map") {
            `when`("a status is missing from the map") {
                then("it should return UNKNOWN string") {
                    // Arrange
                    val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
                    field.isAccessible = true

                    @Suppress("UNCHECKED_CAST")
                    val delegate = field.get(mapper) as Lazy<Map<Event, String>>
                    val originalMap = delegate.value

                    val incompleteMap = originalMap.toMutableMap()
                    incompleteMap.remove(Event.CONVENTION) // Entferne einen Eintrag

                    val newLazy = lazy { incompleteMap as Map<Event, String> }
                    field.set(mapper, newLazy)

                    val expectedFallbackString = unknownString

                    // Act & Assert
                    mapper.mapToString(Event.CONVENTION) shouldBe expectedFallbackString
                }
            }

            `when`("UNKNOWN itself is missing from the map") {
                then("it should return an empty string") {
                    // Arrange
                    val field = mapper.javaClass.getDeclaredField("statusToStringMap\$delegate")
                    field.isAccessible = true

                    @Suppress("UNCHECKED_CAST")
                    val delegate = field.get(mapper) as Lazy<Map<Event, String>>
                    val originalMap = delegate.value

                    val incompleteMap = originalMap.toMutableMap()
                    incompleteMap.remove(Event.CONVENTION)
                    incompleteMap.remove(Event.UNKNOWN)

                    val newLazy = lazy { incompleteMap as Map<Event, String> }
                    field.set(mapper, newLazy)

                    // Act & Assert
                    mapper.mapToString(Event.CONVENTION) shouldBe ""
                }
            }
        }
    }
})
