package com.aybay.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ফর্মের খাতের একটা টাইল: নাম, আইকন, নিজের যোগ করা কি না।
data class CatItem(val name: String, val icon: String, val isCustom: Boolean)

// আইকন বাছাইয়ের ইমোজি গ্রিড (জমা খরচের পাতাগুলো থেকে নেওয়া)।
val EMOJI_GRID = listOf(
    "✨", "🛒", "🍔", "🚗", "🏠", "💊",
    "📚", "🎁", "🎮", "✈️", "👕", "📱",
    "⚡", "💡", "🐾", "🧾", "🏋️", "🎬",
    "☕", "🧴", "🔨", "💇", "🐶", "👶",
    "🚌", "🎓", "💳", "📦", "🌿", "🕌",
    "🍚", "🍛", "🍜", "🍕", "🍗", "🥩",
    "🐟", "🥚", "🥛", "🍞", "🍎", "🍌",
    "🥕", "🍅", "🥗", "🍰", "🍫", "🍦",
    "🧃", "🥤", "🍵", "🛍️", "🧺", "🏪",
    "🏢", "💄", "👗", "👟", "👜", "👓",
    "⌚", "💍", "🧢", "🧥", "👔", "🚕",
    "🚖", "🚙", "🏍️", "🛵", "🚲", "🛺",
    "🚆", "🚂", "🚢", "⛽", "🅿️", "🛣️",
    "🔧", "🔩", "🧰", "🪛", "🪚", "🧱",
    "🪑", "🛏️", "🛋️", "🚿", "🛁", "🚽",
    "🔌", "🔋", "🕯️", "🧹", "🧽", "🗑️",
    "🔑", "🔒", "💻", "🖥️", "⌨️", "🖱️",
    "🖨️", "📺", "📻", "📷", "🎧", "🎤",
    "🎵", "🎸", "🎹", "📞", "☎️", "📡",
    "📊", "🌐", "💾", "💿", "🏥", "🩺",
    "💉", "🩹", "🦷", "🧪", "🩸", "🏃",
    "⚽", "🏏", "🏀", "🎾", "🏊", "🚴",
    "🧘", "🥋", "🎯", "🎲", "♟️", "🎰",
    "🎳", "🎨", "🎭", "🎪", "🎟️", "🎂",
    "🎈", "🎉", "🎄", "🌙", "⭐", "🕋",
    "📿", "🤲", "📖", "📕", "📝", "✏️",
    "🖊️", "📐", "🧮", "🔬", "🔭", "🏫",
    "🏦", "🏛️", "⚖️", "📑", "📄", "📋",
    "📁", "🗂️", "💼", "🧳", "📅", "⏰",
    "🔔", "📢", "✉️", "📮", "💰", "💵",
    "💴", "💶", "💷", "💸", "🪙", "🏧",
    "📈", "📉", "🏷️", "🎫", "🪪", "🤝",
    "🙏", "❤️", "💖", "👥", "👩", "👨",
    "🧑", "👴", "👵", "🍼", "🧸", "🪁",
    "🐄", "🐐", "🐓", "🦆", "🐑", "🐈",
    "🐦", "🌾", "🌱", "🌳", "🌴", "🌸",
    "🌻", "🌧️", "☀️", "🔥", "💧", "🌊",
    "⛰️", "🏖️", "🏕️", "🗺️", "🧭", "🛡️",
    "🚨", "🧯", "⚙️", "♻️", "🚚", "🚛",
    "🏗️", "🏭"
)

// নাম লেখার ডায়ালগ: নতুন খাত, নতুন উপখাত, খাতের নাম বদলানো — সবেতেই চলে।
@Composable
fun CategoryNameDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyBg,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = tr("বাতিল", "Cancel"), fontSize = 15.sp)
                    }
                    Button(
                        onClick = { onConfirm(text.trim()) },
                        enabled = text.trim().isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(
                            text = tr("ঠিক আছে", "OK"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// আইকন বাছাইয়ের ডায়ালগ: স্ক্রল করা যায় এমন ৬ কলামের ইমোজি গ্রিড +
// নিজের ইমোজি লেখার ফিল্ড (ডিফল্ট ✨)।
@Composable
fun IconPickerDialog(
    initialIcon: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var picked by remember { mutableStateOf(initialIcon) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = tr(
                        "একটি আইকন বেছে নিন বা নিজের ইমোজি লিখুন:",
                        "Pick an icon or write your own emoji:"
                    ),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .height(280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (row in EMOJI_GRID.chunked(6)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (emoji in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            if (picked == emoji) BlueTint else GreyBg,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .then(
                                            if (picked == emoji) Modifier.border(
                                                2.dp, JomaBlue, RoundedCornerShape(10.dp)
                                            ) else Modifier
                                        )
                                        .clickable { picked = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                            if (row.size < 6) {
                                repeat(6 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = picked,
                    onValueChange = { picked = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyBg,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = tr("বাতিল", "Cancel"), fontSize = 15.sp)
                    }
                    Button(
                        onClick = { onConfirm(picked.trim().ifBlank { "✨" }) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(
                            text = tr("ঠিক আছে", "OK"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryMenuRow(
    label: String,
    isDanger: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreyBg, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDanger) RedMain else Color.Black
        )
    }
}

// কাস্টম খাতের ⋮ মেনু: নাম বদলান / আইকন বদলান / মুছুন।
@Composable
fun CategoryMenuDialog(
    catName: String,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = catName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                CategoryMenuRow(
                    label = "✏️ " + tr("নাম বদলান", "Rename"),
                    isDanger = false,
                    onClick = onRename
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryMenuRow(
                    label = "🎨 " + tr("আইকন বদলান", "Change icon"),
                    isDanger = false,
                    onClick = onChangeIcon
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryMenuRow(
                    label = "🗑️ " + tr("মুছুন", "Delete"),
                    isDanger = true,
                    onClick = onDelete
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreyBg,
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = tr("বাতিল", "Cancel"), fontSize = 15.sp)
                }
            }
        }
    }
}
