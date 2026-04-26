
package de.geosphere.speechplaning.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.R
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class AppEventStringProviderTest : StringSpec({

    lateinit var mockContext: Context
    lateinit var stringProvider: AppEventStringProvider

    beforeTest {
        mockContext = mockk()
        stringProvider = AppEventStringProvider(mockContext)
    }

    "getStringForEvent with CIRCUIT_ASSEMBLY returns correct string" {
        // Given
        val expectedString = "Circuit Assembly"
        every { mockContext.getString(R.string.event_circuit_assembly) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.CIRCUIT_ASSEMBLY)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with CONGREGATION returns correct string" {
        // Given
        val expectedString = "Congregation"
        every { mockContext.getString(R.string.event_congregation) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.CONGREGATION)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with CIRCUIT_OVERSEER_CONGREGATION_VISIT returns correct string" {
        // Given
        val expectedString = "Circuit Overseer Congregation Visit"
        every { mockContext.getString(R.string.event_circuit_overseer_congregation_visit) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with CONVENTION returns correct string" {
        // Given
        val expectedString = "Convention"
        every { mockContext.getString(R.string.event_convention) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.CONVENTION)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with MEMORIAL returns correct string" {
        // Given
        val expectedString = "Memorial"
        every { mockContext.getString(R.string.event_memorial) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.MEMORIAL)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with SPECIAL_LECTURE returns correct string" {
        // Given
        val expectedString = "Special Lecture"
        every { mockContext.getString(R.string.event_special_lecture) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.SPECIAL_LECTURE)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with BRANCH_CONVENTION returns correct string" {
        // Given
        val expectedString = "Branch Convention"
        every { mockContext.getString(R.string.event_branch_convention) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.BRANCH_CONVENTION)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with STREAM returns correct string" {
        // Given
        val expectedString = "Stream"
        every { mockContext.getString(R.string.event_stream) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.STREAM)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with SPECIAL_CONVENTION returns correct string" {
        // Given
        val expectedString = "Special Convention"
        every { mockContext.getString(R.string.event_special_convention) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.SPECIAL_CONVENTION)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with MISCELLANEOUS returns correct string" {
        // Given
        val expectedString = "Miscellaneous"
        every { mockContext.getString(R.string.event_miscellaneous) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.MISCELLANEOUS)

        // Then
        result shouldBe expectedString
    }

    "getStringForEvent with UNKNOWN returns correct string" {
        // Given
        val expectedString = "Unknown"
        every { mockContext.getString(R.string.event_unknown) } returns expectedString

        // When
        val result = stringProvider.getStringForEvent(Event.UNKNOWN)

        // Then
        result shouldBe expectedString
    }

    "getUnknownEventString returns correct string" {
        // Given
        val expectedString = "Unknown"
        every { mockContext.getString(R.string.event_unknown) } returns expectedString

        // When
        val result = stringProvider.getUnknownEventString()

        // Then
        result shouldBe expectedString
    }
})
