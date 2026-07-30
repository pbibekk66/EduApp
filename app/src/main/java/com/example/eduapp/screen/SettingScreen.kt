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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavHostController, username: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val levels = listOf("Level 1: Beginner", "Level 2: Intermediate", "Level 3: Advanced")
    var selectedLevel by remember { mutableStateOf(levels[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BibekEduApp", fontWeight = FontWeight.SemiBold) },
                actions = {
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
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "SETTING",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Select a level",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(8.dp)
                ) {
                    Text(text = selectedLevel, fontSize = 20.sp)
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

            Spacer(modifier = Modifier.height(48.dp))

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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
