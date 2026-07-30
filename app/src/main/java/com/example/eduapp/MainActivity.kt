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

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "eduapp-db"
        ).build()

        val viewModelFactory = AppViewModelFactory(db.appDao())

        setContent {
            EduAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appViewModel: AppViewModel = viewModel(factory = viewModelFactory)
                    AppNav(applicationContext, appViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNav(currentContext: Context, viewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "landing",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("landing") { LandingScreen(navController) }
        
        composable(
            route = "setting/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            SettingScreen(navController, username)
        }
        
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
        
        composable("score") { ScoreScreen(navController, viewModel) }
        composable("testDB") { TestDBScreen(currentContext) }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduAppTheme {
        // Preview content
    }
}
