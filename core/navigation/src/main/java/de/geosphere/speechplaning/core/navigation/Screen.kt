package de.geosphere.speechplaning.core.navigation

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
sealed class Screen {
    @kotlinx.serialization.Serializable
    data object SpeakerRoute : Screen()

    @kotlinx.serialization.Serializable
    data object PlaningRoute : Screen()

    @Serializable
    data object SpeechesRoute : Screen()
}
