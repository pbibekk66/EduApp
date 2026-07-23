@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.eduapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.eduapp.database.AppDatabase
import com.example.eduapp.ui.theme.EduAppTheme
import com.example.eduapp.viewmodel.AppViewModel
import com.example.eduapp.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val currentContext = applicationContext
        setContent {
            EduAppTheme {
                AppNav(currentContext)
            }
        }
    }
}
@Composable
fun AppNav(currentContext: Context){
    //obtain navController
    val navController = rememberNavController()
    //set navHost and the routes
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") { LandingScreen(navController) }
        composable("setting") { SettingScreen(navController) }
        composable("game") { GameScreen(navController) }
        composable("score") { ScoreScreen(navController) }
        composable("testDB") { TestDBScreen(currentContext) }
    }

}

//landing screen
@Composable
fun LandingScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Landing Screen") }) }
    ) {
            innerPadding ->
        Column(modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Button(onClick = {navController.navigate("setting")})
            { Text("Go to Setting") }
        }
    }
}

//Setting screen
@Composable
fun SettingScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Setting Screen") }) }
    ) {
            innerPadding ->
        Column(modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Button(onClick = {navController.navigate("game")})
            { Text("Play Game") }
        }
    }
}

//Game Screen
@Composable
fun GameScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Game Screen") }) }
    ) {
            innerPadding ->
        Column(modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Button(onClick = {navController.navigate("score")})
            { Text("Display Score") }
        }
    }
}

//Score Screen
@Composable
fun ScoreScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Score Screen") }) }
    ) {
            innerPadding ->
        Column(modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Button(onClick = {navController.navigate("landing")})
            { Text("Go back to landing") }
        }
    }
}

//test db screen
@Composable
fun TestDBScreen(currentContext: Context, modifier: Modifier = Modifier) {
    //steps to work with DB
    val db = Room.databaseBuilder(
        currentContext,
        AppDatabase::class.java,
        "app_db"
    ).build()
    val factory = AppViewModelFactory(db.appDao())
    val viewModel: AppViewModel = viewModel(factory = factory)
    val users by viewModel.users.collectAsStateWithLifecycle(initialValue = emptyList())

    var name by remember { mutableStateOf("") }
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                viewModel.addUser(name)
                name = ""
            }) {
                Text("Add User")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.clearUsers()
            }) {
                Text("Clear")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(users) { user ->
                Text(
                    text = "ID: ${user.id}, ${user.username}, score=${user.score}, level=${user.level}"
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduAppTheme {

    }
}