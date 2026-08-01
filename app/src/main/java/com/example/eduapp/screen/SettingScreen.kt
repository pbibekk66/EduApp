package com.example.eduapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.eduapp.viewmodel.AppViewModel

/**
 * Screen that allows users to configure application settings.
 * Includes features for:
 * - Toggling Dark/Light Mode
 * - Enabling/Disabling Sound
 * - Adjusting Font Size
 * - Selecting Game Difficulty Level
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    username: String,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    // UI State for dropdown menu
    var expanded by remember { mutableStateOf(false) }
    val levels = listOf("Level 1: Beginner", "Level 2: Intermediate", "Level 3: Advanced")
    var selectedLevel by remember { mutableStateOf(levels[0]) }

    // Collect settings state from ViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BibekEduApp", fontWeight = FontWeight.SemiBold) },
                actions = {
                    // Navigate to scores screen
                    IconButton(onClick = { navController.navigate("score") }) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Score List"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SETTING",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            // FEATURE: Dark Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FEATURE: Sound Effects Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Enable Sound", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FEATURE: Font Size Adjustment Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Font Size", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = fontSizeMultiplier,
                    onValueChange = { viewModel.setFontSizeMultiplier(it) },
                    valueRange = 0.8f..1.5f,
                    steps = 6
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FEATURE: Game Level Selection
            Text(
                text = "Select a level",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(8.dp)
                ) {
                    Text(text = selectedLevel, style = MaterialTheme.typography.bodyLarge)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    levels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                selectedLevel = level
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Start Game Button
            Button(
                onClick = { 
                    val levelValue = when(selectedLevel) {
                        "Level 1: Beginner" -> "1"
                        "Level 2: Intermediate" -> "2"
                        "Level 3: Advanced" -> "3"
                        else -> "1"
                    }
                    navController.navigate("game/$username/$levelValue") 
                },
                modifier = Modifier.width(120.dp),
                shape = CircleShape
            ) {
                Text(
                    text = "GO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
