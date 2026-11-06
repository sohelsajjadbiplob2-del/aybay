package com.aybay.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val GreenDark = Color(0xFF0F6B5A)
val GreenMain = Color(0xFF14957A)
val GreenLight = Color(0xFFE3F4EE)
val RedMain = Color(0xFFE14D4D)
val BlueMain = Color(0xFF3B82F6)
val GreyText = Color(0xFF6B7280)
val GreyBg = Color(0xFFF3F4F6)

// পাই চার্ট: আয় (সবুজ) / ব্যয় (লাল) / সঞ্চয় (নীল) অনুপাত।
@Composable
fun DonutChart(income: Long, expense: Long, savings: Long, modifier: Modifier = Modifier) {
    val total = (income + expense + savings).coerceAtLeast(1L)
    val incomeFrac = income.toFloat() / total
    val expenseFrac = expense.toFloat() / total
    val savingsFrac = savings.toFloat() / total
    Canvas(modifier = modifier.size(140.dp)) {
        val stroke = Stroke(width = 34f)
        var start = -90f
        drawArc(
            color = GreenMain,
            startAngle = start,
            sweepAngle = 360f * incomeFrac,
            useCenter = false,
            style = stroke
        )
        start += 360f * incomeFrac
        drawArc(
            color = RedMain,
            startAngle = start,
            sweepAngle = 360f * expenseFrac,
            useCenter = false,
            style = stroke
        )
        start += 360f * expenseFrac
        drawArc(
            color = BlueMain,
            startAngle = start,
            sweepAngle = 360f * savingsFrac,
            useCenter = false,
            style = stroke
        )
    }
}

@Composable
fun LegendRow(color: Color, label: String, amount: String) {
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
            fontSize = 14.sp
        )
        Text(text = amount, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width * frac, size.height)
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
fun TotalCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 13.sp, color = GreyText)
            Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun EmptyRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text, fontSize = 14.sp, color = GreyText)
    }
}
