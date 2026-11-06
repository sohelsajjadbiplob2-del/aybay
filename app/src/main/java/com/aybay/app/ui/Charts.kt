package com.aybay.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val GreenDark = Color(0xFF0F6B5A)
val GreenMain = Color(0xFF14957A)
val GreenLight = Color(0xFFE3F4EE)
val GreenTint = Color(0xFFDCFCE7)
val RedMain = Color(0xFFE14D4D)
val RedTint = Color(0xFFFEE2E2)
val BlueMain = Color(0xFF3B82F6)
val BlueTint = Color(0xFFDBEAFE)
val YellowMain = Color(0xFFEAB308)
val YellowTint = Color(0xFFFEF9C3)
val PurpleMain = Color(0xFF7C3AED)
val PinkMain = Color(0xFFEC4899)
val GreyText = Color(0xFF6B7280)
val GreyBg = Color(0xFFF3F4F6)
val TrackGrey = Color(0xFFE5E7EB)

// খাতের রং: ক্যাটাগরি ডোনাটে ঘুরিয়ে ব্যবহার হয়।
val CategoryColors = listOf(RedMain, GreenMain, BlueMain, YellowMain, PurpleMain, PinkMain)

// পাই চার্ট: আয় (সবুজ) / ব্যয় (লাল) / সঞ্চয় (নীল) অনুপাত।
// ডাটা শূন্য হলেও হালকা ধূসর রিং দেখায়, তাই চার্ট খালি লাগে না।
@Composable
fun DonutChart(
    income: Long,
    expense: Long,
    savings: Long,
    modifier: Modifier = Modifier,
    centerTop: String = "",
    centerBottom: String = ""
) {
    val total = income + expense + savings
    Box(modifier = modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(132.dp)) {
            val stroke = Stroke(width = 36f)
            if (total <= 0L) {
                drawArc(
                    color = TrackGrey,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
            } else {
                var start = -90f
                val parts = listOf(
                    GreenMain to income,
                    RedMain to expense,
                    BlueMain to savings
                )
                for ((color, value) in parts) {
                    if (value <= 0L) continue
                    val sweep = 360f * value.toFloat() / total
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = stroke
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (centerTop.isNotBlank()) {
                Text(
                    text = centerTop,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            if (centerBottom.isNotBlank()) {
                Text(
                    text = centerBottom,
                    fontSize = 11.sp,
                    color = Color(0xFFD1FAE5),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// বহু রঙের ডোনাট: খাত অনুযায়ী বিশ্লেষণে ব্যবহার হয়।
@Composable
fun MultiDonutChart(
    items: List<Pair<Color, Long>>,
    centerTop: String,
    centerBottom: String,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.second }
    Box(modifier = modifier.size(110.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(110.dp)) {
            val stroke = Stroke(width = 30f)
            if (total <= 0L) {
                drawArc(
                    color = TrackGrey,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
            } else {
                var start = -90f
                for ((color, value) in items) {
                    if (value <= 0L) continue
                    val sweep = 360f * value.toFloat() / total
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = stroke
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerTop,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GreyText,
                textAlign = TextAlign.Center
            )
            Text(
                text = centerBottom,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LegendRow(
    color: Color,
    label: String,
    amount: String,
    percent: String? = null,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            fontSize = 14.sp,
            color = textColor
        )
        if (percent != null) {
            Text(
                text = percent,
                modifier = Modifier.padding(end = 8.dp),
                fontSize = 13.sp,
                color = textColor.copy(alpha = 0.75f)
            )
        }
        Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// বার চার্ট: লেবেল আর মানের তালিকা থেকে বার আঁকে।
@Composable
fun BarChart(
    items: List<Pair<String, Long>>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val max = (items.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.first,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Canvas(modifier = Modifier.weight(2f).height(18.dp)) {
                    val frac = item.second.toFloat() / max
                    drawRoundRect(
                        color = barColor,
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(size.width * frac, size.height)
                    )
                }
                Text(
                    text = moneyText(item.second),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TotalCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 13.sp, color = GreyText)
            Text(text = amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// ছোট পরিসংখ্যান বাক্স: আজকের হিসাব, সর্বোচ্চ/সর্বনিম্ন/গড়।
@Composable
fun StatBox(
    title: String,
    amount: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = GreyText, textAlign = TextAlign.Center)
            Text(
                text = amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ডিজাইন করা খালি অবস্থা: নরম বৃত্তে চিহ্ন + ছোট লেবেল।
@Composable
fun EmptyState(icon: String, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(GreyBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 22.sp, color = GreyText)
        }
        Text(
            text = text,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 14.sp,
            color = GreyText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyRow(text: String) {
    EmptyState(icon = "＋", text = text)
}
