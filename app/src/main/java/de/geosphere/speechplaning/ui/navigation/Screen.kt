package de.geosphere.speechplaning.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object SpeakerRoute : Screen()

    @Serializable
    data object PlaningRoute : Screen()

    @Serializable
    data object SpeechesRoute : Screen()
}
