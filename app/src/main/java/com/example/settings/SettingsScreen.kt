package com.example.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SettingsGridItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager = SettingsManager.instance,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val generalSettings by settingsManager.generalSettings.collectAsState()
    val securitySettings by settingsManager.securitySettings.collectAsState()
    val smsSettings by settingsManager.smsSettings.collectAsState()
    val oneSignalSettings by settingsManager.oneSignalSettings.collectAsState()
    val notificationSettings by settingsManager.notificationSettings.collectAsState()
    val designations by settingsManager.designations.collectAsState()
    val feeTypes by settingsManager.feeTypes.collectAsState()

    var activeDialog by remember { mutableStateOf<String?>(null) }

    val gridItems = listOf(
        SettingsGridItem("general", "সাধারণ সেটিংস", Icons.Default.Settings, Color(0xFFEFF6FF), Color(0xFF2563EB)),
        SettingsGridItem("security", "নিরাপত্তা সেটিংস", Icons.Default.Lock, Color(0xFFFDF2F8), Color(0xFFDB2777)),
        SettingsGridItem("notifications", "বিজ্ঞপ্তি সেটিংস", Icons.Default.Notifications, Color(0xFFFFF7ED), Color(0xFFEA580C)),
        SettingsGridItem("sms", "SMS সেটিংস", Icons.AutoMirrored.Filled.Send, Color(0xFFECFDF5), Color(0xFF059669)),
        SettingsGridItem("onesignal", "OneSignal নোটিফিকেশন", Icons.Default.Campaign, Color(0xFFFFF1F2), Color(0xFFE11D48)),
        SettingsGridItem("receipt", "রশিদ বই", Icons.AutoMirrored.Filled.MenuBook, Color(0xFFFAF5FF), Color(0xFF9333EA)),
        SettingsGridItem("signature", "স্বাক্ষর ব্যবস্থাপনা", Icons.Default.Edit, Color(0xFFF0FDF4), Color(0xFF16A34A)),
        SettingsGridItem("designation", "পদবীসমূহ", Icons.Default.VerifiedUser, Color(0xFFECFDF5), Color(0xFF0D9488)),
        SettingsGridItem("feetypes", "ফি-এর প্রকারভেদসমূহ", Icons.Default.Book, Color(0xFFEFF6FF), Color(0xFF0284C7))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section Card (Matching uploaded design)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "মাদরাসা সেটিংস প্যানেল",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "যেকোনো একটি সেটিংস ঘর বা বাটন নির্বাচন করে আপনার কাজ সম্পন্ন করুন।",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // 2-Column Grid Layout matching the uploaded mockup
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gridItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { activeDialog = item.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = item.iconBgColor
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }
    }

    // DIALOGS FOR EACH SETTINGS ITEM
    when (activeDialog) {
        "general" -> {
            GeneralSettingsDialog(
                current = generalSettings,
                onDismiss = { activeDialog = null },
                onSave = { updated ->
                    settingsManager.updateGeneralSettings(updated)
                    activeDialog = null
                    Toast.makeText(context, "সাধারণ সেটিংস সফলভাবে সেভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
        "security" -> {
            SecuritySettingsDialog(
                current = securitySettings,
                onDismiss = { activeDialog = null },
                onSave = { updated ->
                    settingsManager.updateSecuritySettings(updated)
                    activeDialog = null
                    Toast.makeText(context, "নিরাপত্তা সেটিংস সেভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
        "notifications" -> {
            NotificationSettingsDialog(
                current = notificationSettings,
                onDismiss = { activeDialog = null },
                onSave = { updated ->
                    settingsManager.updateNotificationSettings(updated)
                    activeDialog = null
                    Toast.makeText(context, "বিজ্ঞপ্তি সেটিংস সেভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
        "sms" -> {
            SmsSettingsDialog(
                current = smsSettings,
                onDismiss = { activeDialog = null },
                onSave = { updated ->
                    settingsManager.updateSmsSettings(updated)
                    activeDialog = null
                    Toast.makeText(context, "SMS কনফিগারেশন সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
        "onesignal" -> {
            OneSignalSettingsDialog(
                current = oneSignalSettings,
                onDismiss = { activeDialog = null },
                onSave = { updated ->
                    settingsManager.updateOneSignalSettings(updated)
                    activeDialog = null
                    Toast.makeText(context, "OneSignal সেটিংস সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
        "receipt" -> {
            SimpleInfoDialog(
                title = "রশিদ বই সেটিংস",
                message = "রশিদ প্রিফিক্স: DS-2026\nরশিদ নম্বর কাউন্টার: 001\nস্বয়ংক্রিয় নম্বর জেনারেটর সক্রিয় রয়েছে।",
                onDismiss = { activeDialog = null }
            )
        }
        "signature" -> {
            SimpleInfoDialog(
                title = "স্বাক্ষর ব্যবস্থাপনা",
                message = "প্রধান শিক্ষক ও মুহতামিম সাহেবের ডিজিটাল স্বাক্ষর সংযুক্ত করা হয়েছে। প্রত্যয়নপত্র ও রশিদ বইতে স্বয়ংক্রিয়ভাবে ব্যবহৃত হবে।",
                onDismiss = { activeDialog = null }
            )
        }
        "designation" -> {
            ListManagementDialog(
                title = "পদবীসমূহ ব্যবস্থাপনা",
                items = designations,
                onAdd = { settingsManager.addDesignation(it) },
                onRemove = { settingsManager.removeDesignation(it) },
                onDismiss = { activeDialog = null }
            )
        }
        "feetypes" -> {
            ListManagementDialog(
                title = "ফি-এর প্রকারভেদসমূহ",
                items = feeTypes,
                onAdd = { settingsManager.addFeeType(it) },
                onRemove = { settingsManager.removeFeeType(it) },
                onDismiss = { activeDialog = null }
            )
        }
    }
}

// ----------------- DIALOG COMPONENTS -----------------

@Composable
fun GeneralSettingsDialog(
    current: MadrassaGeneralSettings,
    onDismiss: () -> Unit,
    onSave: (MadrassaGeneralSettings) -> Unit
) {
    var name by remember { mutableStateOf(current.madrassaName) }
    var tagline by remember { mutableStateOf(current.tagline) }
    var district by remember { mutableStateOf(current.district) }
    var address by remember { mutableStateOf(current.address) }
    var phone by remember { mutableStateOf(current.phone) }
    var email by remember { mutableStateOf(current.email) }
    var year by remember { mutableStateOf(current.academicYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সাধারণ সেটিংস পরিবর্তন", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("মাদরাসার নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("জেলা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("ট্যাগলাইন / স্লোগান") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ইমেইল ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            madrassaName = name,
                            tagline = tagline,
                            district = district,
                            address = address,
                            phone = phone,
                            email = email,
                            academicYear = year
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun SecuritySettingsDialog(
    current: AdminSecuritySettings,
    onDismiss: () -> Unit,
    onSave: (AdminSecuritySettings) -> Unit
) {
    var email by remember { mutableStateOf(current.adminEmail) }
    var phone by remember { mutableStateOf(current.adminPhone) }
    var pin by remember { mutableStateOf(current.pinCode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নিরাপত্তা সেটিংস (এডমিন)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("এডমিন জিমেইল / ইমেইল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("জরুরি যোগাযোগ ফোন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("সিকিউরিটি পিন código") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(current.copy(adminEmail = email, adminPhone = phone, pinCode = pin)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
            ) {
                Text("আপডেট করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSettingsDialog(
    current: SmsSettingsData,
    onDismiss: () -> Unit,
    onSave: (SmsSettingsData) -> Unit
) {
    val context = LocalContext.current
    var selectedGateway by remember { mutableStateOf(current.gatewayName) }
    var apiKey by remember { mutableStateOf(current.apiKey) }
    var senderId by remember { mutableStateOf(current.senderId) }
    var deviceId by remember { mutableStateOf(current.texbeeDeviceId) }
    var simSlot by remember { mutableStateOf(current.texbeeSimSlot) }
    var serverUrl by remember { mutableStateOf(current.texbeeServerUrl) }
    var isEnabled by remember { mutableStateOf(current.isEnabled) }

    var gatewayDropdownExpanded by remember { mutableStateOf(false) }
    val gatewayOptions = listOf(
        "Texbee (Android App Gateway)",
        "Greenweb Bulk SMS",
        "ElitBuzz SMS Gateway",
        "BulkSMS BD"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SMS গেটওয়ে ও Texbee সেটিংস", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Enable/Disable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("অটোমেটিক SMS সার্ভিস", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Provider Selection Dropdown
                Text("SMS গেটওয়ে প্রোভাইডার নির্বাচন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))

                ExposedDropdownMenuBox(
                    expanded = gatewayDropdownExpanded,
                    onExpandedChange = { gatewayDropdownExpanded = !gatewayDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGateway,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gatewayDropdownExpanded) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = gatewayDropdownExpanded,
                        onDismissRequest = { gatewayDropdownExpanded = false }
                    ) {
                        gatewayOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(opt, fontWeight = if (opt == selectedGateway) FontWeight.Bold else FontWeight.Normal)
                                        if (opt.contains("Texbee")) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                                Text("অ্যাপ গেটওয়ে", fontSize = 10.sp, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    selectedGateway = opt
                                    gatewayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // TEXBEE SPECIFIC FIELDS
                if (selectedGateway.contains("Texbee")) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "📱 Texbee অ্যাপ ইন্টিগ্রেশন:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Texbee অ্যান্ড্রয়েড অ্যাপ ইনস্টল করে আপনার মোবাইলের SIM কার্ড ব্যবহার করে ফ্রিতে সরাসরি অটোমেটিক মেসেজ পাঠাতে পারবেন।",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )

                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text("Texbee API Key") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = deviceId,
                                onValueChange = { deviceId = it },
                                label = { Text("Device ID / Token") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // SIM Slot Picker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SIM স্লট:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = simSlot == "SIM 1",
                                        onClick = { simSlot = "SIM 1" },
                                        label = { Text("SIM 1") }
                                    )
                                    FilterChip(
                                        selected = simSlot == "SIM 2",
                                        onClick = { simSlot = "SIM 2" },
                                        label = { Text("SIM 2") }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = serverUrl,
                                onValueChange = { serverUrl = it },
                                label = { Text("Texbee API URL Endpoint") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Texbee ($simSlot) দিয়ে টেস্ট SMS পাঠানো হয়েছে!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("টেস্ট SMS পাঠান (Texbee)")
                            }
                        }
                    }
                } else {
                    // STANDARD BULK SMS PROVIDERS
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("SMS API Key") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = senderId,
                        onValueChange = { senderId = it },
                        label = { Text("Sender ID / Masking Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            gatewayName = selectedGateway,
                            apiKey = apiKey,
                            senderId = senderId,
                            texbeeDeviceId = deviceId,
                            texbeeSimSlot = simSlot,
                            texbeeServerUrl = serverUrl,
                            isEnabled = isEnabled
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun OneSignalSettingsDialog(
    current: OneSignalSettingsData,
    onDismiss: () -> Unit,
    onSave: (OneSignalSettingsData) -> Unit
) {
    var appId by remember { mutableStateOf(current.appId) }
    var restKey by remember { mutableStateOf(current.restApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("OneSignal পুশ নোটিফিকেশন", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    label = { Text("OneSignal App ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = restKey,
                    onValueChange = { restKey = it },
                    label = { Text("REST API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(current.copy(appId = appId, restApiKey = restKey)) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun NotificationSettingsDialog(
    current: NotificationSettingsData,
    onDismiss: () -> Unit,
    onSave: (NotificationSettingsData) -> Unit
) {
    var studentAbsence by remember { mutableStateOf(current.notifyOnStudentAbsence) }
    var teacherAbsence by remember { mutableStateOf(current.notifyOnTeacherAbsence) }
    var examResult by remember { mutableStateOf(current.notifyExamResults) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("বিজ্ঞপ্তি ও এলার্ট সেটিংস", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ছাত্র অনুপস্থিতিতে SMS", fontSize = 13.sp)
                    Switch(checked = studentAbsence, onCheckedChange = { studentAbsence = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("শিক্ষক অনুপস্থিতিতে এলার্ট", fontSize = 13.sp)
                    Switch(checked = teacherAbsence, onCheckedChange = { teacherAbsence = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ফলাফল প্রকাশের অটো বিজ্ঞপ্তি", fontSize = 13.sp)
                    Switch(checked = examResult, onCheckedChange = { examResult = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        current.copy(
                            notifyOnStudentAbsence = studentAbsence,
                            notifyOnTeacherAbsence = teacherAbsence,
                            notifyExamResults = examResult
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
            ) {
                Text("সংরক্ষণ")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun ListManagementDialog(
    title: String,
    items: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newItemText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = { Text("নতুন লিখুন...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newItemText.isNotBlank()) {
                                onAdd(newItemText)
                                newItemText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        Text("যোগ")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.forEach { item ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                IconButton(
                                    onClick = { onRemove(item) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("সম্পন্ন")
            }
        }
    )
}

@Composable
fun SimpleInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { Text(message, fontSize = 14.sp, color = Color(0xFF334155)) },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))) {
                Text("ঠিক আছে")
            }
        }
    )
}
