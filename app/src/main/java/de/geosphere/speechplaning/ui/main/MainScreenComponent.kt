package de.geosphere.speechplaning.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.ui.theme.SpeechPlaningTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreenComponent(onLogout: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    SpeechPlaningTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopBarComponent(scrollBehavior, onLogout = onLogout)
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
}
