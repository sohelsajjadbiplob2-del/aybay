package com.aybay.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aybay.app.data.Account

val BANK_LIST = listOf(
    "সোনালী ব্যাংক", "ইসলামী ব্যাংক", "ডাচ-বাংলা ব্যাংক", "ব্র্যাক ব্যাংক",
    "সিটি ব্যাংক", "ইস্টার্ন ব্যাংক (EBL)", "প্রাইম ব্যাংক", "পূবালী ব্যাংক",
    "অগ্রণী ব্যাংক", "জনতা ব্যাংক", "রূপালী ব্যাংক", "মিউচুয়াল ট্রাস্ট ব্যাংক (MTB)",
    "ইউনাইটেড কমার্শিয়াল ব্যাংক (UCB)", "ব্যাংক এশিয়া", "ট্রাস্ট ব্যাংক",
    "আল-আরাফাহ ইসলামী ব্যাংক", "সোশ্যাল ইসলামী ব্যাংক", "স্ট্যান্ডার্ড চার্টার্ড ব্যাংক"
)

val MOBILE_LIST = listOf(
    "বিকাশ (bKash)", "নগদ (Nagad)", "রকেট (Rocket)", "উপায় (Upay)",
    "ট্যাপ (Tap)", "এমক্যাশ (mCash)", "শিওরক্যাশ (SureCash)", "ওকে ওয়ালেট (OK Wallet)"
)

private val BANK_TYPES = listOf("সেভিংস অ্যাকাউন্ট", "কারেন্ট অ্যাকাউন্ট", "স্যালারি অ্যাকাউন্ট")
private val MOBILE_TYPES = listOf("পার্সোনাল", "এজেন্ট", "মার্চেন্ট")

private fun loadStringSet(context: Context, key: String): Set<String> =
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE)
        .getStringSet(key, emptySet()) ?: emptySet()

private fun saveStringSet(context: Context, key: String, value: Set<String>) {
    context.getSharedPreferences("aybay_prefs", Context.MODE_PRIVATE).edit()
        .putStringSet(key, value).apply()
}

