package com.aybay.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

// ফরমের ব্যানারের গাঢ় নেভি রং (জমার সতর্ক/সফলতার ব্যানারের মতো)।
val NavyBanner = Color(0xFF0F2B46)

// সংরক্ষণের পর দেখানো সফলতার ব্যানারের তথ্য (সংরক্ষণের আগের মান সহ)।
data class SavedInfo(
    val type: String,
    val displayCat: String,
    val amount: Long,
    val totalBefore: Long,
    val totalAfter: Long,
    val accountLabel: String,
    val accountBefore: Long,
    val accountAfter: Long,
    val isWithdraw: Boolean
)

// সতর্ক-ব্যানার: খাত না বাছা অবস্থায় পরিমাণের ঘরে চাপ দিলে দেখায়।
@Composable
fun WarningBanner(type: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(NavyBanner, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(34.dp)
                    .border(2.dp, OrangeMain, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = OrangeMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = when (type) {
                    "আয়" -> tr(
                        "পরিমাণ লেখার আগে অনুগ্রহ করে উপরে একটি আয়ের খাত নির্বাচন করুন",
                        "Please select an income category above before entering the amount"
                    )
                    "ব্যয়" -> tr(
                        "পরিমাণ লেখার আগে অনুগ্রহ করে উপরে একটি ব্যয়ের খাত নির্বাচন করুন",
                        "Please select an expense category above before entering the amount"
                    )
                    else -> tr(
                        "পরিমাণ লেখার আগে অনুগ্রহ করে উপরে একটি সঞ্চয়ের খাত নির্বাচন করুন",
                        "Please select a savings category above before entering the amount"
                    )
                },
                modifier = Modifier.weight(1f).padding(start = 10.dp),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// সফলতার ব্যানার: সংরক্ষণের পর মোট ও হাতে নগদের হিসাব সহ দেখায়।
@Composable
fun SavedBanner(info: SavedInfo) {
    val pillColor = if (info.type == "ব্যয়" || info.isWithdraw) RedMain else GreenMain
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(NavyBanner, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp)
                        .background(GreenMain, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        text = when {
                            info.type == "আয়" -> tr("আয় যোগ হয়েছে", "Income added")
                            info.type == "ব্যয়" -> tr("ব্যয় যোগ হয়েছে", "Expense added")
                            info.isWithdraw -> tr("সঞ্চয় উত্তোলন হয়েছে", "Savings withdrawn")
                            else -> tr("সঞ্চয় যোগ হয়েছে", "Savings added")
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = info.displayCat + " • " + moneyText(info.amount),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, pillColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            info.type == "আয়" -> "+ " + moneyText(info.amount) + " " + tr("(যোগ হলো)", "(added)")
                            info.type == "ব্যয়" -> "+ " + moneyText(info.amount) + " " + tr("(ব্যয়ে যোগ হলো)", "(added to expense)")
                            info.isWithdraw -> "− " + moneyText(info.amount) + " " + tr("(উত্তোলন হলো)", "(withdrawn)")
                            else -> "+ " + moneyText(info.amount) + " " + tr("(যোগ হলো)", "(added)")
                        },
                        color = pillColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = when (info.type) {
                                "আয়" -> tr("মোট আয়:", "Total Income:")
                                "ব্যয়" -> tr("মোট ব্যয়:", "Total Expense:")
                                else -> tr("মোট সঞ্চয়:", "Total Savings:")
                            },
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = moneyText(info.totalAfter),
                            color = pillColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = moneyText(info.totalBefore), color = Color.White, fontSize = 13.sp)
                        Text(text = if (info.isWithdraw) " − " else " + ", color = Color.White, fontSize = 13.sp)
                        Text(
                            text = moneyText(info.amount),
                            color = if (info.isWithdraw) RedMain else GreenMain,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = " = ", color = Color.White, fontSize = 13.sp)
                        Text(text = moneyText(info.totalAfter), color = Color.White, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = info.accountLabel + " " + when {
                        info.type == "আয়" -> tr("(যোগ হলো):", "(added):")
                        info.type == "ব্যয়" -> tr("(বিয়োগ হলো):", "(deducted):")
                        info.isWithdraw -> tr("(উত্তোলন হলো):", "(withdrawn):")
                        else -> tr("(যোগ হলো):", "(added):")
                    },
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Text(text = moneyText(info.accountBefore), color = pillColor, fontSize = 13.sp)
                    Text(
                        text = if (info.type == "ব্যয়" || info.isWithdraw) " − " else " + ",
                        color = pillColor,
                        fontSize = 13.sp
                    )
                    Text(text = moneyText(info.amount), color = pillColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = " = ", color = pillColor, fontSize = 13.sp)
                    Text(text = moneyText(info.accountAfter), color = pillColor, fontSize = 13.sp)
                }
            }
        }
    }
}

// অ্যাপ-লেভেল ব্যানার ওভারলে: স্ক্রিনের উপরে ভেসে দেখায় (হেডার ঢেকে দেয়,
// কনটেন্ট ঠেলে না)। সক্রিয় ব্যানার একটাই থাকে — সফলতা থাকলে সেটা, নইলে সতর্ক।
@Composable
fun AppBannerOverlay(viewModel: AyBayViewModel) {
    val saved = viewModel.savedBanner
    val warningVisible = viewModel.warningVisible
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = saved != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            if (saved != null) SavedBanner(saved)
        }
        AnimatedVisibility(
            visible = saved == null && warningVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            WarningBanner(viewModel.warningType)
        }
    }
}
