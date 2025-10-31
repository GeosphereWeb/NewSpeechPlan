package de.geosphere.speechplaning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
        // enableEdgeToEdge()
        // Das Ausblenden der Systemleisten kann auch in eine Extension-Function ausgelagert werden

        WindowCompat.getInsetsController(window, window.decorView).hidestSystemBars()

        // /////
        dummyDbBuilder()
        // /////

        setContent {
            // 1. NavController erstellen, der den Navigationszustand verwaltet.
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = koinViewModel()
            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(authViewModel = authViewModel, onLoginSuccess = {
                        // 3. Bei erfolgreichem Login zum Hauptbildschirm navigieren.
                        // popUpTo("login") { inclusive = true } entfernt den Login-Screen
                        // aus dem Back-Stack, damit der Nutzer nicht mit der Zurück-Taste
                        // dorthin zurückkehren kann.
                        navController.navigate("main") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    })
                }
                // Definiert die Route für den Hauptbildschirm
                composable("main") {
                    // Hier wird dein Hauptbildschirm angezeigt.
                    MainScreenComponent(
                        onLogout = {
                            // Navigiere zurück zum Login-Screen
                            navController.navigate("login") {
                                // Entferne den Main-Screen und alles darüber vom Back-Stack
                                authViewModel.signOut()
                                popUpTo("main") {
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

private fun WindowInsetsControllerCompat.hidestSystemBars() {
    hide(WindowInsetsCompat.Type.statusBars())
    hide(WindowInsetsCompat.Type.navigationBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
