package com.aybay.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

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

// জমা খরচের হোমের রং: পেজের হালকা নীলচে পটভূমি, নীল সিলেক্টেড ট্যাব,
// হালকা নীল ব্যালেন্স কার্ড, কমলা সঞ্চয়, বেগুনি ট্রান্সফার।
val JomaBlue = Color(0xFF2563EB)
val PageBg = Color(0xFFF0F4FA)
val LightBlueCard = Color(0xFFDBEAFE)
val OrangeMain = Color(0xFFEA580C)
val OrangeTint = Color(0xFFFED7AA)
// সঞ্চয়ের বাদামি (জমা খরচের সঞ্চয় পাতা/বোতাম): গাঢ় বাদামি।
val BrownMain = Color(0xFFA16207)
val PurpleTint = Color(0xFFEDE9FE)
val PinkTint = Color(0xFFFCE7F3)
val PinkDisc = Color(0xFFF9A8A8)
val LightChartBg = Color(0xFFEFF6FF)

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

// ---------- জমা খরচের হোমের (V3) উপাদান ----------

// সেকশনের শিরোনাম: রঙিন ডট + নাম + ডানে ঐচ্ছিক বোতাম/লিংক।
@Composable
fun JomaSectionHeader(
    dotColor: Color,
    title: String,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape))
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        trailing()
    }
}