@Composable
fun AccountPickDialog(
    onDismiss: () -> Unit,
    onPickBank: () -> Unit,
    onPickMobile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "🗂️ " + tr("অ্যাকাউন্ট নির্বাচন করুন", "Choose an Account"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr("ব্যাংক অ্যাকাউন্ট অথবা মোবাইল ব্যাংকিং বেছে নিন", "Choose a bank account or mobile banking"),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Spacer(modifier = Modifier.height(14.dp))
                AccountPickRow(
                    icon = "🏦",
                    iconBg = BlueTint,
                    title = tr("ব্যাংক অ্যাকাউন্ট", "Bank Account"),
                    subtitle = tr("সোনালী, ইসলামী, ডাচ-বাংলা, ব্র্যাক ইত্যাদি", "Sonali, Islami, Dutch-Bangla, BRAC etc."),
                    onClick = onPickBank
                )
                Spacer(modifier = Modifier.height(10.dp))
                AccountPickRow(
                    icon = "📱",
                    iconBg = PurpleTint,
                    title = tr("মোবাইল ব্যাংকিং", "Mobile Banking"),
                    subtitle = tr("বিকাশ, নগদ, রকেট, উপায় ইত্যাদি", "bKash, Nagad, Rocket, Upay etc."),
                    onClick = onPickMobile
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
                    Text(
                        text = tr("বন্ধ করুন", "Close"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountPickRow(
    icon: String,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, TrackGrey, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 12.sp, color = GreyText)
        }
        Text(text = "›", fontSize = 22.sp, color = GreyText)
    }
}

@Composable
fun BankAccountDialog(
    viewModel: AyBayViewModel,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onGoMobile: () -> Unit,
    onAccountChosen: (String) -> Unit
) {
    ProviderSelectDialog(
        viewModel = viewModel,
        kind = "ব্যাংক",
        titleIcon = "🏦",
        title = tr("ব্যাংক অ্যাকাউন্ট", "Bank Account"),
        switchPillIcon = "📱",
        switchPillLabel = tr("মোবাইল", "Mobile"),
        subtitle = tr("তালিকা থেকে ব্যাংক নির্বাচন করুন (ট্যাপ করলেই যুক্ত হবে)", "Pick a bank from the list (tap to add it)"),
        builtInItems = BANK_LIST,
        itemIcon = "🏦",
        customKey = "custom_banks",
        hiddenKey = "hidden_banks",
        addLabel = tr("নতুন ব্যাংক বা নির্দিষ্ট অ্যাকাউন্ট যোগ করুন", "Add a new bank or a specific account"),
        inputHint = tr("যেমন: ইসলামী ব্যাংক - সেভিংস", "e.g. Islami Bank - Savings"),
        typeOptions = BANK_TYPES,
        onDismiss = onDismiss,
        onBack = onBack,
        onSwitch = onGoMobile,
        onAccountChosen = onAccountChosen
    )
}

@Composable
fun MobileAccountDialog(
    viewModel: AyBayViewModel,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onGoBank: () -> Unit,
    onAccountChosen: (String) -> Unit
) {
    ProviderSelectDialog(
        viewModel = viewModel,
        kind = "মোবাইল",
        titleIcon = "📱",
        title = tr("মোবাইল ব্যাংকিং", "Mobile Banking"),
        switchPillIcon = "🏦",
        switchPillLabel = tr("ব্যাংক", "Bank"),
        subtitle = tr("মোবাইল ব্যাংকিং প্রোভাইডার নির্বাচন করুন", "Choose a mobile banking provider"),
        builtInItems = MOBILE_LIST,
        itemIcon = "📱",
        customKey = "custom_mobile",
        hiddenKey = "hidden_mobile",
        addLabel = tr("নতুন মোবাইল ব্যাংকিং বা ওয়ালেট যোগ করুন", "Add a new mobile banking or wallet"),
        inputHint = tr("যেমন: বিকাশ পার্সোনাল, নগদ", "e.g. bKash Personal, Nagad"),
        typeOptions = MOBILE_TYPES,
        onDismiss = onDismiss,
        onBack = onBack,
        onSwitch = onGoBank,
        onAccountChosen = onAccountChosen
    )
}

@Composable
private fun ProviderSelectDialog(
    viewModel: AyBayViewModel,
    kind: String,
    titleIcon: String,
    title: String,
    switchPillIcon: String,
    switchPillLabel: String,
    subtitle: String,
    builtInItems: List<String>,
    itemIcon: String,
    customKey: String,
    hiddenKey: String,
    addLabel: String,
    inputHint: String,
    typeOptions: List<String>,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onSwitch: () -> Unit,
    onAccountChosen: (String) -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    var selected by remember { mutableStateOf<String?>(null) }
    var customText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var customItems by remember { mutableStateOf(loadStringSet(context, customKey)) }
    var hiddenItems by remember { mutableStateOf(loadStringSet(context, hiddenKey)) }

    val shownItems = builtInItems.filter { it !in hiddenItems } + customItems.filter { it !in builtInItems }
    val canConfirm = selected != null || customText.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titleIcon + " " + title,
                        modifier = Modifier.weight(1f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(GreyBg, RoundedCornerShape(16.dp))
                            .clickable { onSwitch() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = switchPillIcon + " " + switchPillLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = subtitle, fontSize = 13.sp, color = GreyText)
                Spacer(modifier = Modifier.height(12.dp))
                for (row in shownItems.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (item in row) {
                            ProviderChip(
                                icon = itemIcon,
                                name = item,
                                selected = selected == item,
                                onSelect = { selected = item },
                                onDelete = {
                                    if (item in customItems) {
                                        val next = customItems - item
                                        customItems = next
                                        saveStringSet(context, customKey, next)
                                    } else {
                                        val next = hiddenItems + item
                                        hiddenItems = next
                                        saveStringSet(context, hiddenKey, next)
                                    }
                                    if (selected == item) selected = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+ " + addLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = { Text(text = inputHint, color = GreyText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tr("ধরন:", "Type:"),
                        fontSize = 13.sp,
                        color = GreyText,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    for (t in typeOptions) {
                        val typeSelected = selectedType == t
                        Box(
                            modifier = Modifier
                                .background(
                                    if (typeSelected) GreenMain else GreyBg,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedType = t }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ " + tr(
                                    t,
                                    if (t == "সেভিংস অ্যাকাউন্ট") "Savings Account"
                                    else if (t == "কারেন্ট অ্যাকাউন্ট") "Current Account"
                                    else if (t == "স্যালারি অ্যাকাউন্ট") "Salary Account"
                                    else if (t == "পার্সোনাল") "Personal"
                                    else if (t == "এজেন্ট") "Agent"
                                    else "Merchant"
                                ),
                                fontSize = 12.sp,
                                fontWeight = if (typeSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (typeSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyBg,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "‹ " + tr("পেছনে", "Back"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreyBg,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = tr("বাতিল", "Cancel"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            val typed = customText.trim()
                            val typeLabel = selectedType ?: typeOptions.first()
                            val name = if (typed.isNotBlank()) {
                                if (typed !in builtInItems && typed !in customItems) {
                                    val next = customItems + typed
                                    customItems = next
                                    saveStringSet(context, customKey, next)
                                }
                                typed
                            } else {
                                (selected ?: return@Button) + " - " + typeLabel
                            }
                            if (accounts.none { it.name == name }) {
                                viewModel.addAccount(Account(name = name, kind = kind))
                            }
                            onAccountChosen(name)
                            onDismiss()
                        },
                        enabled = canConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(
                            text = tr("নির্বাচন করুন", "Select"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderChip(
    icon: String,
    name: String,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                if (selected) BlueTint else Color.White,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (selected) JomaBlue else TrackGrey,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon + " " + name,
            modifier = Modifier.weight(1f).clickable { onSelect() },
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) JomaBlue else Color.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "🗑️",
            modifier = Modifier.clickable { onDelete() },
            fontSize = 14.sp
        )
    }
}
