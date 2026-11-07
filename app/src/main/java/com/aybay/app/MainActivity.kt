package com.aybay.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.biometric.BiometricPrompt
import com.aybay.app.data.scheduleAutoIncome
import com.aybay.app.ui.AddFormScreen
import com.aybay.app.ui.AppPrefs
import com.aybay.app.ui.AyBayViewModel
import com.aybay.app.ui.BudgetScreen
import com.aybay.app.ui.DueScreen
import com.aybay.app.ui.GreenDark
import com.aybay.app.ui.HomeScreen
import com.aybay.app.ui.JomaBlue
import com.aybay.app.ui.PinSetDialog
import com.aybay.app.ui.SecurityRecoveryDialog
import com.aybay.app.ui.LangState
import com.aybay.app.ui.NotesScreen
import com.aybay.app.ui.PageBg
import com.aybay.app.ui.ReportScreen
import com.aybay.app.ui.SettingsScreen
import com.aybay.app.ui.VoiceScreen
import com.aybay.app.ui.VoiceText
import com.aybay.app.ui.isEnglishPref
import com.aybay.app.ui.LockedSectionGate
import com.aybay.app.ui.loadAppPrefs
import com.aybay.app.ui.parseBackupJson
import com.aybay.app.ui.saveAppPrefs
import com.aybay.app.ui.tr

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LangState.isEnglish = isEnglishPref(this)
        scheduleAutoIncome(this)
        setContent {
            val viewModel: AyBayViewModel = viewModel()
            viewModel.ensureDefaultAccount()
            var prefs by remember { mutableStateOf(loadAppPrefs(this)) }
            var unlocked by remember { mutableStateOf(false) }
            // অ্যাপস পিন টগল চালু থাকলেই চালুতে লক চাইবে (পিন সেট থাকলেও
            // টগল বন্ধ থাকলে চাইবে না; পিন না থাকলে সেট করতে বলবে)।
            val needsLock = prefs.appPinEnabled && !unlocked
            if (needsLock) {
                LockScreen(
                    prefs = prefs,
                    onUnlocked = { unlocked = true },
                    onPinSet = { pin ->
                        val next = prefs.copy(pin = pin)
                        prefs = next
                        saveAppPrefs(this, next)
                        unlocked = true
                    },
                    activity = this
                )
            } else {
                AyBayApp(
                    viewModel = viewModel,
                    prefs = prefs,
                    onPrefsChange = { next ->
                        prefs = next
                        LangState.isEnglish = next.language == "en"
                        saveAppPrefs(this, next)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyBayApp(viewModel: AyBayViewModel, prefs: AppPrefs, onPrefsChange: (AppPrefs) -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route ?: "home"
    // রিপোর্ট লক: এই সেশনে একবার খুললে আর পিন চাইবে না; রিপোর্ট থেকে
    // বেরোলেই আবার লক হয়।
    var reportUnlocked by remember { mutableStateOf(false) }
    LaunchedEffect(route) {
        if (route != "report") reportUnlocked = false
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                val items = parseBackupJson(text)
                for (item in items) {
                    viewModel.addTransaction(item)
                }
            } catch (e: Exception) {
            }
        }
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "৳",
                            modifier = Modifier
                                .background(GreenDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = tr("আয় - ব্যয়", "Ay - Bay"),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr("আয়-ব্যয়ের দৈনন্দিন হিসাব", "Daily income-expense tracker"),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("report") }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = tr("খুঁজুন", "Search"))
                    }
                    Button(
                        onClick = { navController.navigate("notes") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFACC15),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = tr("নোট", "Notes"),
                            modifier = Modifier.padding(end = 2.dp)
                        )
                        Text(text = tr("নোট", "Notes"), fontSize = 13.sp)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = tr("মেনু", "Menu"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf(
                    "home" to tr("হোম", "Home"),
                    "due" to tr("বাকি ও ধার", "Due & Loan"),
                    "budget" to tr("বাজেট ও খাতা", "Budget"),
                    "report" to tr("তালিকা ও রিপোর্ট", "List & Report"),
                    "voice" to tr("ভয়েস", "Voice")
                )
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.AccountBalanceWallet,
                    Icons.Default.Assessment,
                    Icons.Default.BarChart,
                    Icons.Default.Mic
                )
                for ((index, item) in items.withIndex()) {
                    val selected = route == item.first
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigate(item.first) },
                        icon = { Icon(imageVector = icons[index], contentDescription = item.second) },
                        label = { Text(text = item.second, fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onAdd = { type -> navController.navigate("add/" + type) },
                        onOpenDue = { navController.navigate("due") },
                        onOpenBudget = { navController.navigate("budget") },
                        onOpenReport = { navController.navigate("report") },
                        onOpenSettings = { navController.navigate("settings") }
                    )
                }
                composable(
                    "add/{type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType })
                ) { entry ->
                    val type = entry.arguments?.getString("type") ?: "আয়"
                    val initialNote = VoiceText.text
                    VoiceText.text = ""
                    AddFormScreen(
                        viewModel = viewModel,
                        initialType = type,
                        initialNote = initialNote
                    ) {
                        navController.popBackStack()
                    }
                }
                composable("due") { DueScreen(viewModel = viewModel) }
                composable("budget") { BudgetScreen(viewModel = viewModel) }
                composable("report") {
                    if ("রিপোর্ট" in prefs.lockedSections && !reportUnlocked) {
                        LockedSectionGate(
                            prefs = prefs,
                            onPrefsChange = onPrefsChange,
                            onUnlocked = { reportUnlocked = true }
                        )
                    } else {
                        ReportScreen(viewModel = viewModel)
                    }
                }
                composable("notes") { NotesScreen(viewModel = viewModel) }
                composable("voice") {
                    VoiceScreen(
                        onUseText = { text ->
                            VoiceText.text = text
                            navController.navigate("add/ব্যয়")
                        }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        prefs = prefs,
                        onPrefsChange = onPrefsChange,
                        onImportBackup = { uri ->
                            importLauncher.launch("application/json")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LockScreen(
    prefs: AppPrefs,
    onUnlocked: () -> Unit,
    onPinSet: (String) -> Unit,
    activity: FragmentActivity
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var showRecovery by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Text(text = "৳", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = GreenDark)
        Text(text = tr("আয় - ব্যয়", "Ay - Bay"), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        if (prefs.pin.isBlank()) {
            // অ্যাপস পিন চালু কিন্তু পিন সেট নেই: আগে পিন সেট করতে হবে।
            Text(text = tr("পিন সেট করুন", "Set a PIN"), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { v -> pin = v.filter { it.isDigit() }.take(6) },
                label = { Text(text = tr("৪-৬ সংখ্যার পিন", "4-6 digit PIN")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (pin.length >= 4) onPinSet(pin) },
                enabled = pin.length >= 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text(text = tr("সংরক্ষণ করুন", "Save"), fontSize = 16.sp)
            }
        } else {
            Text(text = tr("পিন দিন", "Enter PIN"), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { v -> pin = v.filter { it.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (error) {
                Text(text = tr("ভুল পিন", "Wrong PIN"), color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (pin == prefs.pin) onUnlocked() else error = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
            ) {
                Text(text = tr("খুলুন", "Unlock"), fontSize = 16.sp)
            }
            if (prefs.biometricEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(activity)
                        val prompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    onUnlocked()
                                }
                            }
                        )
                        val info = BiometricPrompt.PromptInfo.Builder()
                            .setTitle(tr("আয় - ব্যয়", "Ay - Bay"))
                            .setSubtitle(tr("ফিঙ্গারপ্রিন্ট দিন", "Use fingerprint"))
                            .setNegativeButtonText(tr("বাতিল", "Cancel"))
                            .build()
                        prompt.authenticate(info)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                    Text(text = " " + tr("ফিঙ্গারপ্রিন্ট", "Fingerprint"), fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = tr("পিন ভুলে গেছেন?", "Forgot PIN?"),
                color = JomaBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    if (prefs.securityQuestion.isBlank()) {
                        info = tr("সিকিউরিটি প্রশ্ন সেট করা নেই", "No security question set")
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
                Toast.makeText(
                    context,
                    tr("পিন সংরক্ষণ করা হয়েছে", "PIN saved"),
                    Toast.LENGTH_SHORT
                ).show()
                showNewPin = false
                onPinSet(newPin)
            },
            onDismiss = { showNewPin = false }
        )
    }
}
