package com.example.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.notice.NoticeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private data class TeacherMenuItemData(
    val title: String,
    val icon: ImageVector,
    val requiredPermissionKey: String? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherDashboardScreen(
    teacher: Teacher,
    repository: TeacherRepository,
    onLogout: () -> Unit = {},
    onNavigateToStudents: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Repositories for permitted modules
    val studentRepository = remember { com.example.student.StudentRepository() }
    val examRepository = remember { com.example.exam.ExamRepository() }

    // Live list of teachers from repository to sync permissions on the fly
    val allTeachers by repository.teachers.collectAsState()
    val currentTeacher = remember(allTeachers, teacher.id) {
        allTeachers.firstOrNull { it.id == teacher.id } ?: teacher
    }

    var selectedMenu by remember { mutableStateOf("ড্যাশবোর্ড") }

    // Attendance data
    val attendances by repository.attendances.collectAsState()
    val isTimeActive = repository.isAttendanceWindowActive()

    val todayDateString = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }
    val todayFormattedBangla = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("bn", "BD"))
        sdf.format(Date())
    }

    val myTodayRecord = remember(attendances, todayDateString, currentTeacher.id) {
        attendances.firstOrNull { it.teacherId == currentTeacher.id && it.date == todayDateString }
    }
    val isPresentToday = myTodayRecord?.status == "উপস্থিত" || myTodayRecord?.status == "বিলম্ব"

    var showTimeErrorDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    // List of master menu items with permissions mapping
    val allMenuItems = remember {
        listOf(
            TeacherMenuItemData("ড্যাশবোর্ড", Icons.Default.Dashboard),
            TeacherMenuItemData("শিক্ষার্থীরা", Icons.Default.People, "শিক্ষার্থীরা"),
            TeacherMenuItemData("হাজিরা", Icons.Default.EventAvailable, "হাজিরা"),
            TeacherMenuItemData("শিক্ষক হাজিরা", Icons.Default.CheckCircle, "শিক্ষক হাজিরা"),
            TeacherMenuItemData("ফি ব্যবস্থাপনা", Icons.Default.Payments, "ফি ব্যবস্থাপনা"),
            TeacherMenuItemData("ব্যয় ব্যবস্থাপনা", Icons.Default.ReceiptLong, "ব্যয় ব্যবস্থাপনা"),
            TeacherMenuItemData("শিক্ষকদের বেতন", Icons.Default.AccountBalance, "শিক্ষকদের বেতন"),
            TeacherMenuItemData("পরীক্ষা", Icons.Default.Quiz, "পরীক্ষা"),
            TeacherMenuItemData("রেজাল্ট", Icons.Default.Assessment, "রেজাল্ট"),
            TeacherMenuItemData("ছুটির আবেদন", Icons.Default.CardTravel, "ছুটির আবেদন"),
            TeacherMenuItemData("সময়সূচী", Icons.Default.Schedule, "সময়সূচী"),
            TeacherMenuItemData("নোটিশ বোর্ড", Icons.Default.Campaign, "নোটিশ বোর্ড"),
            TeacherMenuItemData("রিপোর্ট", Icons.Default.BarChart, "রিপোর্ট"),
            TeacherMenuItemData("শিক্ষার্থী তথ্য", Icons.Default.ContactPage, "শিক্ষার্থী তথ্য")
        )
    }

    // Filter menu items by current teacher's permissions
    val accessibleMenuItems = remember(currentTeacher.permissions, currentTeacher.isSuperAdmin) {
        if (currentTeacher.isSuperAdmin) {
            allMenuItems
        } else {
            allMenuItems.filter { item ->
                item.requiredPermissionKey == null || currentTeacher.permissions.contains(item.requiredPermissionKey)
            }
        }
    }

    fun syncPowerAndPermissions() {
        Toast.makeText(context, "⚡ ক্ষমতা ও পারমিশন সফলভাবে সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF024BB0),
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drawer Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentTeacher.name.take(1),
                                        color = Color(0xFF024BB0),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentTeacher.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentTeacher.designation} • শিক্ষক পোর্টাল",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    // Menu List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp)
                    ) {
                        items(accessibleMenuItems) { item ->
                            val isSelected = item.title == selectedMenu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF1E5BB8) else Color.Transparent)
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        selectedMenu = item.title
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

                        // Static bottom items
                        item {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        selectedMenu = "আমার প্রোফাইল"
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "আমার প্রোফাইল",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        syncPowerAndPermissions()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "ক্ষমতা রিফ্রেশ করুন",
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        onLogout()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "লগআউট",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                        // Refresh Power / Permissions Button
                        IconButton(onClick = { syncPowerAndPermissions() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Power",
                                tint = Color(0xFF2563EB)
                            )
                        }

                        // Notifications Icon
                        IconButton(onClick = {
                            Toast.makeText(context, "কোন নতুন নোটিফিকেশন নেই", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF64748B)
                            )
                        }

                        // Profile Avatar Icon
                        IconButton(onClick = { selectedMenu = "আমার প্রোফাইল" }) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = Color(0xFF2563EB)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentTeacher.name.take(1),
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            val allowedClasses = remember(currentTeacher) {
                if (currentTeacher.isSuperAdmin || currentTeacher.assignedClasses.contains("সকল শ্রেণী") || currentTeacher.assignedClasses.contains("সমস্ত শ্রেণী")) {
                    null
                } else {
                    currentTeacher.assignedClasses.ifEmpty { null }
                }
            }

            when (selectedMenu) {
                "ড্যাশবোর্ড" -> {
                    TeacherMainDashboardContent(
                        teacher = currentTeacher,
                        repository = repository,
                        todayFormattedBangla = todayFormattedBangla,
                        isTimeActive = isTimeActive,
                        isPresentToday = isPresentToday,
                        myTodayRecord = myTodayRecord,
                        onMarkAttendance = {
                            if (!isTimeActive) {
                                showTimeErrorDialog = true
                            } else {
                                val result = repository.markSingleAttendance(teacher = currentTeacher, status = "উপস্থিত")
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        onOpenLeaveDialog = { showLeaveDialog = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষার্থীরা", "শিক্ষার্থী তথ্য" -> {
                    com.example.student.StudentScreen(
                        repository = studentRepository,
                        allowedClasses = allowedClasses,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "হাজিরা" -> {
                    com.example.attendance.AttendanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = repository,
                        allowedClasses = allowedClasses,
                        initialTab = 0,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষক হাজিরা" -> {
                    com.example.attendance.AttendanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = repository,
                        allowedClasses = allowedClasses,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ফি ব্যবস্থাপনা" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = repository,
                        allowedClasses = allowedClasses,
                        initialTab = 0,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ব্যয় ব্যবস্থাপনা" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = repository,
                        allowedClasses = allowedClasses,
                        initialTab = 1,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "শিক্ষকদের বেতন" -> {
                    com.example.finance.FinanceScreen(
                        studentRepository = studentRepository,
                        teacherRepository = repository,
                        allowedClasses = allowedClasses,
                        initialTab = 2,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "পরীক্ষা", "রেজাল্ট" -> {
                    com.example.exam.ExamScreen(
                        examRepository = examRepository,
                        studentRepository = studentRepository,
                        allowedClasses = allowedClasses,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                "ছুটির আবেদন" -> {
                    com.example.admin.AdminRolesScreen(
                        teacherRepository = repository,
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
                "আমার প্রোফাইল" -> {
                    TeacherProfileFullSection(
                        teacher = currentTeacher,
                        repository = repository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                else -> {
                    TeacherMainDashboardContent(
                        teacher = currentTeacher,
                        repository = repository,
                        todayFormattedBangla = todayFormattedBangla,
                        isTimeActive = isTimeActive,
                        isPresentToday = isPresentToday,
                        myTodayRecord = myTodayRecord,
                        onMarkAttendance = {
                            if (!isTimeActive) {
                                showTimeErrorDialog = true
                            } else {
                                val result = repository.markSingleAttendance(teacher = currentTeacher, status = "উপস্থিত")
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            }
                        },
                        onOpenLeaveDialog = { showLeaveDialog = true },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Leave Application Dialog
    if (showLeaveDialog) {
        var leaveReason by remember { mutableStateOf("") }
        var leaveDays by remember { mutableStateOf("১") }

        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            icon = { Icon(Icons.Default.EventNote, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(36.dp)) },
            title = { Text("ছুটির আবেদন জমা দিন", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ছুটির কারণ এবং কতদিনের ছুটি প্রয়োজন তা উল্লেখ করুন:", fontSize = 13.sp, color = Color(0xFF475569))
                    OutlinedTextField(
                        value = leaveDays,
                        onValueChange = { leaveDays = it },
                        label = { Text("ছুটির দিন সংখ্যা") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text("ছুটির কারণ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (leaveReason.isNotBlank()) {
                            Toast.makeText(context, "ছুটির আবেদন এডমিন প্যানেলে জমা দেওয়া হয়েছে!", Toast.LENGTH_LONG).show()
                            showLeaveDialog = false
                        } else {
                            Toast.makeText(context, "অনুগ্রহ করে কারণ লিখুন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("আবেদন জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Time Error Dialog
    if (showTimeErrorDialog) {
        AlertDialog(
            onDismissRequest = { showTimeErrorDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp)) },
            title = { Text("হাজিরা সময় শেষ", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Text(
                    text = "দুঃখিত! শিক্ষক হাজিরা গ্রহণের নির্ধারিত সময় সকাল ০৮:০০ টা থেকে নিয়ে বিকাল ০৪:০০ টা পর্যন্ত।\n\nবর্তমানে সময় পার হয়ে যাওয়ার কারণে হাজিরা দেওয়া সম্ভব নয়।",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTimeErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeacherMainDashboardContent(
    teacher: Teacher,
    repository: TeacherRepository,
    todayFormattedBangla: String,
    isTimeActive: Boolean,
    isPresentToday: Boolean,
    myTodayRecord: TeacherAttendance?,
    onMarkAttendance: () -> Unit,
    onOpenLeaveDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notices by NoticeRepository.notices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card (Matching Screenshot 1)
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "স্বাগতম, ${teacher.name}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "আপনার ব্যক্তিগত ড্যাশবোর্ডে স্বাগতম।",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Latest Notices Container (Matching Screenshot 1)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), // Light lavender
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "সর্বশেষ নোটিশ সমূহ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B21B6)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (notices.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "বর্তমানে কোনো নোটিশ নেই",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "এডমিন প্যানেল থেকে কোনো নোটিশ প্রকাশ করা হলে এখানে দেখা যাবে।",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            notices.forEach { notice ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notice.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Surface(
                                                color = Color(0xFFEEF2FF),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = notice.date,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF4F46E5),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = notice.description,
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 19.sp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "প্রাপক: ${notice.targetAudience}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Surface(
                                                color = Color(0xFFF1F5F9),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "এডমিন নোটিশ",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF475569),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Activities Section (Matching Screenshot 1)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "দৈনিক কার্যক্রম",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                // 1. Daily Attendance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "দৈনিক হাজিরা",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = todayFormattedBangla,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isPresentToday) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF15803D))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "আজকের হাজিরা সম্পন্ন হয়েছে (${myTodayRecord?.time ?: ""})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = onMarkAttendance,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTimeActive) Color(0xFF2563EB) else Color(0xFFEF4444)
                                )
                            ) {
                                Text(
                                    text = if (isTimeActive) "এক ক্লিকে হাজিরা দিন" else "হাজিরার সময় শেষ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "উপস্থিতির সময়: সকাল ৮:০০ - বিকাল ৪:০০",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // 2. Leave Application Card (Matching Screenshot 1 & 2)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ছুটির আবেদন",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "প্রয়োজনে ছুটির জন্য আবেদন করুন",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onOpenLeaveDialog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text(
                                text = "আবেদন করুন",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // My Profile Section (Matching Screenshot 2 & 3)
        item {
            TeacherProfileCard(
                teacher = teacher,
                repository = repository
            )
        }

        // My Courses Section (Matching Screenshot 3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আমার কোর্সসমূহ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (teacher.assignedClasses.isEmpty()) {
                        Text(
                            text = "আপনাকে এখনও কোনো কোর্স বরাদ্দ করা হয়নি।",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            teacher.assignedClasses.forEach { cls ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(cls, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // My Weekly Schedule Section (Matching Screenshot 3)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "আমার সাপ্তাহিক সময়সূচী",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "সাপ্তাহিক ক্লাসের সময়সূচী নিয়মিত দেখতে 'সময়সূচী' মেনু ব্যবহার করুন।",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherProfileCard(
    teacher: Teacher,
    repository: TeacherRepository
) {
    val context = LocalContext.current
    val canEdit = teacher.isSuperAdmin

    var designation by remember(teacher) { mutableStateOf(teacher.designation.ifBlank { "সহকারী শিক্ষক" }) }
    var subject by remember(teacher) { mutableStateOf(teacher.subject.ifBlank { "বাংলা, ইংরেজি ও গণিত" }) }
    var email by remember(teacher) { mutableStateOf(teacher.email.ifBlank { "shohelrana4296@gmail.com" }) }
    var address by remember(teacher) { mutableStateOf(teacher.address.ifBlank { "পোড়াবাড়ি গোপালপুর" }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আমার প্রোফাইল",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                if (!canEdit) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("এডমিন দ্বারা নিয়ন্ত্রিত", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Picture
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .border(3.dp, Color(0xFFDBEAFE), CircleShape),
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = teacher.name.take(1),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = teacher.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "মোবাইল: ${teacher.phone.ifBlank { "N/A" }}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fields
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = designation,
                    onValueChange = { if (canEdit) designation = it },
                    readOnly = !canEdit,
                    enabled = canEdit,
                    label = { Text("পদবী:") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = if (!canEdit) OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF1E293B),
                        disabledBorderColor = Color(0xFFCBD5E1),
                        disabledLabelColor = Color(0xFF64748B),
                        disabledContainerColor = Color(0xFFF8FAFC)
                    ) else OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { if (canEdit) subject = it },
                    readOnly = !canEdit,
                    enabled = canEdit,
                    label = { Text("বিষয়:") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = if (!canEdit) OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF1E293B),
                        disabledBorderColor = Color(0xFFCBD5E1),
                        disabledLabelColor = Color(0xFF64748B),
                        disabledContainerColor = Color(0xFFF8FAFC)
                    ) else OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { if (canEdit) email = it },
                    readOnly = !canEdit,
                    enabled = canEdit,
                    label = { Text("ইমেইল:") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = if (!canEdit) OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF1E293B),
                        disabledBorderColor = Color(0xFFCBD5E1),
                        disabledLabelColor = Color(0xFF64748B),
                        disabledContainerColor = Color(0xFFF8FAFC)
                    ) else OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { if (canEdit) address = it },
                    readOnly = !canEdit,
                    enabled = canEdit,
                    label = { Text("ঠিকানা:") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = if (!canEdit) OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color(0xFF1E293B),
                        disabledBorderColor = Color(0xFFCBD5E1),
                        disabledLabelColor = Color(0xFF64748B),
                        disabledContainerColor = Color(0xFFF8FAFC)
                    ) else OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (canEdit) {
                    Button(
                        onClick = {
                            val updated = teacher.copy(
                                designation = designation,
                                subject = subject,
                                email = email,
                                address = address
                            )
                            repository.updateTeacher(updated)
                            Toast.makeText(context, "প্রোফাইল তথ্য সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("তথ্য সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "শিক্ষকের প্রোফাইল তথ্য পরিবর্তনের অনুমতি শুধুমাত্র এডমিন প্যানেলের রয়েছে।",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherProfileFullSection(
    teacher: Teacher,
    repository: TeacherRepository,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            TeacherProfileCard(
                teacher = teacher,
                repository = repository
            )
        }
    }
}
