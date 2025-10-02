package de.geosphere.speechplaning.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.ui.theme.SpeechPlaningTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreenComponent() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    SpeechPlaningTheme {
        Scaffold(
            topBar = {
                TopBarComponent(scrollBehavior)
            },
            content = { innerPadding ->
                AppNavHostComponent(navController, innerPadding)
            },

            bottomBar = {
                BottomBarComponent(currentDestination = currentDestination, navController = navController)
            },
        )
    }
}
