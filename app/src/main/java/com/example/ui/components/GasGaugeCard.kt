package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.LocalizedStrings
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SafeEmerald
import com.example.ui.theme.WarningAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GasGaugeCard(
    gasPPM: Int,
    isDarkMode: Boolean,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier
) {
    val targetFraction = ((gasPPM - 50f) / (950f - 50f)).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "gasGaugeSweep"
    )

    val (statusLabel, statusSub, targetColor) = when {
        gasPPM > 350 -> Triple(strings.badgeDanger, strings.gaugeSubDanger, DangerRed)
        gasPPM > 240 -> Triple(strings.badgeWarning, strings.gaugeSubWarning, WarningAmber)
        else -> Triple(strings.badgeSafe, strings.gaugeSubSafe, AccentTeal)
    }

    val activeColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "gaugeActiveColor"
    )

    val trackColor = if (isDarkMode) Color.White.copy(alpha = 0.10f) else Color(0xFF0F172A).copy(alpha = 0.10f)

    FuturisticGlassCard(
        isDarkMode = isDarkMode,
        borderColor = if (gasPPM > 350) DangerRed.copy(alpha = 0.60f) else activeColor.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Speed,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strings.gaugeTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Surface(
                shape = CircleShape,
                color = activeColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, activeColor.copy(alpha = 0.50f))
            ) {
                Text(
                    text = statusLabel,
                    color = activeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterHorizontally)
                .testTag("mq2_gas_gauge"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                val inset = strokeWidth / 2f + 10.dp.toPx()
                val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                val topLeft = Offset(inset, inset)
                val startAngle = 150f
                val totalSweep = 240f

                // Outer subtle background track
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = totalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active animated gradient arc
                val activeSweep = (totalSweep * animatedFraction).coerceAtLeast(4f)
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to SafeEmerald,
                        0.40f to AccentTeal,
                        0.65f to WarningAmber,
                        1.0f to DangerRed
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 350 PPM Danger threshold tick on outer rim
                val thresholdFrac = (350f - 50f) / (950f - 50f)
                val thresholdRad = ((startAngle + totalSweep * thresholdFrac) * PI / 180.0)
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = arcSize.width / 2f + strokeWidth * 0.55f
                val innerRadius = arcSize.width / 2f - strokeWidth * 0.55f
                drawLine(
                    color = DangerRed.copy(alpha = 0.85f),
                    start = Offset(
                        center.x + (innerRadius * cos(thresholdRad)).toFloat(),
                        center.y + (innerRadius * sin(thresholdRad)).toFloat()
                    ),
                    end = Offset(
                        center.x + (outerRadius * cos(thresholdRad)).toFloat(),
                        center.y + (outerRadius * sin(thresholdRad)).toFloat()
                    ),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Tip indicator dot
                val tipRad = ((startAngle + activeSweep) * PI / 180.0)
                val arcRadius = arcSize.width / 2f
                val tipPos = Offset(
                    center.x + (arcRadius * cos(tipRad)).toFloat(),
                    center.y + (arcRadius * sin(tipRad)).toFloat()
                )
                drawCircle(
                    color = activeColor.copy(alpha = 0.35f),
                    radius = 12.dp.toPx(),
                    center = tipPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.5.dp.toPx(),
                    center = tipPos
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "MQ-2",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$gasPPM",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    modifier = Modifier.testTag("gauge_ppm_text")
                )
                Text(
                    text = "PPM",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scale Min / Threshold / Max markers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "50 PPM",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = SafeEmerald
            )
            Text(
                text = "Limit: 350",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = DangerRed
            )
            Text(
                text = "950 PPM",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = DangerRed
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = CircleShape,
            color = activeColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, activeColor.copy(alpha = 0.30f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusSub,
                color = activeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
