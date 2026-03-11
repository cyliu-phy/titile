package cc.cyliu.kegels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cc.cyliu.kegels.ui.exercise.CompletionScreen
import cc.cyliu.kegels.ui.exercise.ExerciseScreen
import cc.cyliu.kegels.ui.home.HomeScreen
import cc.cyliu.kegels.ui.onboarding.OnboardingScreen
import cc.cyliu.kegels.ui.settings.SettingsScreen
import cc.cyliu.kegels.ui.stats.StatsScreen
import cc.cyliu.kegels.data.datastore.AppPreferences
import cc.cyliu.kegels.data.datastore.dataStore
import cc.cyliu.kegels.ui.theme.Kegels_ktTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // On API < 33, re-apply the saved locale every time the Activity (re)creates its context,
    // including after recreate() is called from the language switcher.
    override fun attachBaseContext(newBase: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val tag = runBlocking {
                newBase.dataStore.data.first()[AppPreferences.LANGUAGE_TAG]
            }
            if (tag != null && tag != "system") {
                val locale = Locale.forLanguageTag(tag)
                val config = Configuration(newBase.resources.configuration)
                config.setLocale(locale)
                super.attachBaseContext(newBase.createConfigurationContext(config))
                return
            }
        }
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kegels_ktTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsStateWithLifecycle()
    when (isOnboardingComplete) {
        null -> Box(modifier = Modifier.fillMaxSize()) // brief loading state
        false -> OnboardingScreen(
            onFinished = { languageTag ->
                viewModel.saveLanguage(languageTag)
                viewModel.completeOnboarding()
            }
        )
        true -> KagelApp()
    }
}

// ── Navigation ────────────────────────────────────────────────────────────────

sealed class Screen(
    val route: String,
    val labelRes: Int,
    val icon: @Composable (contentDescription: String) -> Unit
) {
    object Home : Screen("home", R.string.nav_home,
        { desc -> Icon(Icons.Filled.Home, contentDescription = desc) })
    object Exercise : Screen("exercise", R.string.nav_exercise,
        { desc -> Icon(painterResource(R.drawable.ic_fitness_center), contentDescription = desc) })
    object Stats : Screen("stats", R.string.nav_stats,
        { desc -> Icon(painterResource(R.drawable.ic_show_chart), contentDescription = desc) })
    object Settings : Screen("settings", R.string.nav_settings,
        { desc -> Icon(Icons.Filled.Settings, contentDescription = desc) })
}

private val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Exercise,
    Screen.Stats,
    Screen.Settings,
)

@Composable
fun KagelApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val rationaleMsg = stringResource(R.string.permission_notification_rationale)
    val openSettingsLabel = stringResource(R.string.permission_open_settings)

    // Fallback permission request (for users who somehow reach main app without onboarding)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = rationaleMsg,
                    actionLabel = openSettingsLabel,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon(stringResource(screen.labelRes)) },
                        label = { Text(stringResource(screen.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Exercise.route) {
                ExerciseScreen(navController = navController)
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = "completion/{totalCount}/{durationSeconds}",
                arguments = listOf(
                    navArgument("totalCount") { type = NavType.IntType },
                    navArgument("durationSeconds") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                CompletionScreen(
                    navController = navController,
                    totalCount = backStackEntry.arguments?.getInt("totalCount") ?: 0,
                    durationSeconds = backStackEntry.arguments?.getLong("durationSeconds") ?: 0L
                )
            }
        }
    }
}
