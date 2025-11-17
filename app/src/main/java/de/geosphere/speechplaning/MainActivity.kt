package de.geosphere.speechplaning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.core.navigation.Screen
import de.geosphere.speechplaning.data.authentication.AuthUiState
import de.geosphere.speechplaning.feature.login.ui.AuthViewModel
import de.geosphere.speechplaning.feature.login.ui.LoginScreen
import de.geosphere.speechplaning.mocking.BuildDummyDBConnection
import de.geosphere.speechplaning.ui.MainScreenComponent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@Suppress("UnusedPrivateProperty")
class MainActivity :
    ComponentActivity(),
    KoinComponent {

    private val dummyDbBuilder: BuildDummyDBConnection by inject {
        parametersOf(lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        WindowCompat.getInsetsController(window, window.decorView).hidestSystemBars()

        // dummyDbBuilder()
        setContent {
            val authViewModel: AuthViewModel = koinViewModel()
            val authState by authViewModel.getAuthUiState()
                .collectAsStateWithLifecycle(initialValue = AuthUiState.Loading)

            splashScreen.setKeepOnScreenCondition {
                authState is AuthUiState.Loading
            }

            if (authState !is AuthUiState.Loading) {
                val navController = rememberNavController()
                val startDestination = when (authState) {
                    is AuthUiState.Authenticated -> Screen.Main
                    else -> Screen.Login
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable<Screen.Login> {
                        LoginScreen(authViewModel = authViewModel, onLoginSuccess = {
                            navController.navigate(Screen.Main) {
                                popUpTo(Screen.Login) {
                                    inclusive = true
                                }
                            }
                        })
                    }
                    composable<Screen.Main> {
                        MainScreenComponent(
                            onLogout = {
                                authViewModel.signOut()
                                navController.navigate(Screen.Login) {
                                    popUpTo(Screen.Main) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun WindowInsetsControllerCompat.hidestSystemBars() {
    hide(WindowInsetsCompat.Type.statusBars())
    hide(WindowInsetsCompat.Type.navigationBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
