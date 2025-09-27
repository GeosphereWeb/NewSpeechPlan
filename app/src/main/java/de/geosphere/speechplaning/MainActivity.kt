package de.geosphere.speechplaning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import de.geosphere.speechplaning.data.AuthUiState
import de.geosphere.speechplaning.ui.auth.AuthViewModel
import de.geosphere.speechplaning.ui.theme.SpeechPlaningTheme
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { /* No-op, the AuthStateListener in the ViewModel will handle the result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            val viewModel: AuthViewModel = koinViewModel()
            val authState by viewModel.uiState.collectAsStateWithLifecycle()

            SpeechPlaningTheme {
                when (authState) {
                    is AuthUiState.Authenticated -> {
                        val navController = rememberNavController()
                        MainScreen(navController)
                    }
                    AuthUiState.Unauthenticated -> {
                        launchSignInFlow()
                    }
                    AuthUiState.NeedsApproval -> {
                        NeedsApprovalScreen {
                            AuthUI.getInstance().signOut(this)
                        }
                    }
                    AuthUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }
}

@Composable
fun NeedsApprovalScreen(onSignOut: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = "Dein Account wurde erfolgreich erstellt, muss aber noch von einem Administrator " +
                    "freigegeben werden.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSignOut) {
                Text("Abmelden")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopBarComponents(scrollBehavior)
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavigationItem.tabs.first().route,
                modifier = Modifier.padding(innerPadding)
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
        },

        bottomBar = {
            BottomBarComponents(currentDestination, selectedItemIndex, navController)
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBarComponents(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = Color.Unspecified
        ),
        title = {
            Text(
                "Centered Top App Bar",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun BottomBarComponents(
    currentDestination: NavDestination?,
    selectedItemIndex: Int,
    navController: NavHostController
) {
    var selectedItemIndex1 = selectedItemIndex
    NavigationBar {
        BottomNavigationItem.tabs.forEachIndexed { index, navItems ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any {
                    it.route == navItems.route::class.qualifiedName
                } == true,
                onClick = {
                    selectedItemIndex1 = index
                    navController.navigate(navItems.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(navItems.label) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (navItems.badgeCount != null) {
                                Badge {
                                    Text(text = navItems.badgeCount.toString())
                                }
                            } else if (navItems.hasNews) {
                                Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedItemIndex1 == index) {
                                navItems.selectedIcon
                            } else {
                                navItems.unselectedIcon
                            },
                            contentDescription = navItems.label
                        )
                    }
                }
            )
        }
    }
}

@Serializable
private sealed class Screen {
    @Serializable
    data object SpeakerRoute : Screen()

    @Serializable
    data object PlaningRoute : Screen()

    @Serializable
    data object SpeechesRoute : Screen()
}

private data class BottomNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val route: Screen,
    val badgeCount: Int? = null,
) {
    companion object {
        val tabs = listOf(
            BottomNavigationItem(
                label = "Plan",
                selectedIcon = Icons.Filled.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                route = Screen.PlaningRoute,
                hasNews = true,
            ),
            BottomNavigationItem(
                label = "Speakers",
                selectedIcon = Icons.Filled.FolderShared,
                unselectedIcon = Icons.Outlined.FolderShared,
                route = Screen.SpeakerRoute,
                hasNews = false,
            ),
            BottomNavigationItem(
                label = "Speeches",
                selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
                unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt,
                hasNews = false,
                route = Screen.SpeechesRoute,
                badgeCount = 45
            ),
        )
    }
}
