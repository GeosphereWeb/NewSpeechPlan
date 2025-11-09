
package de.geosphere.speechplaning.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.theme.R
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class AppSpiritualStatusStringProviderTest : StringSpec({

    lateinit var mockContext: Context
    lateinit var stringProvider: AppSpiritualStatusStringProvider

    beforeTest {
        mockContext = mockk()
        stringProvider = AppSpiritualStatusStringProvider(mockContext)
    }

    "getStringForSpiritualStatus with MINISTERIAL_SERVANT returns correct string" {
        // Given
        val expectedString = "Ministerial Servant"
        every { mockContext.getString(R.string.spiritual_status_ministerial_servant) } returns expectedString

        // When
        val result = stringProvider.getStringForSpiritualStatus(SpiritualStatus.MINISTERIAL_SERVANT)

        // Then
        result shouldBe expectedString
    }

    "getStringForSpiritualStatus with ELDER returns correct string" {
        // Given
        val expectedString = "Elder"
        every { mockContext.getString(R.string.spiritual_status_elder) } returns expectedString

        // When
        val result = stringProvider.getStringForSpiritualStatus(SpiritualStatus.ELDER)

        // Then
        result shouldBe expectedString
    }

    "getStringForSpiritualStatus with UNKNOWN returns correct string" {
        // Given
        val expectedString = "Unknown"
        every { mockContext.getString(R.string.spiritual_status_unknown) } returns expectedString

        // When
        val result = stringProvider.getStringForSpiritualStatus(SpiritualStatus.UNKNOWN)

        // Then
        result shouldBe expectedString
    }

    "getUnknownSpiritualStatusString returns correct string" {
        // Given
        val expectedString = "Unknown"
        every { mockContext.getString(R.string.spiritual_status_unknown) } returns expectedString

        // When
        val result = stringProvider.getUnknownSpiritualStatusString()

        // Then
        result shouldBe expectedString
    }
})
