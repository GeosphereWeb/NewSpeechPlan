package de.geosphere.speechplaning.ui

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

@Composable
fun BottomBarComponent(
    currentDestination: NavDestination?,
    navController: NavHostController,
    currentUser: de.geosphere.speechplaning.core.model.AppUser?,
    modifier: Modifier = Modifier,
) {
    val visibleTabs = filterTabsByPermissions(currentUser)

    NavigationBar(modifier = modifier) {
        visibleTabs.forEach { navItem ->
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
                label = {
                    Text(
                        text = stringResource(id = navItem.label),
                        minLines = 2,
                        textAlign = TextAlign.Center
                    )
                },
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
                        val iconData = if (isSelected) navItem.selectedIcon else navItem.unselectedIcon
                        Icon(
                            painter = painterResource(id = iconData),
                            contentDescription = stringResource(navItem.label)
                        )
                    }
                }
            )
        }
    }
}
