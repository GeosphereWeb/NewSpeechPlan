package de.geosphere.speechplaning.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.feature.login.ui.UserViewModel
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import org.koin.androidx.compose.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreenComponent(
    onLogout: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Beobachte den aktuellen User
    val userViewModel: UserViewModel = koinViewModel()
    val currentUser by userViewModel.currentUser.collectAsStateWithLifecycle()

    SpeechPlaningTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopBarComponent(scrollBehavior, onLogout = onLogout)
                },
                content = { innerPadding ->
                    AppNavHostComponent(navController, innerPadding, currentUser)
                },

                bottomBar = {
                    BottomBarComponent(
                        currentDestination = currentDestination,
                        navController = navController,
                        currentUser = currentUser
                    )
                },
            )
        }
    }
}
