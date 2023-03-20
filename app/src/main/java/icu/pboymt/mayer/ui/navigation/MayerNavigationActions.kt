package icu.pboymt.mayer.ui.navigation

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import icu.pboymt.mayer.R

@Keep
object MayerRoute {
    const val HOME = "Home"
    const val SCRIPTS = "Scripts"
    const val SETTINGS = "Settings"
}

@Keep
data class MayerTopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int
)

class MayerNavigationActions(private val navController: NavHostController) {

    fun navigateTo(destination: MayerTopLevelDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}


val TOP_LEVEL_DESTINATIONS = listOf(
    MayerTopLevelDestination(
        route = MayerRoute.HOME,
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Default.Home,
        iconTextId = R.string.tab_home
    ),
    MayerTopLevelDestination(
        route = MayerRoute.SCRIPTS,
        selectedIcon = Icons.Default.List,
        unselectedIcon = Icons.Default.List,
        iconTextId = R.string.tab_scripts
    ),
    MayerTopLevelDestination(
        route = MayerRoute.SETTINGS,
        selectedIcon = Icons.Outlined.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = R.string.tab_settings
    ),
)