@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.eduapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.eduapp.database.AppDatabase
import com.example.eduapp.screen.GameScreen
import com.example.eduapp.screen.LandingScreen
import com.example.eduapp.screen.ScoreScreen
import com.example.eduapp.screen.SettingScreen
import com.example.eduapp.screen.TestDBScreen
import com.example.eduapp.ui.theme.EduAppTheme
import com.example.eduapp.viewmodel.AppViewModel
import com.example.eduapp.viewmodel.AppViewModelFactory

/**
 * MainActivity is the main entry point of the NumNinja app.
 * It sets up the database, the shared ViewModel, and handles the top-level UI and navigation.
 */
class MainActivity : ComponentActivity() {
    // Database instance for persisting game scores and user data.
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Makes the app layout span the entire screen (immersive feel).
        enableEdgeToEdge()
        
        // Initialize the Room Database. 
        // We use a singleton-like pattern here to ensure the database is built only once.
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "eduapp-db"
        ).build()

        // Setup the Factory to provide the AppDao to our ViewModel.
        val viewModelFactory = AppViewModelFactory(db.appDao())

        // setContent is where we define the UI using Jetpack Compose.
        setContent {
            // Instantiate the shared ViewModel that all screens will use.
            val appViewModel: AppViewModel = viewModel(factory = viewModelFactory)
            
            // Observe the theme and font size settings from the ViewModel.
            // When these change in the Settings screen, the entire app UI will recompose.
            val isDarkTheme by appViewModel.isDarkTheme.collectAsState()
            val fontSizeMultiplier by appViewModel.fontSizeMultiplier.collectAsState()
            
            // Wrap the app in our custom theme which handles dynamic colors, dark mode, and font scaling.
            EduAppTheme(darkTheme = isDarkTheme, fontSizeMultiplier = fontSizeMultiplier) {
                // Surface provides the background color and fills the screen.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Kick off the navigation controller.
                    AppNav(applicationContext, appViewModel)
                }
            }
        }
    }
}

/**
 * AppNav defines the navigation graph of the application.
 * It manages which screen is shown and how data flows between them.
 */
@Composable
fun AppNav(currentContext: Context, viewModel: AppViewModel) {
    // Holds the state of the navigation (which screen we are currently on).
    val navController = rememberNavController()
    
    // NavHost acts as a container for all the screens (composables).
    NavHost(
        navController = navController,
        startDestination = "landing", // The app starts on the Landing Screen.
        modifier = Modifier.fillMaxSize()
    ) {
        // Landing Screen: The entry point where the user enters their name.
        composable("landing") { LandingScreen(navController) }
        
        // Setting Screen: Receives the 'username' as a navigation parameter.
        composable(
            route = "setting/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            SettingScreen(navController, username, viewModel = viewModel)
        }
        
        // Game Screen: Takes both 'username' and 'level' to start the math challenge.
        composable(
            route = "game/{username}/{level}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("level") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val level = backStackEntry.arguments?.getString("level") ?: "1"
            GameScreen(currentContext, navController, username, level, viewModel)
        }
        
        // Score Screen: Displays the ranked leaderboard.
        composable("score") { ScoreScreen(navController, viewModel) }
        
        // TestDB Screen: A utility screen for debugging database entries.
        composable("testDB") { TestDBScreen(viewModel) }
    }
}

/**
 * Preview function to see the app's theme in the Android Studio design tab.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduAppTheme {
        // Preview content
    }
}
