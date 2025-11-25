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
import org.jetbrains.compose.ui.tooling.preview.Preview

// ================== Cores e Estilos ==================
object RatingColors {
    val Background = Color(0xFF000000)
    val CardBg = Color(0xFF1C1C1E)
    val InputBg = Color(0xFF2C2C2E)
    val White = Color(0xFFFFFFFF)
    val GrayText = Color(0xFF8E8E93)
    val DisabledButton = Color(0xFF3A3A3C)
    val TagBg = Color(0xFF000000)
}

@Composable
fun TripRatingScreen(onFinished: () -> Unit) {
    // Estados da tela
    var rating by remember { mutableIntStateOf(0) } // 0 = Nenhuma estrela selecionada
    var comment by remember { mutableStateOf("") }
    var selectedTip by remember { mutableStateOf<String?>(null) }

    // Lista de Tags (Exemplo estático)
    val feedbackTags = listOf(
        "✨ Clean car", "🛡 Safe driving",
        "😊 Friendly", "🎵 Good music",
        "🗺 Best route", "❄️ AC on"
    )
    val selectedTags = remember { mutableStateListOf<String>() }

    Scaffold(
        containerColor = RatingColors.Background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Toggle de tema (decorativo)
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(RatingColors.InputBg, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Outlined.WbSunny, null, tint = RatingColors.GrayText)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Cabeçalho (Ícone de festa)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(RatingColors.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎉", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Trip completed!", color = RatingColors.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("How was your experience?", color = RatingColors.GrayText, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Card Principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RatingColors.CardBg)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Info do Motorista
                    DriverInfoRow()

                    Spacer(modifier = Modifier.height(32.dp))

                    // Estrelas
                    Text("Rate the driver", color = RatingColors.GrayText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    StarRatingBar(
                        rating = rating,
                        onRatingChanged = { newRating -> rating = newRating }
                    )

                    // ==============================================================
                    // ÁREA EXPANSÍVEL - Só aparece se rating > 0
                    // ==============================================================
                    AnimatedVisibility(
                        visible = rating > 0,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Spacer(modifier = Modifier.height(32.dp))

                            // Tags
                            Text("What did you like most?", color = RatingColors.GrayText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid simples de Tags
                            FlowLayoutLikeRow(tags = feedbackTags, selectedTags = selectedTags) { tag ->
                                if (selectedTags.contains(tag)) selectedTags.remove(tag) else selectedTags.add(tag)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Comentário
                            Text("Leave a comment (optional)", color = RatingColors.GrayText, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(RatingColors.InputBg, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                if (comment.isEmpty()) {
                                    Text("Tell us more about your trip...", color = RatingColors.GrayText.copy(alpha = 0.5f))
                                }
                                BasicTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    textStyle = TextStyle(color = RatingColors.White, fontSize = 14.sp),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Gorjeta (Tips)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RatingColors.InputBg, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💝", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Add a tip", color = RatingColors.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Reward good service", color = RatingColors.GrayText, fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Passe o Modifier.weight(1f) aqui, pois estamos dentro de uma Row
                                        TipButton("R$ 2", selectedTip == "2", Modifier.weight(1f)) { selectedTip = "2" }
                                        TipButton("R$ 5", selectedTip == "5", Modifier.weight(1f)) { selectedTip = "5" }
                                        TipButton("R$ 10", selectedTip == "10", Modifier.weight(1f)) { selectedTip = "10" }
                                        TipButton("Other", selectedTip == "Other", Modifier.weight(1f)) { selectedTip = "Other" }
                                    }
                                }
                            }
                        }
                    } // Fim do AnimatedVisibility

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botão Principal (Muda de estado)
                    Button(
                        onClick = { if (rating > 0) onFinished() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (rating > 0) RatingColors.White else RatingColors.DisabledButton,
                            contentColor = if (rating > 0) Color.Black else RatingColors.GrayText
                        )
                    ) {
                        Text(
                            text = if (rating > 0) "Submit rating" else "Select a rating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Skip",
                        color = RatingColors.GrayText,
                        modifier = Modifier.clickable { onFinished() }
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
                .background(RatingColors.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("👨‍✈️", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text("Carlos Silva", color = RatingColors.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Toyota Corolla • ABC-1234", color = RatingColors.GrayText, fontSize = 12.sp)
        }

        // Preço
        Column(horizontalAlignment = Alignment.End) {
            Text("R$ 15.90", color = RatingColors.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("8 min", color = RatingColors.GrayText, fontSize = 12.sp)
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
                tint = if (i <= rating) RatingColors.White else Color(0xFF3A3A3C), // Branco se selecionado, Cinza escuro se vazio
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
                if (isSelected) RatingColors.White else RatingColors.InputBg.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) Color.Black else RatingColors.GrayText,
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
                color = RatingColors.TagBg, // Fundo preto puro como na imagem
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if(isSelected) RatingColors.White else RatingColors.InputBg, // Borda acende se selecionado
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text,
            color = if (isSelected) RatingColors.White else RatingColors.GrayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun RatingPreview() {
    TripRatingScreen {}
}