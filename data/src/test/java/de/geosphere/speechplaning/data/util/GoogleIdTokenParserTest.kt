package de.geosphere.speechplaning.data.util

import android.os.Bundle
import androidx.compose.ui.Modifier.Companion.then
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.common.base.Verify.verify
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify

class GoogleIdTokenParserTest : BehaviorSpec({

    // Wir brauchen kein beforeSpec/afterSpec und kein mockkStatic mehr!

    given("a GoogleIdTokenParser") {
        `when`("parseIdToken is called with a Bundle") {
            then("it should call the internal credential creator and return the idToken") {
                // Arrange (Vorbereiten)
                val mockBundle = mockk<Bundle>()
                val mockCredential = mockk<GoogleIdTokenCredential>()
                val expectedToken = "my-test-id-token"

                // Erstelle einen "Spy" von der echten Parser-Instanz.
                // Ein Spy ist ein teilweiser Mock - echte Methoden werden aufgerufen,
                // es sei denn, wir überschreiben sie.
                val googleIdTokenParserSpy = spyk<GoogleIdTokenParser>()

                // --- HIER IST DIE FINALE LÖSUNG ---
                // Wir überschreiben nur unsere neue, interne Wrapper-Funktion.
                // Die problematische statische Methode wird so niemals erreicht.
                every { googleIdTokenParserSpy.createCredentialFrom(mockBundle) } returns mockCredential

                // Wir definieren weiterhin, was die idToken-Eigenschaft zurückgibt.
                every { mockCredential.idToken } returns expectedToken

                // Act (Ausführen)
                // Wir rufen die öffentliche Methode auf dem Spy auf.
                val actualToken = googleIdTokenParserSpy.parseIdToken(mockBundle)

                // Assert (Überprüfen)
                actualToken shouldBe expectedToken

                // Verifizieren, dass unsere interne, gemockte Methode aufgerufen wurde.
                verify(exactly = 1) { googleIdTokenParserSpy.createCredentialFrom(mockBundle) }
                verify(exactly = 1) { mockCredential.idToken }
            }
        }
    }
})
