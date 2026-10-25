package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NexusBorderHairline
import com.example.ui.theme.NexusCardDark
import com.example.ui.theme.NexusSilver
import com.example.ui.theme.NexusSilverBright
import com.example.ui.theme.NexusSilverDark
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTitanium

fun Modifier.cyberBorder(
    color: Color = NexusBorderHairline,
    width: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(12.dp)
) = this
    .border(width = width, color = color, shape = shape)
    .clip(shape)

@Composable
fun CyberStatusBadge(
    text: String,
    statusColor: Color = NeonEmerald,
    icon: ImageVector = Icons.Default.Circle
) {
    Surface(
        color = NexusSurfaceDark,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text.uppercase(),
                color = NexusSilverBright,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Minimalist connection indicator light.
 * Green when connected, Red when disconnected/no key, Grey while checking.
 * No text labels, strictly a subtle luminous status light.
 */
@Composable
fun NexusConnectionIndicator(
    state: com.example.viewmodel.ConnectionState,
    onIndicatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (indicatorColor, isChecking) = when (state) {
        is com.example.viewmodel.ConnectionState.Online -> Color(0xFF10B981) to false
        is com.example.viewmodel.ConnectionState.Checking -> Color(0xFF64748B) to true
        is com.example.viewmodel.ConnectionState.Error,
        is com.example.viewmodel.ConnectionState.NoKey -> Color(0xFFEF4444) to false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "indicatorTransition")
    val checkingAlpha = if (isChecking) {
        val anim by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(750),
                repeatMode = RepeatMode.Reverse
            ),
            label = "checkingAlpha"
        )
        anim
    } else {
        1.0f
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onIndicatorClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer soft glow halo
        Box(
            modifier = Modifier
                .size(15.dp)
                .background(indicatorColor.copy(alpha = 0.22f * checkingAlpha), CircleShape)
        )
        // Core luminous point
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(indicatorColor.copy(alpha = checkingAlpha), CircleShape)
        )
    }
}

@Composable
fun ApiKeyWarningBanner(
    onConfigureClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = NexusCardDark,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = NeonRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gemini API Key Required",
                    color = NexusSilverBright,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Configure in AI Studio Secrets or enter key in Settings.",
                    color = NexusSilver,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onConfigureClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusSilverBright),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusSilverDark)
            ) {
                Text("Setup", fontSize = 11.5.sp)
            }
        }
    }
}

@Composable
fun CyberChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NexusBorderHairline
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = NexusSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusBorderHairline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = NexusTitanium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
