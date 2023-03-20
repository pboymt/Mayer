package icu.pboymt.mayer.ui

import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import icu.pboymt.mayer.R
import icu.pboymt.mayer.ui.component.TabTitle
import icu.pboymt.mayer.ui.navigation.MayerBottomNavigationBar
import icu.pboymt.mayer.ui.navigation.MayerNavigationActions
import icu.pboymt.mayer.ui.navigation.MayerRoute
import icu.pboymt.mayer.ui.navigation.MayerTopLevelDestination

@Keep
data class MayerPage(
    val route: String,
    val title: String,
    val content: @Composable () -> Unit,
)

@Composable
fun MayerApp(
    pages: List<MayerPage>,
    scriptRunning: Boolean,
    floatingButtonAction: () -> Unit,
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        MayerNavigationActions(navController)
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination =
        navBackStackEntry?.destination?.route ?: MayerRoute.HOME

    MayerAppContent(
        navController = navController,
        selectedDestination = selectedDestination,
        navigateToTopLevelDestination = navigationActions::navigateTo,
        pages = pages,
        scriptRunning = scriptRunning,
        floatingButtonAction = floatingButtonAction,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MayerAppContent(
    navController: NavHostController, selectedDestination: String,
    navigateToTopLevelDestination: (MayerTopLevelDestination) -> Unit,
    pages: List<MayerPage>,
    scriptRunning: Boolean,
    floatingButtonAction: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            MayerBottomNavigationBar(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigateToTopLevelDestination
            )
        },
        topBar = {
            TabTitle(title = pages.find { it.route == selectedDestination }?.title ?: "")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { floatingButtonAction() }) {
                if (scriptRunning) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_stop_24),
                        contentDescription = "Add",
                        tint = Color.Red
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            MayerNavHost(
                navController = navController,
//                modifier = Modifier.weight(1f),
                pages = pages,
            )
        }

    }
}

@Composable
private fun MayerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    pages: List<MayerPage>,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MayerRoute.HOME,
    ) {
        pages.forEach { page ->
            composable(page.route) {
                page.content()
            }
        }
    }
}

@Composable
private fun EmptyComingSoon() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = System.currentTimeMillis().toString(),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}