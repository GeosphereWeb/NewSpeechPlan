package de.geosphere.speechplaning.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.geosphere.speechplaning.ui.navigation.Screen

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
            HorizontalDivider()
        }
    }
}
