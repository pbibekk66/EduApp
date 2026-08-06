package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.navigation.NavHostController
import com.example.eduapp.helper.playSound
import com.example.eduapp.helper.rememberAssetImage
import com.example.eduapp.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * The main Game Engine screen. 
 * This screen handles:
 * 1. Gameplay logic (Timers, Scoring, Levels).
 * 2. Visual presentation (Adaptive layout, Modern UI).
 * 3. Validation and User Feedback.
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
    
    // State preservation for rotation support using rememberSaveable.
    // We use ArrayList to ensure the list of images survives phone rotation without reshuffling.
    var puzzleImages by rememberSaveable(level) { mutableStateOf(arrayListOf<String>()) }
    var currentPuzzleIndex by rememberSaveable { mutableIntStateOf(0) }
    var lastHandledIndex by rememberSaveable { mutableIntStateOf(-1) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var answerText by rememberSaveable { mutableStateOf("") }
    var secondsElapsed by rememberSaveable { mutableIntStateOf(0) }
    var questionTimer by rememberSaveable { mutableIntStateOf(30) }
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()

    // FEATURE: Random Question Generation
    // Only fetch and shuffle questions if the list is empty (e.g., first start of the level).
    LaunchedEffect(level) {
        if (puzzleImages.isEmpty()) {
            withContext(Dispatchers.IO) {
                val shuffled = assetManager.list(level)?.toList()?.shuffled() ?: emptyList()
                puzzleImages = ArrayList(shuffled)
            }
        }
    }

    // Dialog state
    var showResultDialog by rememberSaveable { mutableStateOf(false) }
    val allScores by viewModel.users.collectAsState(initial = emptyList())
    
    val scrollState = rememberScrollState()

    // FEATURE: Total Game Duration Timer.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    // FEATURE: Per-Question 30s Countdown Timer.
    // Logic handles rotation: only resets the timer to 30s if we have moved to a new puzzle index.
    LaunchedEffect(currentPuzzleIndex, puzzleImages) {
        if (puzzleImages.isNotEmpty()) {
            if (currentPuzzleIndex != lastHandledIndex) {
                questionTimer = 30
                lastHandledIndex = currentPuzzleIndex
            }
            while (questionTimer > 0) {
                delay(1000)
                questionTimer--
            }
            
            // Time Out Logic: Executed when the countdown hits zero.
            if (!showResultDialog) {
                Toast.makeText(currentContext, "Time Out!", Toast.LENGTH_SHORT).show()
                if (isSoundEnabled) playSound(currentContext, "losing")
                
                if (currentPuzzleIndex < puzzleImages.size - 1) {
                    currentPuzzleIndex++
                    answerText = ""
                } else {
                    // Save final results to Room DB when the game ends via timeout.
                    viewModel.saveGameResult(username, level, score, secondsElapsed)
                    showResultDialog = true
                }
            }
        }
    }

    // Determines the current puzzle asset path.
    val currentImageName = if (puzzleImages.isNotEmpty() && currentPuzzleIndex < puzzleImages.size) {
        puzzleImages[currentPuzzleIndex]
    } else {
        ""
    }

    val currentImagePath = if (currentImageName.isNotEmpty()) "$level/$currentImageName" else ""
    val imageBitmap = rememberAssetImage(currentImagePath)

    // Logic to extract correct numeric answer from the filename (e.g. pic_5.jpg -> 5).
    val correctAnswer = remember(currentImageName) {
        if (currentImageName.contains("_") && currentImageName.contains(".")) {
            currentImageName.substringBeforeLast(".").substringAfterLast("_")
        } else {
            ""
        }
    }

    /**
     * Formats raw seconds into a MM:SS string.
     */
    fun formatDuration(seconds: Int): String {
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Countdown timer in Red/Bold for visibility.
                        Text(
                            text = questionTimer.toString(),
                            modifier = Modifier.padding(end = 12.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        // Total game duration.
                        Text(
                            text = formatDuration(secondsElapsed),
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress tracking UI.
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

                // Puzzle Card: Displays the brainteaser image.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .padding(vertical = 8.dp),
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

                // FEATURE: Answer Input Validation.
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
                        onValueChange = { input ->
                            // Only allow numeric input. Show Toast if user enters non-numeric text.
                            if (input.all { char -> char.isDigit() }) {
                                if (input.length <= 5) answerText = input
                            } else {
                                Toast.makeText(currentContext, "wrong input answer must be in number", Toast.LENGTH_SHORT).show()
                            }
                        },
                        placeholder = { Text("?", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.width(150.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                Button(
                    onClick = {
                        // Logic to check answer and play success/fail sound.
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
                            // Save game results to database.
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

    // FEATURE: Game Result Popup.
    // Shows status (Pass/Fail) and calculates ranking relative to previous attempts.
    if (showResultDialog) {
        val maxPossibleScore = puzzleImages.size * 5
        val isPassed = score >= (maxPossibleScore * 0.5)
        // Calculates user rank by comparing against all database entries for this level.
        val rank = allScores.filter { it.level == level }.count { it.score > score } + 1

        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                navController.navigate("score")
             },
            confirmButton = {},
            title = {
                Text(
                    text = if (isPassed) "MISSION COMPLETE" else "GAME OVER, TRY AGAIN",
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPassed) {
                        Text(
                            text = "Congratulations, Mission Completed!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val rankSuffix = when (rank) {
                            1 -> "1st"
                            2 -> "2nd"
                            3 -> "3rd"
                            else -> "${rank}th"
                        }
                        Text(
                            text = "You secured $rankSuffix position!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        )
                    } else {
                        Text(
                            text = "Better luck next time!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "You secured less than 50%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Final Score: $score / $maxPossibleScore",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showResultDialog = false
                            navController.navigate("score")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("VIEW SCOREBOARD")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}
