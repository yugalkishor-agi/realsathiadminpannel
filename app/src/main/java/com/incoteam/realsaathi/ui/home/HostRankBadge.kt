package com.incoteam.realsaathi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HostRankBadge(
    rank: String,
    totalCallMinutes: Int,
    modifier: Modifier = Modifier
) {
    val title = rank.trim().replaceFirstChar { it.uppercase() }.ifBlank { "Starter" }
    val normalizedRank = rank.trim().lowercase()
    val accent = when (rank.lowercase()) {
        "bronze" -> Color(0xFFCD8B5A)
        "silver" -> Color(0xFFC7D2E0)
        "gold" -> Color(0xFFFFCC66)
        "platinum" -> Color(0xFF8FD8FF)
        "diamond" -> Color(0xFFBFA7FF)
        else -> Color(0xFFAEB7C6)
    }
    val (nextRank, currentFloor, nextFloor) = when (normalizedRank) {
        "starter" -> Triple("Bronze", 0, 300)
        "bronze" -> Triple("Silver", 300, 600)
        "silver" -> Triple("Gold", 600, 1000)
        "gold" -> Triple("Platinum", 1000, 1500)
        "platinum" -> Triple("Diamond", 1500, 2200)
        else -> Triple("", 2200, 2200)
    }
    val progress = if (nextFloor > currentFloor) {
        ((totalCallMinutes - currentFloor).toFloat() / (nextFloor - currentFloor)).coerceIn(0f, 1f)
    } else 1f
    val minutesRemaining = (nextFloor - totalCallMinutes).coerceAtLeast(0)
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .background(accent, CircleShape)
                .padding(4.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Rank: $title", color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("• $totalCallMinutes call minutes", color = Color.White.copy(alpha = 0.68f), fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .background(accent, RoundedCornerShape(999.dp))
                )
            }
            Text(
                text = if (nextRank.isBlank()) "Maximum rank reached" else "$minutesRemaining min to $nextRank",
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 10.sp
            )
        }
    }
}
