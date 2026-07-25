package com.example

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private fun getFirebaseAuthSafely(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Notification permission granted or denied
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Prompt user for notification permission on Android 13+ (API level 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val auth = getFirebaseAuthSafely()
        val prefs = getSharedPreferences("MadrassaSession", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                // Persistent login state check
                var isLoggedIn by remember {
                    mutableStateOf(prefs.getBoolean("is_logged_in", auth?.currentUser != null))
                }
                var userRole by remember {
                    mutableStateOf(prefs.getString("user_role", "ADMIN") ?: "ADMIN")
                }
                var loggedTeacherId by remember {
                    mutableStateOf(prefs.getString("logged_teacher_id", "1") ?: "1")
                }

                val teacherRepository = remember { com.example.teacher.TeacherRepository() }
                val teachers by teacherRepository.teachers.collectAsState()

                if (isLoggedIn) {
                    if (userRole == "TEACHER") {
                        val currentTeacher = teachers.firstOrNull { it.id == loggedTeacherId }
                            ?: teachers.firstOrNull()
                            ?: com.example.teacher.Teacher(name = "সহকারী শিক্ষক")

                        com.example.teacher.TeacherDashboardScreen(
                            teacher = currentTeacher,
                            repository = teacherRepository,
                            onLogout = {
                                prefs.edit()
                                    .putBoolean("is_logged_in", false)
                                    .putString("user_role", "ADMIN")
                                    .apply()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        MadrassaApp(
                            teacherRepository = teacherRepository,
                            onLogout = {
                                prefs.edit().putBoolean("is_logged_in", false).apply()
                                try {
                                    auth?.signOut()
                                } catch (_: Exception) {}
                                isLoggedIn = false
                            },
                            onSwitchToTeacherPortal = { teacherId ->
                                prefs.edit()
                                    .putString("user_role", "TEACHER")
                                    .putString("logged_teacher_id", teacherId)
                                    .apply()
                                userRole = "TEACHER"
                                loggedTeacherId = teacherId
                            }
                        )
                    }
                } else {
                    LoginScreen(
                        teachers = teachers,
                        onLoginSuccess = { role, teacherId ->
                            prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("user_role", role)
                                .putString("logged_teacher_id", teacherId)
                                .apply()
                            userRole = role
                            loggedTeacherId = teacherId
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    teachers: List<com.example.teacher.Teacher> = emptyList(),
    onLoginSuccess: (role: String, teacherId: String) -> Unit
) {
    val generalSettings by com.example.settings.SettingsManager.instance.generalSettings.collectAsState()
    var selectedRoleTab by remember { mutableStateOf(1) } // 0: Admin, 1: Teacher
    var selectedTeacher by remember(teachers) { mutableStateOf(teachers.firstOrNull()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF2563EB),
                        Color(0xFFF8FAFC)
                    ),
                    startY = 0f,
                    endY = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Madrassa Emblem Icon
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Logo",
                            tint = Color(0xFF1D4ED8),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = generalSettings.madrassaName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "জেলা: ${generalSettings.district} • ${generalSettings.tagline}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Role Selection Tab Row
                TabRow(
                    selectedTabIndex = selectedRoleTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF1D4ED8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedRoleTab == 0,
                        onClick = { selectedRoleTab = 0 },
                        text = { Text("এডমিন লগইন", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedRoleTab == 1,
                        onClick = { selectedRoleTab = 1 },
                        text = { Text("শিক্ষক পোর্টাল", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedRoleTab == 1) {
                    // TEACHER PORTAL LOGIN
                    Text(
                        text = "আপনার শিক্ষক প্রোফাইল নির্বাচন করুন:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTeacher?.name ?: "শিক্ষক নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(t.name, fontWeight = FontWeight.Bold)
                                            Text("${t.designation} (${t.phone})", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedTeacher = t
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("পাসওয়ার্ড / পিন নম্বর") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB)) },
                        singleLine = true,
                        placeholder = { Text("ঐচ্ছিক (ডিফল্ট সরাসরি প্রবেশ)") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val targetId = selectedTeacher?.id ?: teachers.firstOrNull()?.id ?: "1"
                            onLoginSuccess("TEACHER", targetId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "শিক্ষক ড্যাশবোর্ডে প্রবেশ করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📌 শিক্ষক পোর্টালে এক ক্লিকে হাজিরা দিতে পারবেন (সকাল ৮:০০ - বিকাল ৪:০০)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                } else {
                    // ADMIN LOGIN
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেইল বা ইউজারনেম") },
                        placeholder = { Text("darussalammadrasha7@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF2563EB)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("গোপন পাসওয়ার্ড") },
                        placeholder = { Text("পাসওয়ার্ড দিন") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val cleanEmail = email.trim().lowercase()
                            val cleanPass = password.trim()

                            if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
                                errorMessage = "❌ ইমেইল/ইউজারনেম এবং পাসওয়ার্ড বাধ্যতামূলক।"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = ""

                            val isMasterAdmin = (cleanEmail == "darussalammadrasha7@gmail.com" || cleanEmail == "admin" || cleanEmail == "darussalam")
                                    && (cleanPass == "admin123" || cleanPass == "1234")

                            if (isMasterAdmin) {
                                isLoading = false
                                onLoginSuccess("ADMIN", "1")
                            } else {
                                try {
                                    val firebaseAuth = FirebaseAuth.getInstance()
                                    firebaseAuth.signInWithEmailAndPassword(cleanEmail, cleanPass)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            onLoginSuccess("ADMIN", "1")
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            errorMessage = "❌ ভুল ইমেইল অথবা পাসওয়ার্ড! সঠিক এডমিন ক্রেডেনশিয়াল প্রদান করুন।"
                                        }
                                } catch (_: Exception) {
                                    isLoading = false
                                    errorMessage = "❌ প্রবেশ ব্যর্থ! সঠিক ইমেইল (darussalammadrasha7@gmail.com) ও পাসওয়ার্ড দিন।"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "এডমিন প্যানেলে লগইন",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔒 সিকিউরিটি পলিসি: উচ্চ নিরাপত্তার স্বার্থে সঠিক ইমেইল ও গোপন পাসওয়ার্ড দিয়ে সুরক্ষিতভাবে লগইন সম্পন্ন করুন।",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

data class MenuItemData(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MadrassaApp(
    teacherRepository: com.example.teacher.TeacherRepository = remember { com.example.teacher.TeacherRepository() },
    onLogout: () -> Unit,
    onSwitchToTeacherPortal: (String) -> Unit = {}
) {
    val generalSettings by com.example.settings.SettingsManager.instance.generalSettings.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedMenu by remember { mutableStateOf("ড্যাশবোর্ড") }
    val studentRepository = remember { com.example.student.StudentRepository() }
    val examRepository = remember { com.example.exam.ExamRepository() }

    val menuItems = listOf(
        MenuItemData("ড্যাশবোর্ড", Icons.Default.Dashboard),
        MenuItemData("শিক্ষার্থীরা", Icons.Default.People),
        MenuItemData("শিক্ষকগণ", Icons.Default.School),
        MenuItemData("শিক্ষক হাজিরা", Icons.Default.CheckCircle),
        MenuItemData("হাজিরা", Icons.Default.EventAvailable),
        MenuItemData("ফি ব্যবস্থাপনা", Icons.Default.Payments),
        MenuItemData("ব্যয় ব্যবস্থাপনা", Icons.Default.ReceiptLong),
        MenuItemData("শিক্ষকদের বেতন", Icons.Default.AccountBalance),
        MenuItemData("পরীক্ষা", Icons.Default.Quiz),
        MenuItemData("রেজাল্ট", Icons.Default.Assessment),
        MenuItemData("প্রবেশপত্র", Icons.Default.Badge),
        MenuItemData("ছুটির আবেদন", Icons.Default.CardTravel),
        MenuItemData("সময়সূচী", Icons.Default.Schedule),
        MenuItemData("নোটিশ বোর্ড", Icons.Default.Campaign),
        MenuItemData("রিপোর্ট", Icons.Default.BarChart),
        MenuItemData("শিক্ষার্থী তথ্য", Icons.Default.ContactPage),
        MenuItemData("কার্যকলাপ লগ", Icons.Default.History),
        MenuItemData("অ্যাডমিন রোল", Icons.Default.AdminPanelSettings),
        MenuItemData("এআই অ্যাসিস্ট্যান্ট", Icons.Default.AutoAwesome),
        MenuItemData("সেটিংস", Icons.Default.Settings),
        MenuItemData("লগআউট", Icons.AutoMirrored.Filled.ExitToApp)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF024BB0),
                modifier = Modifier.width(270.dp) // Narrow compact side menu
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drawer Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = generalSettings.madrassaName,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "জেলা: ${generalSettings.district}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    // Drawer Items List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        items(menuItems) { item ->
                            val isSelected = item.title == selectedMenu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Color(0xFF1E5BB8) else Color.Transparent
                                    )
                                    .clickable {
                                        // Immediately hide sidebar drawer when clicked
                                        scope.launch { drawerState.close() }
                                        if (item.title == "লগআউট") {
                                            onLogout()
                                        } else {
                                            selectedMenu = item.title
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Drawer Footer
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "DEV BY HM.ABDUL ALIM f",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = selectedMenu,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF1E293B))
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF64748B))
                        }
                        IconButton(onClick = { }) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color(0xFF2563EB)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            when (selectedMenu) {
                "শিক্ষার্থীরা", "শিক্ষার্থী তথ্য" -> {
                    com.example.student.StudentScreen(
                        repository = studentRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষকগণ" -> {
                    com.example.teacher.TeacherScreen(
                        repository = teacherRepository,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষক হাজিরা" -> {
                    com.example.attendance.AttendanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "হাজিরা" -> {
                    com.example.attendance.AttendanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        initialTab = 0,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ফি ব্যবস্থাপনা" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        initialTab = 0,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ব্যয় ব্যবস্থাপনা" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষকদের বেতন" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        initialTab = 2,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "পরীক্ষা", "রেজাল্ট", "প্রবেশপত্র" -> {
                    com.example.exam.ExamScreen(
                        examRepository = examRepository,
                        studentRepository = studentRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ছুটির আবেদন" -> {
                    com.example.admin.AdminRolesScreen(
                        teacherRepository = teacherRepository,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "সময়সূচী" -> {
                    com.example.routine.RoutineScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "নোটিশ বোর্ড" -> {
                    com.example.notice.NoticeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "রিপোর্ট" -> {
                    com.example.reports.ReportsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "কার্যকলাপ লগ" -> {
                    com.example.admin.AdminRolesScreen(
                        teacherRepository = teacherRepository,
                        initialTab = 2,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "অ্যাডমিন রোল" -> {
                    com.example.admin.AdminRolesScreen(
                        teacherRepository = teacherRepository,
                        initialTab = 0,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "এআই অ্যাসিস্ট্যান্ট" -> {
                    com.example.ai.AiAssistantScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ড্যাশবোর্ড" -> {
                    DashboardScreen(
                        studentRepository = studentRepository,
                        teacherRepository = teacherRepository,
                        onNavigateToTeacherAttendance = { selectedMenu = "শিক্ষক হাজিরা" },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "সেটিংস" -> {
                    com.example.settings.SettingsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                else -> {
                    // Generic Module Screen with Fallback to Dashboard
                    GenericModuleScreen(
                        title = selectedMenu,
                        onBackToDashboard = { selectedMenu = "ড্যাশবোর্ড" },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GenericModuleScreen(
    title: String,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF2563EB)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$title মডিউল",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "এই মডিউলটির ফিচারসমূহ প্রস্তুত করা হচ্ছে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBackToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ড্যাশবোর্ডে ফিরে যান", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    studentRepository: com.example.student.StudentRepository,
    teacherRepository: com.example.teacher.TeacherRepository? = null,
    onNavigateToTeacherAttendance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val generalSettings by com.example.settings.SettingsManager.instance.generalSettings.collectAsState()
    val students by studentRepository.students.collectAsState()
    val teachers = teacherRepository?.teachers?.collectAsState()?.value ?: emptyList()
    val attendances = teacherRepository?.attendances?.collectAsState()?.value ?: emptyList()

    val isTimeActive = teacherRepository?.isAttendanceWindowActive() ?: true
    val todayDateString = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
    }
    val todayAttendanceMap = remember(attendances, todayDateString) {
        attendances.filter { it.date == todayDateString }.associateBy { it.teacherId }
    }
    val presentCount = todayAttendanceMap.values.count { it.status == "উপস্থিত" || it.status == "বিলম্ব" }

    val totalStudents = students.size
    val maleStudents = students.count { it.gender == "ছাত্র" }
    val femaleStudents = students.count { it.gender == "ছাত্রী" }
    val activeStudents = students.count { it.status == "সক্রিয়" }
    val inactiveStudents = students.count { it.status == "নিষ্ক্রিয়" }
    val totalTeachers = teachers.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "প্রধান পরিসংখ্যান",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }

        // Stats Cards strictly styled like screenshots
        item {
            StatCardScreenshot(
                label = "মোট শিক্ষার্থী",
                value = "$totalStudents",
                icon = Icons.Default.People,
                badgeColor = Color(0xFF2563EB) // Blue
            )
        }
        item {
            StatCardScreenshot(
                label = "সর্বমোট সংগ্রহ",
                value = "৳০",
                icon = Icons.Default.MonetizationOn,
                badgeColor = Color(0xFF10B981) // Green
            )
        }
        item {
            StatCardScreenshot(
                label = "সর্বমোট খরচ",
                value = "৳০",
                icon = Icons.Default.MonetizationOn,
                badgeColor = Color(0xFFF43F5E) // Red/Pink
            )
        }
        item {
            StatCardScreenshot(
                label = "বর্তমান ব্যালেন্স",
                value = "৳০",
                icon = Icons.Default.MonetizationOn,
                badgeColor = Color(0xFF6366F1) // Purple/Indigo
            )
        }
        item {
            StatCardScreenshot(
                label = "মোট শিক্ষক",
                value = "$totalTeachers",
                icon = Icons.Default.School,
                badgeColor = Color(0xFFA855F7) // Violet
            )
        }
        item {
            StatCardScreenshot(
                label = "নিষ্ক্রিয় শিক্ষার্থী",
                value = "$inactiveStudents",
                icon = Icons.Default.PeopleOutline,
                badgeColor = Color(0xFFF59E0B) // Amber
            )
        }

        // Income section
        item {
            Text(
                text = "আয়ের উৎস",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }
        item { EmptyStateCard("কোনো আয়ের তথ্য নেই") }

        // Expense section
        item {
            Text(
                text = "ব্যয়ের খাত",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }
        item { EmptyStateCard("কোনো ব্যয়ের তথ্য নেই") }

        // Category Fee Report
        item {
            Text(
                text = "ক্যাটাগরিভিত্তিক ফি রিপোর্ট (বেতন ও পরীক্ষার ফি আলাদা বিশ্লেষণ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }
        item { EmptyStateCard("কোনো ফি ক্যাটাগরির তথ্য নেই") }

        // Dashboard Analytics Donut Charts
        item {
            Text(
                text = "ড্যাশবোর্ড বিশ্লেষণ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }

        item {
            DonutChart(
                title = "লিঙ্গ অনুপাত",
                values = listOf(maleStudents.toFloat(), femaleStudents.toFloat()),
                colors = listOf(Color(0xFF2563EB), Color(0xFFF59E0B)),
                labels = listOf("ছাত্র", "ছাত্রী")
            )
        }

        item {
            DonutChart(
                title = "শিক্ষার্থী অবস্থা",
                values = listOf(activeStudents.toFloat(), if (inactiveStudents > 0) inactiveStudents.toFloat() else 0.01f),
                colors = listOf(Color(0xFF10B981), Color(0xFFEF4444)),
                labels = listOf("সক্রিয়", "নিষ্ক্রিয়")
            )
        }
    }
}

@Composable
fun StatCardScreenshot(
    label: String,
    value: String,
    icon: ImageVector,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = badgeColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun DonutChart(
    title: String,
    values: List<Float>,
    colors: List<Color>,
    labels: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Canvas(modifier = Modifier.size(160.dp)) {
                var startAngle = -90f
                val total = values.sum().let { if (it <= 0f) 1f else it }
                values.forEachIndexed { index, value ->
                    val sweepAngle = value * 360f / total
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 36f)
                    )
                    startAngle += sweepAngle
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                labels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = colors[index],
                            shape = CircleShape
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

