package com.aybay.app.ui

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// বায়োমেট্রিক আছে কি না: হার্ডওয়্যার + এনরোল করা আঙুল/মুখ থাকলেই true।
fun biometricAvailable(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
        BiometricManager.BIOMETRIC_SUCCESS
}

// বায়োমেট্রিক প্রম্পট চালু করে; সফল হলে onSuccess ডাকে।
fun authenticateBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(tr("আয় - ব্যয়", "Ay - Bay"))
        .setSubtitle(tr("ফিঙ্গারপ্রিন্ট বা ফেস আনলক দিন", "Use fingerprint or face unlock"))
        .setNegativeButtonText(tr("বাতিল", "Cancel"))
        .build()
    prompt.authenticate(info)
}

// টগল সারি: লেখা বাঁয়ে, Switch ডানে।
@Composable
private fun LockToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(GreyBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// পিন সেট/বদলানোর ডায়ালগ — জমার মতো: শিরোনাম "নতুন ৪–৬ সংখ্যার পিন দিন:",
// শুধু সংখ্যার ইনপুট, বোতাম বাতিল (হালকা) / ঠিক আছে (নীল)।
@Composable
fun PinSetDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = tr("নতুন ৪–৬ সংখ্যার পিন দিন:", "Enter a new 4-6 digit PIN:"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { v ->
                        pin = v.filter { it.isDigit() }.take(6)
                        error = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (error) {
                    Text(
                        text = tr("৪–৬ সংখ্যার পিন দিন", "Enter a 4-6 digit PIN"),
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        onClick = { if (pin.length in 4..6) onSave(pin) else error = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(text = tr("ঠিক আছে", "OK"), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// সিকিউরিটি প্রশ্ন ডায়ালগ — পিন সেভের পর জমার মতো খোলে।
// উত্তর ছোট হাতের করে ট্রিম করে সেভ হয়, মেলানোর সময়ও তাই।
@Composable
fun SecurityQuestionDialog(
    prefs: AppPrefs,
    onPrefsChange: (AppPrefs) -> Unit,
    onDismiss: () -> Unit
) {
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = tr(
                        "পিন ভুলে গেলে পুনরুদ্ধারের জন্য একটি সিকিউরিটি প্রশ্ন ও উত্তর লিখুন:",
                        "Write a security question and answer to recover your PIN if you forget it:"
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text(text = tr("প্রশ্ন লিখুন", "Write a question")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text(text = tr("উত্তর লিখুন", "Write the answer")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        onClick = {
                            onPrefsChange(
                                prefs.copy(
                                    securityQuestion = question.trim(),
                                    securityAnswer = answer.trim().lowercase()
                                )
                            )
                            onDismiss()
                        },
                        enabled = question.isNotBlank() && answer.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(text = tr("সংরক্ষণ", "Save"), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// পিন ভুলে গেলে সিকিউরিটি প্রশ্নের উত্তর দিয়ে নতুন পিন সেট করা যায়।
@Composable
fun SecurityRecoveryDialog(
    prefs: AppPrefs,
    onCorrect: () -> Unit,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = tr("পিন পুনরুদ্ধার", "Recover PIN"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = prefs.securityQuestion, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it; error = false },
                    placeholder = { Text(text = tr("উত্তর লিখুন", "Write the answer")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (error) {
                    Text(
                        text = tr("ভুল উত্তর", "Wrong answer"),
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        onClick = {
                            if (answer.trim().lowercase() == prefs.securityAnswer) {
                                onCorrect()
                            } else {
                                error = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                    ) {
                        Text(text = tr("ঠিক আছে", "OK"), fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

// লক করা অংশ খুলতে পিন/বায়োমেট্রিক ডায়ালগ। পিন সেট না থাকলে
// আগে পিন সেট করতে বলে পিন-সেট ডায়ালগ দেখায়।
@Composable
fun PinUnlockDialog(
    prefs: AppPrefs,
    onPrefsChange: (AppPrefs) -> Unit,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var showRecovery by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showSecurityQ by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }
    if (prefs.pin.isBlank()) {
        PinSetDialog(
            onSave = { newPin ->
                onPrefsChange(prefs.copy(pin = newPin))
                Toast.makeText(
                    context,
                    tr("পিন সংরক্ষণ করা হয়েছে", "PIN saved"),
                    Toast.LENGTH_SHORT
                ).show()
                if (prefs.securityQuestion.isBlank()) showSecurityQ = true
            },
            onDismiss = onDismiss
        )
    } else if (!showRecovery && !showNewPin && !showSecurityQ) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔒", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = tr("পিন দিন", "Enter PIN"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { v -> pin = v.filter { it.isDigit() }.take(6) },
                        label = { Text(text = tr("পিন", "PIN")) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (error) {
                        Text(
                            text = tr("ভুল পিন", "Wrong PIN"),
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { if (pin == prefs.pin) onUnlocked() else error = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                    ) {
                        Text(text = tr("খুলুন", "Unlock"), fontSize = 16.sp)
                    }
                    if (prefs.biometricEnabled && activity != null && biometricAvailable(context)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { authenticateBiometric(activity) { onUnlocked() } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                        ) {
                            Text(text = tr("ফিঙ্গারপ্রিন্ট", "Fingerprint"), fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = tr("পিন ভুলে গেছেন?", "Forgot PIN?"),
                        color = JomaBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            if (prefs.securityQuestion.isBlank()) {
                                info = tr(
                                    "সিকিউরিটি প্রশ্ন সেট করা নেই",
                                    "No security question set"
                                )
                            } else {
                                showRecovery = true
                            }
                        }
                    )
                    if (info.isNotBlank()) {
                        Text(
                            text = info,
                            color = Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    if (showRecovery) {
        SecurityRecoveryDialog(
            prefs = prefs,
            onCorrect = { showRecovery = false; showNewPin = true },
            onDismiss = { showRecovery = false }
        )
    }
    if (showNewPin) {
        PinSetDialog(
            onSave = { newPin ->
                onPrefsChange(prefs.copy(pin = newPin))
                Toast.makeText(
                    context,
                    tr("পিন সংরক্ষণ করা হয়েছে", "PIN saved"),
                    Toast.LENGTH_SHORT
                ).show()
                showNewPin = false
                onUnlocked()
            },
            onDismiss = { showNewPin = false }
        )
    }
    if (showSecurityQ) {
        SecurityQuestionDialog(
            prefs = prefs,
            onPrefsChange = onPrefsChange,
            onDismiss = { showSecurityQ = false }
        )
    }
}

// "পিন ও গোপনীয়তা" ডায়ালগ: বায়োমেট্রিক টগল, পিন সেট/বদলান/মুছুন,
// অ্যাপস পিন টগল, আর অংশ লকের টগল (আয়/ব্যয়/সঞ্চয়/রিপোর্ট/সার্চ বার)।
@Composable
fun PinPrivacyDialog(
    prefs: AppPrefs,
    onPrefsChange: (AppPrefs) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPinSet by remember { mutableStateOf(false) }
    var showSecurityQ by remember { mutableStateOf(false) }
    var pendingSection by remember { mutableStateOf<String?>(null) }
    var pendingAppPin by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun openPinSet(section: String?, appPin: Boolean) {
        pendingSection = section
        pendingAppPin = appPin
        showPinSet = true
    }
    fun applyPin(newPin: String) {
        val section = pendingSection
        var next = prefs.copy(pin = newPin)
        if (pendingAppPin) next = next.copy(appPinEnabled = true)
        if (section != null) next = next.copy(lockedSections = next.lockedSections + section)
        onPrefsChange(next)
        pendingSection = null
        pendingAppPin = false
        showPinSet = false
        Toast.makeText(
            context,
            tr("পিন সংরক্ষণ করা হয়েছে", "PIN saved"),
            Toast.LENGTH_SHORT
        ).show()
        // সিকিউরিটি প্রশ্ন আগে সেট না থাকলে এখন চাইবে; পিন বদলালে
        // আগের প্রশ্ন থাকলে আর চাইবে না।
        if (prefs.securityQuestion.isBlank()) showSecurityQ = true
        message = tr("পিন সেট হয়েছে", "PIN set")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = tr("🔐 পিন ও গোপনীয়তা", "PIN & Privacy"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                LockToggleRow(
                    label = tr(
                        "ফিঙ্গারপ্রিন্ট / ফেস আনলক (Biometric Lock)",
                        "Fingerprint / Face Unlock (Biometric Lock)"
                    ),
                    checked = prefs.biometricEnabled,
                    onCheckedChange = { on ->
                        if (on && !biometricAvailable(context)) {
                            message = tr(
                                "এই ফোনে ফিঙ্গারপ্রিন্ট/ফেস আনলক নেই",
                                "No fingerprint/face unlock on this phone"
                            )
                        } else {
                            onPrefsChange(prefs.copy(biometricEnabled = on))
                        }
                    }
                )
                Text(
                    text = tr(
                        "চালু করলে অ্যাপ স্টার্টআপে এবং লক করা বিভাগে ফিঙ্গারপ্রিন্ট বা ফেস আনলক দিয়ে আনলক করা যাবে।",
                        "When on, you can unlock at app startup and in locked sections with fingerprint or face unlock."
                    ),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showPinSet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreyBg,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (prefs.pin.isBlank()) {
                            tr("🔒 পিন সেট করুন", "Set PIN")
                        } else {
                            tr("🔒 পিন বদলান", "Change PIN")
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (prefs.pin.isNotBlank()) {
                    Text(
                        text = tr("পিন মুছে ফেলুন", "Remove PIN"),
                        modifier = Modifier.padding(top = 8.dp).clickable {
                            onPrefsChange(
                                prefs.copy(
                                    pin = "",
                                    appPinEnabled = false,
                                    lockedSections = emptySet(),
                                    securityQuestion = "",
                                    securityAnswer = ""
                                )
                            )
                        },
                        fontSize = 14.sp,
                        color = RedMain
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LockToggleRow(
                    label = tr("🔐 অ্যাপস পিন (পুরো অ্যাপ লক)", "App PIN (Lock whole app)"),
                    checked = prefs.appPinEnabled,
                    onCheckedChange = { on ->
                        if (on && prefs.pin.isBlank()) {
                            openPinSet(null, true)
                        } else {
                            onPrefsChange(prefs.copy(appPinEnabled = on))
                        }
                    }
                )
                Text(
                    text = tr(
                        "চালু করলে অ্যাপ খুললেই পিন বা বায়োমেট্রিক চাইবে এবং সব হিসাব সুরক্ষিত থাকবে।",
                        "When on, opening the app will ask for a PIN or biometric, and all records will stay protected."
                    ),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = tr(
                        "নিচের যেকোনো অংশ লক করে রাখতে পারেন — লক করা অংশ দেখতে পিন বা বায়োমেট্রিক লাগবে।",
                        "You can lock any section below — a locked section needs a PIN or biometric to view."
                    ),
                    fontSize = 13.sp,
                    color = GreyText
                )
                Spacer(modifier = Modifier.height(12.dp))
                val sections = listOf(
                    "আয়" to tr("💰 আয়", "Income"),
                    "ব্যয়" to tr("💸 ব্যয়", "Expense"),
                    "সঞ্চয়" to tr("🏦 সঞ্চয়", "Savings"),
                    "রিপোর্ট" to tr("📈 রিপোর্ট", "Report"),
                    "সার্চ" to tr("🔍 সার্চ বার", "Search bar")
                )
                for ((key, label) in sections) {
                    LockToggleRow(
                        label = label,
                        checked = key in prefs.lockedSections,
                        onCheckedChange = { on ->
                            if (on && prefs.pin.isBlank()) {
                                openPinSet(key, false)
                            } else {
                                val next = if (on) {
                                    prefs.lockedSections + key
                                } else {
                                    prefs.lockedSections - key
                                }
                                onPrefsChange(prefs.copy(lockedSections = next))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = GreenDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JomaBlue)
                ) {
                    Text(
                        text = tr("বন্ধ করুন", "Close"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    if (showPinSet) {
        PinSetDialog(
            onSave = { applyPin(it) },
            onDismiss = {
                showPinSet = false
                pendingSection = null
                pendingAppPin = false
            }
        )
    }
    if (showSecurityQ) {
        SecurityQuestionDialog(
            prefs = prefs,
            onPrefsChange = onPrefsChange,
            onDismiss = { showSecurityQ = false }
        )
    }
}

// লক করা অংশ (রিপোর্ট) খুলতে গেট স্ক্রিন: পিন/বায়োমেট্রিক না দিলে
// ভেতরের হিসাব দেখায় না।
@Composable
fun LockedSectionGate(
    prefs: AppPrefs,
    onPrefsChange: (AppPrefs) -> Unit,
    onUnlocked: () -> Unit
) {
    var showPin by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(text = "🔒", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tr("এই অংশ লক করা আছে", "This section is locked"),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tr("দেখতে পিন বা বায়োমেট্রিক লাগবে।", "A PIN or biometric is needed to view it."),
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showPin = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
        ) {
            Text(text = tr("পিন দিয়ে খুলুন", "Unlock with PIN"), fontSize = 16.sp)
        }
    }
    if (showPin) {
        PinUnlockDialog(
            prefs = prefs,
            onPrefsChange = onPrefsChange,
            onUnlocked = { showPin = false; onUnlocked() },
            onDismiss = { showPin = false }
        )
    }
}
