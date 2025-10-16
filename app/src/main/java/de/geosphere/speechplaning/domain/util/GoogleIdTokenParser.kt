package de.geosphere.speechplaning.domain.util

import android.os.Bundle
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Ein Wrapper um die statische Methode GoogleIdTokenCredential.createFrom,
 * um die Abhängigkeit vom Android-Framework in Unit-Tests zu entkoppeln.
 */
class GoogleIdTokenParser {
    /**
     * Extrahiert das ID-Token aus dem Bundle der Credential-Daten.
     * @param data Das Bundle aus GetCredentialResponse.credential.data
     * @return Das extrahierte ID-Token als String.
     */
    fun parseIdToken(data: Bundle): String {
        // Wir rufen unsere eigene, testbare Wrapper-Funktion auf.
        return createCredentialFrom(data).idToken
    }

    /**
     * Kapselt den problematischen statischen Aufruf.
     * Diese Funktion kann in Tests leicht durch einen Mock ersetzt werden.
     */
    internal fun createCredentialFrom(data: Bundle): GoogleIdTokenCredential {
        return GoogleIdTokenCredential.createFrom(data)
    }
}
