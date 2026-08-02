package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.eduapp.helper.playSound
import com.example.eduapp.helper.rememberAssetImage
import com.example.eduapp.viewmodel.AppViewModel
import kotlinx.coroutines.delay

/**
 * Tightly designed Game Screen that fits perfectly on all screen sizes.
 * Optimized layout for better visibility and faster gameplay.
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
    val puzzleImages: List<String> = remember(level) {
        assetManager.list(level)?.sorted() ?: emptyList()
    }

    var currentPuzzleIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answerText by remember { mutableStateOf("") }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    // Dialog state
    var showResultDialog by remember { mutableStateOf(false) }
    val allScores by viewModel.users.collectAsState(initial = emptyList())

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

    val correctAnswer = remember(currentImageName) {
        if (currentImageName.contains("_") && currentImageName.contains(".")) {
            currentImageName.substringBeforeLast(".").substringAfterLast("_")
        } else {
            ""
        }
    }

    fun formatDuration(seconds: Int): String {
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Level $level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(username, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatDuration(secondsElapsed),
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Compact HUD Progress
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (currentPuzzleIndex + 1).toFloat() / puzzleImages.size },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${currentPuzzleIndex + 1}/${puzzleImages.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "SCORE: $score",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Puzzle Card - Fills available space
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "Puzzle Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Answer Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FIND THE NUMBER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { if (it.length <= 5) answerText = it },
                        placeholder = { Text("?", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.width(150.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (answerText.trim() == correctAnswer) {
                            score += 5
                            if (isSoundEnabled) playSound(currentContext, "winning")
                        } else {
                            if (isSoundEnabled) playSound(currentContext, "losing")
                        }
                        
                        if (currentPuzzleIndex < puzzleImages.size - 1) {
                            currentPuzzleIndex++
                            answerText = ""
                        } else {
                            viewModel.saveGameResult(username, level, score, secondsElapsed)
                            showResultDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "SUBMIT ANSWER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showResultDialog) {
        val maxPossibleScore = puzzleImages.size * 5
        val isPassed = score >= (maxPossibleScore * 0.5)
        val rank = allScores.filter { it.level == level }.count { it.score > score } + 1

        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                navController.navigate("score")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        navController.navigate("score")
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VIEW SCOREBOARD")
                }
            },
            title = {
                Text(
                    text = if (isPassed) "MISSION COMPLETE" else "GAME OVER, TRY AGAIN",
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPassed) {
                        Text(
                            text = "Congratulations, Mission Complited!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        val rankSuffix = when (rank) {
                            1 -> "1st"
                            2 -> "2nd"
                            3 -> "3rd"
                            else -> "${rank}th"
                        }
                        Text(
                            text = "You secured $rankSuffix position!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    } else {
                        Text(
                            text = "You loose!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "You secured less than 50%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Score: $score / $maxPossibleScore",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}
