package de.geosphere.speechplaning.ui

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import de.geosphere.speechplaning.core.navigation.BottomNavigationItem

@Composable
fun BottomBarComponent(
    currentDestination: NavDestination?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        BottomNavigationItem.Companion.tabs.forEach { navItem ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == navItem.route::class.qualifiedName
            } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(navItem.label) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (navItem.badgeCount != null) {
                                Badge {
                                    Text(text = navItem.badgeCount.toString())
                                }
                            } else if (navItem.hasNews) {
                                Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) navItem.selectedIcon else navItem.unselectedIcon,
                            contentDescription = navItem.label
                        )
                    }
                }
            )
        }
    }
}
