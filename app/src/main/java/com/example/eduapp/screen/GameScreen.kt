package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.eduapp.helper.rememberAssetImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    currentContext: Context,
    navController: NavHostController,
    username: String,
    level: String,
    modifier: Modifier = Modifier
) {
    val assetManager = currentContext.assets
    
    // Explicitly specify List<String> type to resolve inference issues.
    // .sorted() returns a List, so we use emptyList() for the fallback.
    val puzzleImages: List<String> = remember(level) {
        assetManager.list(level)?.sorted() ?: emptyList()
    }

    var currentPuzzleIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answerText by remember { mutableStateOf("") }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    // Timer logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val currentImageName = if (puzzleImages.isNotEmpty() && currentPuzzleIndex < puzzleImages.size) {
        puzzleImages[currentPuzzleIndex]
    } else {
        ""
    }

    val currentImagePath = if (currentImageName.isNotEmpty()) "$level/$currentImageName" else ""
    val imageBitmap = rememberAssetImage(currentImagePath)

    // Extract answer from filename (e.g., "level01_pic04_55.jpg" -> "55")
    val correctAnswer = remember(currentImageName) {
        if (currentImageName.contains("_") && currentImageName.contains(".")) {
            currentImageName.substringBeforeLast(".").substringAfterLast("_")
        } else {
            ""
        }
    }

    fun formatDuration(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%d:%02d:%02d", h, m, s)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BibekEduApp", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Row: Score, Puzzle Count, Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val totalPuzzles = puzzleImages.size
                Text(text = "Score: $score (/${totalPuzzles * 5})", fontSize = 14.sp)
                Text(text = "Puzzle: ${currentPuzzleIndex + 1} (/$totalPuzzles)", fontSize = 14.sp)
                Text(text = "Duration: ${formatDuration(secondsElapsed)}", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info
            Text(text = "User: $username  Level: $level", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(64.dp))

            // Puzzle Image
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Puzzle Image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (currentImagePath.isNotEmpty()) {
                    CircularProgressIndicator()
                } else {
                    Text("No puzzles found for this level.")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Answer Input
            OutlinedTextField(
                value = answerText,
                onValueChange = { answerText = it },
                placeholder = { Text("Enter your answer ...") },
                modifier = Modifier.fillMaxWidth(0.9f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Check Button
            Button(
                onClick = {
                    if (answerText.trim() == correctAnswer) {
                        score += 5
                    }
                    
                    if (currentPuzzleIndex < puzzleImages.size - 1) {
                        currentPuzzleIndex++
                        answerText = ""
                    } else {
                        // Game Over - Navigate to Score Screen
                        navController.navigate("score")
                    }
                },
                modifier = Modifier.width(180.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "CHECK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
