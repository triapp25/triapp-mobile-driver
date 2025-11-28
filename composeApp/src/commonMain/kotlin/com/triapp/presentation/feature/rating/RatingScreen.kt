package com.triapp.presentation.feature.rating

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.triapp.TriAppTheme
import com.triapp.TriColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TripRatingFlowScreen(
    onFinished: () -> Unit
) {
    val viewModel = koinViewModel<RatingViewModel>()
    val uiState by viewModel.state.collectAsState()
    val onAction: (RatingIntent) -> Unit = viewModel::processIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RatingEffect.Finished -> onFinished()
                is RatingEffect.ShowToast -> { /* toast */ }
            }
        }
    }

    val feedbackTags = listOf(
        "✨ Clean car", "🛡 Safe driving",
        "😊 Friendly", "🎵 Good music",
        "🗺 Best route", "❄️ AC on"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎉", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Trip completed!", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("How was your experience?", color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    DriverInfoRow()

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Rate the driver", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    StarRatingBar(
                        rating = uiState.rating,
                        onRatingChanged = { onAction(RatingIntent.SelectRating(it)) }
                    )

                    AnimatedVisibility(
                        visible = uiState.rating > 0,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {

                            Spacer(modifier = Modifier.height(32.dp))

                            Text("What did you like most?", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            FlowLayoutLikeRow(
                                tags = feedbackTags,
                                selectedTags = uiState.selectedTags
                            ) { tag ->
                                onAction(RatingIntent.ToggleTag(tag))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text("Leave a comment (optional)", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                if (uiState.comment.isEmpty()) {
                                    Text(
                                        "Tell us more about your trip...",
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    )
                                }
                                BasicTextField(
                                    value = uiState.comment,
                                    onValueChange = {
                                        onAction(RatingIntent.UpdateComment(it))
                                    },
                                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary, fontSize = 14.sp),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💝", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Add a tip", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Reward good service", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TipButton("R$ 2", uiState.selectedTip == "2", Modifier.weight(1f)) {
                                            onAction(RatingIntent.SelectTip("2"))
                                        }
                                        TipButton("R$ 5", uiState.selectedTip == "5", Modifier.weight(1f)) {
                                            onAction(RatingIntent.SelectTip("5"))
                                        }
                                        TipButton("R$ 10", uiState.selectedTip == "10", Modifier.weight(1f)) {
                                            onAction(RatingIntent.SelectTip("10"))
                                        }
                                        TipButton("Other", uiState.selectedTip == "Other", Modifier.weight(1f)) {
                                            onAction(RatingIntent.SelectTip("Other"))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onAction(RatingIntent.Submit) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.rating > 0) MaterialTheme.colorScheme.primary else TriColors.ButtonGray,
                            contentColor = if (uiState.rating > 0) Color.Black else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = if (uiState.rating > 0) "Submit rating" else "Select a rating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable {
                            onAction(RatingIntent.Skip)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ================== Componentes Auxiliares ==================

@Composable
fun DriverInfoRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("👨‍✈️", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text("Carlos Silva", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Toyota Corolla • ABC-1234", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }

        // Preço
        Column(horizontalAlignment = Alignment.End) {
            Text("R$ 15.90", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("8 min", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun StarRatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) MaterialTheme.colorScheme.primary else Color(0xFF3A3A3C), // Branco se selecionado, Cinza escuro se vazio
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun TipButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Layout manual para simular FlowRow (Tags) em 2 linhas de 3
@Composable
fun FlowLayoutLikeRow(tags: List<String>, selectedTags: List<String>, onClick: (String) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(2).forEach { tag ->
                TagChip(tag, selectedTags.contains(tag), Modifier.weight(1f)) { onClick(tag) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(2).take(2).forEach { tag ->
                TagChip(tag, selectedTags.contains(tag), Modifier.weight(1f)) { onClick(tag) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.drop(4).take(2).forEach { tag ->
                TagChip(tag, selectedTags.contains(tag), Modifier.weight(1f)) { onClick(tag) }
            }
        }
    }
}

@Composable
fun TagChip(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(
                color = MaterialTheme.colorScheme.background, // Fundo preto puro como na imagem
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, // Borda acende se selecionado
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun RatingPreview() {
    TripRatingFlowScreen {}
}