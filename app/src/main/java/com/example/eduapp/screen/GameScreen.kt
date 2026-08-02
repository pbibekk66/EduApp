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
 * Redesigned Game Screen with immersive UI and improved visual feedback.
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
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%d:%02d:%02d", h, m, s)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level $level", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HUD Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(label = "Score", value = score.toString())
                    InfoChip(label = "Timer", value = formatDuration(secondsElapsed))
                    InfoChip(label = "Puzzle", value = "${currentPuzzleIndex + 1}/${puzzleImages.size}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Puzzle Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Answer Section
                Text(
                    text = "WHAT'S THE HIDDEN NUMBER?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    placeholder = { Text("Enter number here...", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Check Button
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
                            navController.navigate("score")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "SUBMIT ANSWER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}
