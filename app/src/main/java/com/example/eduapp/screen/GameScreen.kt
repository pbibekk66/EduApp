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
import androidx.navigation.NavHostController
import com.example.eduapp.helper.playSound
import com.example.eduapp.helper.rememberAssetImage
import com.example.eduapp.viewmodel.AppViewModel
import kotlinx.coroutines.delay

/**
 * Main game screen where the user solves number puzzles.
 * Handles timer, score tracking, puzzle image loading, and answer validation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    currentContext: Context,
    navController: NavHostController,
    username: String,
    level: String,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val assetManager = currentContext.assets
    
    // Load puzzle images from the selected level folder in assets
    val puzzleImages: List<String> = remember(level) {
        assetManager.list(level)?.sorted() ?: emptyList()
    }

    // Game State
    var currentPuzzleIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answerText by remember { mutableStateOf("") }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    
    // Collect settings from ViewModel
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    // FEATURE: Game Timer - increments every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Determine current puzzle image path
    val currentImageName = if (puzzleImages.isNotEmpty() && currentPuzzleIndex < puzzleImages.size) {
        puzzleImages[currentPuzzleIndex]
    } else {
        ""
    }

    val currentImagePath = if (currentImageName.isNotEmpty()) "$level/$currentImageName" else ""
    val imageBitmap = rememberAssetImage(currentImagePath)

    // LOGIC: Extract correct answer from filename (pattern: ..._answer.jpg)
    val correctAnswer = remember(currentImageName) {
        if (currentImageName.contains("_") && currentImageName.contains(".")) {
            currentImageName.substringBeforeLast(".").substringAfterLast("_")
        } else {
            ""
        }
    }

    /**
     * Formats seconds into HH:MM:SS string for display.
     */
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
            // HUD: Score, Puzzle Count, and Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val totalPuzzles = puzzleImages.size
                Text(text = "Score: $score (/${totalPuzzles * 5})", style = MaterialTheme.typography.bodySmall)
                Text(text = "Puzzle: ${currentPuzzleIndex + 1} (/$totalPuzzles)", style = MaterialTheme.typography.bodySmall)
                Text(text = "Duration: ${formatDuration(secondsElapsed)}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User & Level Info
            Text(text = "User: $username  Level: $level", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(64.dp))

            // Puzzle Image Display
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
                    Text("No puzzles found for this level.", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Answer Input Field
            OutlinedTextField(
                value = answerText,
                onValueChange = { answerText = it },
                placeholder = { Text("Enter your answer ...") },
                modifier = Modifier.fillMaxWidth(0.9f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Check Answer Button
            Button(
                onClick = {
                    // Validate answer and play sound if enabled
                    if (answerText.trim() == correctAnswer) {
                        score += 5
                        if (isSoundEnabled) playSound(currentContext, "winning")
                    } else {
                        if (isSoundEnabled) playSound(currentContext, "losing")
                    }
                    
                    // Proceed to next puzzle or end game
                    if (currentPuzzleIndex < puzzleImages.size - 1) {
                        currentPuzzleIndex++
                        answerText = ""
                    } else {
                        // Game Over - Save result to database and Navigate to Score Screen
                        viewModel.saveGameResult(username, level, score, secondsElapsed)
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
