package de.geosphere.speechplaning.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Login : Screen()

    @Serializable
    data object Main : Screen()

    @Serializable
    data object SpeakerRoute : Screen()

    @Serializable
    data object PlaningRoute : Screen()

    @Serializable
    data object SpeechesRoute : Screen()

    @Serializable
    data object DistrictsRoute : Screen()

    @Serializable
    data object CongregationRoute : Screen()
}
