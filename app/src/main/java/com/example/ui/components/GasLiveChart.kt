package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GasDataPoint
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun GasLiveChart(
    points: List<GasDataPoint>,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val latestPpm = points.lastOrNull()?.ppm ?: 120
    val lineColor by animateColorAsState(
        targetValue = if (latestPpm > 350) DangerRed else AccentTeal,
        animationSpec = tween(300),
        label = "chartLineColor"
    )
    val gridColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0xFF0F172A).copy(alpha = 0.10f)
    val thresholdColor = DangerRed.copy(alpha = 0.45f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .height(160.dp)
                    .padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("1000", "750", "500", "250", "0").forEach { tick ->
                    Text(
                        text = tick,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = labelColor
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val maxVal = 1000f

                // Horizontal grid lines (0, 250, 500, 750, 1000)
                for (step in 0..4) {
                    val y = h - (step / 4f) * h
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 350 PPM Danger threshold dashed line
                val thresholdY = h - (350f / maxVal) * h
                drawLine(
                    color = thresholdColor,
                    start = Offset(0f, thresholdY),
                    end = Offset(w, thresholdY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )

                if (points.size >= 2) {
                    val stepX = w / (points.size - 1).toFloat()
                    val coords = points.mapIndexed { index, pt ->
                        val x = index * stepX
                        val clamped = pt.ppm.coerceIn(0, 1000).toFloat()
                        val y = h - (clamped / maxVal) * h
                        Offset(x, y)
                    }

                    val strokePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 1 until coords.size) {
                            val prev = coords[i - 1]
                            val curr = coords[i]
                            val midX = (prev.x + curr.x) / 2f
                            cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(coords.last().x, h)
                        lineTo(coords.first().x, h)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.28f),
                                lineColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    coords.forEachIndexed { idx, pt ->
                        val isLatest = idx == coords.lastIndex
                        drawCircle(
                            color = lineColor,
                            radius = if (isLatest) 4.5.dp.toPx() else 2.5.dp.toPx(),
                            center = pt
                        )
                        if (isLatest) {
                            drawCircle(
                                color = lineColor.copy(alpha = 0.25f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }
        }

        // X-axis sparse timestamps
        if (points.size >= 2) {
            val first = points.first().timestamp
            val mid = points[points.size / 2].timestamp
            val last = points.last().timestamp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(first, mid, last).forEach { ts ->
                    Text(
                        text = ts,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                }
            }
        }
    }
}
