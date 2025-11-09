package de.geosphere.speechplaning.core.ui

import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.core.ui.atoms.AvatarProvider
import de.geosphere.speechplaning.core.ui.atoms.AvatarResourceProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class AvatarProviderTest : FunSpec({

    val mockResourceProvider = mockk<AvatarResourceProvider>()
    val avatarProvider = AvatarProvider(mockResourceProvider)

    context("getAvatar") {
        test("should return the drawable from the provider for ELDER") {
            // Given: Der Provider ist konfiguriert, eine bestimmte ID zurückzugeben
            val expectedDrawableId = 12345 // Eine Fake-ID für den Test
            every { mockResourceProvider.getAvatarResource(SpiritualStatus.ELDER) } returns expectedDrawableId

            // When: getAvatar wird aufgerufen
            val actualDrawable = avatarProvider.getAvatar(SpiritualStatus.ELDER)

            // Then: Das Ergebnis sollte genau die ID vom Provider sein
            actualDrawable shouldBe expectedDrawableId
        }

        test("should return the drawable from the provider for MINISTERIAL_SERVANT") {
            // Given
            val expectedDrawableId = 67890
            every { mockResourceProvider.getAvatarResource(SpiritualStatus.MINISTERIAL_SERVANT) } returns
                expectedDrawableId

            // When
            val actualDrawable = avatarProvider.getAvatar(SpiritualStatus.MINISTERIAL_SERVANT)

            // Then
            actualDrawable shouldBe expectedDrawableId
        }
    }
})
