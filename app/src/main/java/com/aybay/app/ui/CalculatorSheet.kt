package com.aybay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToLong

// বাংলা অঙ্ককে ইংরেজি অঙ্কে বদলায় (হিসাবের জন্য)।
fun bengaliDigitToAscii(text: String): String {
    val out = StringBuilder()
    for (ch in text) {
        if (ch in '০'..'৯') out.append('0' + (ch - '০')) else out.append(ch)
    }
    return out.toString()
}

// হিসাবের লেখাটা দেখানোর মতো করে: * → ×, / → ÷, - → −, অঙ্ক বাংলা।
fun calcDisplayText(expr: String): String {
    val out = StringBuilder()
    for (ch in expr) {
        when (ch) {
            '*' -> out.append('×')
            '/' -> out.append('÷')
            '-' -> out.append('−')
            in '0'..'9' -> out.append('০' + (ch - '0'))
            else -> out.append(ch)
        }
    }
    return out.toString()
}

// হিসাব করে ফল দেয়; ভুল লেখা বা শূন্য দিয়ে ভাগ হলে null।
// % মানে আগের সংখ্যাকে ১০০ দিয়ে ভাগ। × ÷ আগে, তারপর + −।
fun evalCalc(expr: String): Double? {
    if (expr.isBlank()) return null
    val tokens = mutableListOf<Any>()
    var i = 0
    var expectNumber = true
    while (i < expr.length) {
        val ch = expr[i]
        when {
            ch.isDigit() || ch == '.' -> {
                var j = i
                while (j < expr.length && (expr[j].isDigit() || expr[j] == '.')) j++
                val num = expr.substring(i, j).toDoubleOrNull() ?: return null
                tokens.add(num)
                i = j
                if (i < expr.length && expr[i] == '%') {
                    tokens[tokens.lastIndex] = (tokens.last() as Double) / 100.0
                    i++
                }
                expectNumber = false
            }
            ch in "+-*/" && !expectNumber -> {
                tokens.add(ch)
                i++
                expectNumber = true
            }
            ch == '-' && expectNumber -> {
                tokens.add(0.0)
                tokens.add('-')
                i++
                expectNumber = true
            }
            else -> return null
        }
    }
    if (expectNumber) return null
    val first = mutableListOf<Any>()
    var k = 0
    while (k < tokens.size) {
        val t = tokens[k]
        if (t is Char && (t == '*' || t == '/')) {
            val left = first.removeLast() as Double
            val right = tokens[k + 1] as Double
            if (t == '/' && right == 0.0) return null
            first.add(if (t == '*') left * right else left / right)
            k += 2
        } else {
            first.add(t)
            k++
        }
    }
    var result = first[0] as Double
    var m = 1
    while (m < first.size) {
        val op = first[m] as Char
        val v = first[m + 1] as Double
        result = if (op == '+') result + v else result - v
        m += 2
    }
    return result
}

fun formatCalcResult(value: Double): String {
    if (value == kotlin.math.floor(value) && value < 1000000000000.0) {
        return value.toLong().toString()
    }
    return String.format("%.2f", value).trimEnd('0').trimEnd('.')
}

// আসল ক্যালকুলেটর: বাংলা সংখ্যা, ÷ × − + %, C, ⌫, সবুজ ঠিক আছে।
// ঠিক আছে চাপলে হিসাবের ফল টাকার ঘরে বসে।
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorSheet(
    initialAmount: String,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var expr by remember { mutableStateOf(initialAmount.filter { it in '0'..'9' }) }
    val result = remember(expr) { evalCalc(expr) }

    fun appendOperator(op: Char) {
        if (expr.isEmpty()) {
            if (op == '-') expr = "-"
            return
        }
        val last = expr.last()
        expr = when {
            last in "+-*/" -> expr.dropLast(1) + op
            last == '.' -> expr + "0" + op
            else -> expr + op
        }
    }

    fun press(key: String) {
        when (key) {
            "C" -> expr = ""
            "⌫" -> expr = expr.dropLast(1)
            "÷" -> appendOperator('/')
            "×" -> appendOperator('*')
            "−" -> appendOperator('-')
            "+" -> appendOperator('+')
            "%" -> if (expr.isNotEmpty() && expr.last().isDigit()) expr += "%"
            "." -> {
                val seg = expr.split('+', '-', '*', '/', '%').last()
                if ('.' !in seg) expr += if (seg.isEmpty()) "0." else "."
            }
            else -> {
                val ascii = bengaliDigitToAscii(key)
                if (ascii.isNotEmpty()) expr = (expr + ascii).take(12)
            }
        }
    }

    val calcRows = listOf(
        listOf("৭", "৮", "৯", "÷"),
        listOf("৪", "৫", "৬", "×"),
        listOf("১", "২", "৩", "−"),
        listOf(".", "০", "⌫", "+"),
        listOf("০০", "০০০", "C", "%")
    )
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(GreyBg, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = calcDisplayText(expr).ifBlank { "০" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (result != null) {
                        Text(
                            text = "= " + toBengaliDigits(formatCalcResult(result)),
                            fontSize = 16.sp,
                            color = GreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            for (row in calcRows) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        val isOperator = key in listOf("÷", "×", "−", "+", "%")
                        val isClear = key == "C" || key == "⌫"
                        Text(
                            text = key,
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    when {
                                        isClear -> YellowTint
                                        isOperator -> BlueTint
                                        else -> GreyBg
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { press(key) }
                                .padding(vertical = 13.dp),
                            color = if (isClear) Color(0xFFEF6C00) else Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val value = result
                    if (value != null) onApply(value.roundToLong().toString())
                },
                enabled = result != null && result > 0.0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenMain,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = tr("ঠিক আছে", "OK"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