// ব্যালেন্স কার্ডের ডোনাট: হালকা গোলাপি চাকতি, উপরে ব্যয়ের অংশের লাল চাপ,
// ভেতরে "ব্যয়" আর শতাংশ।
@Composable
fun ExpenseShareDonut(expense: Long, total: Long) {
    val percent = if (total > 0L) (expense * 100 / total).toInt() else 0
    Box(modifier = Modifier.size(118.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(118.dp)) {
            drawCircle(color = PinkDisc)
            if (total > 0L && expense > 0L) {
                val stroke = Stroke(width = 26f)
                drawArc(
                    color = RedMain,
                    startAngle = -90f,
                    sweepAngle = 360f * expense.toFloat() / total,
                    useCenter = false,
                    style = stroke
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = tr("ব্যয়", "Expense"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = toBengaliDigits(percent.toString()) + "%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// জমা খরচের মতো হোমের ব্যালেন্স পাই: সঞ্চয় (হালকা হলুদ) / নগদ (হালকা সবুজ) / ব্যয় (হালকা লাল)।
// সব ০ থাকলেও তিন সমান ১২০° ভাগ দেখায়; নাম + শতাংশ ভাগের ওপরেই লেখা থাকে, মাঝখানে কিছু নেই।
@Composable
fun JomaBalanceDonut(
    savings: Long,
    cash: Long,
    expense: Long
) {
    val s = maxOf(0L, savings)
    val c = maxOf(0L, cash)
    val e = maxOf(0L, expense)
    val total = s + c + e
    // জমার মতো উপর থেকে ঘড়ির কাঁটার দিকে: নগদ, ব্যয়, সঞ্চয়।
    val parts = listOf(
        Triple(GreenTint, c, tr("নগদ", "Cash")),
        Triple(RedTint, e, tr("ব্যয়", "Expense")),
        Triple(YellowTint, s, tr("সঞ্চয়", "Savings"))
    )
    val sweeps = if (total <= 0L) listOf(120f, 120f, 120f)
    else parts.map { 360f * it.second.toFloat() / total.toFloat() }
    val percents = if (total <= 0L) listOf(0, 0, 0)
    else parts.map { (it.second * 100L / total).toInt() }
    Box(modifier = Modifier.size(118.dp)) {
        Canvas(modifier = Modifier.size(118.dp)) {
            var start = -90f
            for (i in parts.indices) {
                val sweep = sweeps[i]
                if (sweep > 0f) {
                    // ভাগগুলোর মাঝে চিকন ফাঁক, যাতে সীমানা বোঝা যায়।
                    val gap = minOf(2f, sweep / 2f)
                    drawArc(
                        color = parts[i].first,
                        startAngle = start + gap / 2f,
                        sweepAngle = sweep - gap,
                        useCenter = true
                    )
                }
                start += sweep
            }
        }
        var start = -90f
        for (i in parts.indices) {
            val sweep = sweeps[i]
            val mid = start + sweep / 2f
            start += sweep
            if (sweep < 30f) continue
            val rad = Math.toRadians(mid.toDouble())
            val lx = 59.dp + 32.dp * cos(rad).toFloat() - 24.dp
            val ly = 59.dp + 32.dp * sin(rad).toFloat() - 15.dp
            val pct = percents[i]
            val pctText = if (LangState.isEnglish) "$pct%" else toBengaliDigits(pct.toString()) + "%"
            Box(
                modifier = Modifier.align(Alignment.TopStart).offset(lx, ly).size(48.dp, 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = parts[i].third,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = pctText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// এক রঙের রিং ডোনাট: খাত অনুযায়ী বিশ্লেষণ কার্ডে ব্যবহার হয়।
// খালি থাকলে ধূসর রিং দেখায়।
@Composable
fun RingDonut(
    color: Color,
    top: String,
    mid: String,
    bottom: String
) {
    Box(modifier = Modifier.size(118.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(118.dp)) {
            val stroke = Stroke(width = 26f)
            drawArc(
                color = TrackGrey,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = top,
                fontSize = 12.sp,
                color = GreyText,
                textAlign = TextAlign.Center
            )
            Text(
                text = mid,
                fontSize = 12.sp,
                color = GreyText,
                textAlign = TextAlign.Center
            )
            Text(
                text = bottom,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

// আয়-ব্যয়ের দুইটা বড় বার: উপরে অঙ্ক, নিচে নাম।
@Composable
fun IncomeExpenseBars(income: Long, expense: Long) {
    val max = maxOf(income, expense).coerceAtLeast(1L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightChartBg, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IncomeExpenseBar(
            moneyText(income), income, max, GreenMain,
            Icons.Default.ArrowUpward, tr("আয়", "Income")
        )
        IncomeExpenseBar(
            moneyText(expense), expense, max, RedMain,
            Icons.Default.ArrowDownward, tr("ব্যয়", "Expense")
        )
    }
}

@Composable
private fun IncomeExpenseBar(
    amount: String,
    value: Long,
    max: Long,
    color: Color,
    icon: ImageVector,
    label: String
) {
    val frac = if (max > 0L) value.toFloat() / max else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = amount,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier.width(72.dp).height(150.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height((150f * frac).dp)
                    .background(
                        color,
                        RoundedCornerShape(
                            topStart = 10.dp, topEnd = 10.dp,
                            bottomStart = 4.dp, bottomEnd = 4.dp
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 2.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// আয় (সবুজ) / ব্যয় (লাল) অনুপাতে ভাগ করা প্রোগ্রেস বার।
@Composable
fun SplitProgressBar(income: Long, expense: Long) {
    val total = income + expense
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(TrackGrey, RoundedCornerShape(5.dp))
    ) {
        if (total > 0L) {
            if (income > 0L) {
                Box(
                    modifier = Modifier
                        .weight(income.toFloat())
                        .fillMaxHeight()
                        .background(
                            GreenMain,
                            RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
                        )
                )
            }
            if (expense > 0L) {
                Box(
                    modifier = Modifier
                        .weight(expense.toFloat())
                        .fillMaxHeight()
                        .background(
                            RedMain,
                            RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp)
                        )
                )
            }
        }
    }
}

// মাসের প্রতিদিনের ব্যয়ের লাইন চার্ট: দিনে ট্যাপ বা স্লাইড করলে ওই দিন সিলেক্ট হয়।
@Composable
fun FlowLineChart(
    days: Int,
    values: List<Long>,
    selectedDay: Int,
    avg: Long,
    onSelectDay: (Int) -> Unit
) {
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val hasData = values.any { it > 0L }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .pointerInput(days, values) {
                detectTapGestures { offset ->
                    val day = ((offset.x / size.width.toFloat()) * days)
                        .toInt().coerceIn(0, days - 1) + 1
                    onSelectDay(day)
                }
            }
            .pointerInput(days, values) {
                detectHorizontalDragGestures { change, _ ->
                    val day = ((change.position.x / size.width.toFloat()) * days)
                        .toInt().coerceIn(0, days - 1) + 1
                    onSelectDay(day)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            val w = size.width
            val h = size.height
            val padTop = 24f
            val padBottom = 8f
            val chartH = h - padTop - padBottom
            val baseY = padTop + chartH
            fun x(day: Int): Float =
                if (days <= 1) w / 2f else (day - 1).toFloat() / (days - 1) * w
            fun y(v: Long): Float = padTop + chartH * (1f - v.toFloat() / max)
            // বেসলাইন (অক্ষের রেখা): হালকা নিরেট ধূসর।
            drawLine(
                color = Color(0xFFE5E7EB),
                start = Offset(0f, baseY),
                end = Offset(w, baseY),
                strokeWidth = 2f
            )
            // গড়ের ড্যাশ-লাইন (গড় ০ হলে আঁকে না — বেসলাইনের উপর পড়ত)।
            if (avg > 0L) {
                val avgY = y(avg.coerceAtMost(max))
                drawLine(
                    color = Color(0xFF9CA3AF),
                    start = Offset(0f, avgY),
                    end = Offset(w, avgY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            }
            if (hasData) {
                val linePath = Path().apply {
                    moveTo(x(1), y(values[0]))
                    for (d in 2..days) lineTo(x(d), y(values[d - 1]))
                }
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(x(days), baseY)
                    lineTo(x(1), baseY)
                    close()
                }
                drawPath(fillPath, color = GreenMain.copy(alpha = 0.12f))
                drawPath(linePath, color = GreenMain, style = Stroke(width = 5f))
                val maxDay = values.indexOf(max) + 1
                drawCircle(
                    color = GreenMain,
                    radius = 12f,
                    center = Offset(x(maxDay), y(max))
                )
                val sel = selectedDay.coerceIn(1, days)
                // বাছাই করা দিনে খাড়া গাইড-লাইন (ডটেড) + X-অক্ষে ছোট টিক।
                drawLine(
                    color = Color(0xFF9CA3AF),
                    start = Offset(x(sel), y(values[sel - 1])),
                    end = Offset(x(sel), padTop),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
                drawLine(
                    color = GreenMain,
                    start = Offset(x(sel), baseY - 8f),
                    end = Offset(x(sel), baseY + 8f),
                    strokeWidth = 5f
                )
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = Offset(x(sel), y(values[sel - 1]))
                )
                drawCircle(
                    color = GreenMain,
                    radius = 11f,
                    center = Offset(x(sel), y(values[sel - 1]))
                )
            }
        }
    }
}
