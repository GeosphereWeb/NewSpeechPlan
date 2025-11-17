package de.geosphere.speechplaning.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.geosphere.speechplaning.core.navigation.Screen
import de.geosphere.speechplaning.feature.speeches.ui.SpeechListScreen

@Composable
fun AppNavHostComponent(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavigationItem.Companion.tabs.first().route,
        modifier = Modifier.Companion
            .padding(5.dp)
            .padding(innerPadding)
    ) {
        composable<Screen.PlaningRoute> {
            HorizontalDivider()
        }
        composable<Screen.SpeakerRoute> {
            HorizontalDivider()
        }
        composable<Screen.SpeechesRoute> {
            SpeechListScreen()
        }
    }
}
