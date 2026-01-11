package de.geosphere.speechplaning.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.navigation.Screen
import de.geosphere.speechplaning.feature.congregation.CongregationListScreen
import de.geosphere.speechplaning.feature.congregationEvent.CongregationEventListScreen
import de.geosphere.speechplaning.feature.districts.ui.DistrictListScreen
import de.geosphere.speechplaning.feature.speaker.ui.SpeakerListScreen
import de.geosphere.speechplaning.feature.speeches.ui.SpeechListScreen

@Composable
fun AppNavHostComponent(
    navController: NavHostController,
    innerPadding: PaddingValues,
    currentUser: AppUser? = null
) {
    val visibleTabs = filterTabsByPermissions(currentUser)

    NavHost(
        navController = navController,
        startDestination = visibleTabs.first().route,
        modifier = Modifier.Companion
            .padding(5.dp)
            .padding(innerPadding)
    ) {
        composable<Screen.PlaningRoute> {
            CongregationEventListScreen()
        }
        composable<Screen.SpeakerRoute> {
            SpeakerListScreen()
        }
        composable<Screen.SpeechesRoute> {
            SpeechListScreen()
        }
        composable<Screen.DistrictsRoute> {
            DistrictListScreen()
        }
        composable<Screen.CongregationRoute> {
            CongregationListScreen()
        }
    }
}
