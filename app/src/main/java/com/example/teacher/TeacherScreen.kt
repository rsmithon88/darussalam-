package com.example.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherScreen(
    repository: TeacherRepository,
    classList: List<String> = listOf("শ্রেণি প্লে", "শ্রেণি নার্সারি", "শ্রেণি প্রথম", "নূরানী", "নাজেরা", "হিফজ", "ইবতেদায়ী", "দাখিল"),
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teachers by repository.teachers.collectAsState()
    val attendances by repository.attendances.collectAsState()

    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var viewingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "শিক্ষক প্যানেল ও হাজিরা",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "মোট শিক্ষক: ${teachers.size} জন • সময়সূচী: সকাল ৮:০০ - বিকাল ৪:০০",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (selectedTab == 1) {
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("শিক্ষক যোগ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1D4ED8),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("এক ক্লিকে হাজিরা", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("শিক্ষক তালিকা", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> TeacherAttendanceTabContent(
                repository = repository,
                teachers = teachers,
                attendances = attendances
            )
            1 -> TeacherManagementTabContent(
                teachers = teachers,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onView = { viewingTeacher = it },
                onEdit = { editingTeacher = it },
                onDelete = { teacherId ->
                    repository.deleteTeacher(teacherId)
                    Toast.makeText(context, "শিক্ষকের তথ্য মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Add Teacher Dialog
    if (showAddDialog) {
        AddOrEditTeacherDialog(
            title = "নতুন শিক্ষক যোগ করুন",
            initialTeacher = Teacher(),
            classList = classList,
            onDismiss = { showAddDialog = false },
            onConfirm = { newTeacher ->
                repository.addTeacher(newTeacher)
                showAddDialog = false
                Toast.makeText(context, "নতুন শিক্ষক সফলভাবে যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Teacher Dialog
    editingTeacher?.let { teacher ->
        AddOrEditTeacherDialog(
            title = "শিক্ষকের তথ্য সম্পাদনা করুন",
            initialTeacher = teacher,
            classList = classList,
            onDismiss = { editingTeacher = null },
            onConfirm = { updatedTeacher ->
                repository.updateTeacher(updatedTeacher)
                editingTeacher = null
                Toast.makeText(context, "শিক্ষকের তথ্য আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // View Detailed Teacher Info Dialog
    viewingTeacher?.let { teacher ->
        TeacherDetailDialog(
            teacher = teacher,
            onDismiss = { viewingTeacher = null },
            onEditRequest = {
                editingTeacher = teacher
                viewingTeacher = null
            }
        )
    }
}

// ------------------- TAB 0: TEACHER ATTENDANCE (ONE CLICK + TIME RESTRICTION) -------------------
@Composable
fun TeacherAttendanceTabContent(
    repository: TeacherRepository,
    teachers: List<Teacher>,
    attendances: List<TeacherAttendance>
) {
    val context = LocalContext.current
    val isTimeActive = repository.isAttendanceWindowActive()

    val todayDateString = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
    }
    val todayAttendanceMap = remember(attendances, todayDateString) {
        attendances.filter { it.date == todayDateString }.associateBy { it.teacherId }
    }

    val presentCount = todayAttendanceMap.values.count { it.status == "উপস্থিত" || it.status == "বিলম্ব" }
    val absentCount = todayAttendanceMap.values.count { it.status == "অনুপস্থিত" }

    var selectedFilter by remember { mutableStateOf("সকল") } // "সকল", "উপস্থিত", "অনুপস্থিত"
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = teachers.filter { teacher ->
        val record = todayAttendanceMap[teacher.id]
        val matchesSearch = teacher.name.contains(searchQuery, ignoreCase = true) || teacher.designation.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "উপস্থিত" -> record?.status == "উপস্থিত" || record?.status == "বিলম্ব"
            "অনুপস্থিত" -> record?.status == "অনুপস্থিত" || record == null
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Time & Policy Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTimeActive) Color(0xFFEFF6FF) else Color(0xFFFEF2F2)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isTimeActive) Color(0xFFBFDBFE) else Color(0xFFFECACA)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isTimeActive) Color(0xFF1D4ED8) else Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "হাজিরা গ্রহণের সময়সূচী",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        // Status Badge
                        Surface(
                            color = if (isTimeActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTimeActive) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isTimeActive) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTimeActive) "হাজিরা সচল" else "হাজিরা সময় সমাপ্ত",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimeActive) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📌 সকাল ০৮:০০ টা থেকে নিয়ে বিকাল ০৪:০০ টা পর্যন্ত শিক্ষকরা হাজিরা দিতে পারবেন। নির্ধারিত সময়ের বাইরে হাজিরা দেয়া যাবে না।",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Summary Statistics & Bulk 1-Click Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "আজকের হাজিরা সামারি",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "উপস্থিত: $presentCount জন • বাকি: ${teachers.size - presentCount} জন",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = {
                                val result = repository.markAllTeachersPresent()
                                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimeActive) Color(0xFF059669) else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সবাইকে উপস্থিত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search & Filter row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("শিক্ষকের নাম খুঁজুন...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                listOf("সকল", "উপস্থিত", "অনুপস্থিত").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Teachers Attendance List Items
        items(filteredList, key = { it.id }) { teacher ->
            val attendanceRecord = todayAttendanceMap[teacher.id]
            val isPresent = attendanceRecord?.status == "উপস্থিত" || attendanceRecord?.status == "বিলম্ব"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = teacher.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = teacher.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${teacher.designation} • ${teacher.subject.ifBlank { "সাধারণ" }}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            if (attendanceRecord != null) {
                                Text(
                                    text = "স্ট্যাটাস: ${attendanceRecord.status} (${attendanceRecord.time})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPresent) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            } else {
                                Text(
                                    text = "আজকের হাজিরা দেয়া হয়নি",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    // ONE-CLICK ATTENDANCE BUTTON
                    Button(
                        onClick = {
                            val result = repository.markSingleAttendance(
                                teacher = teacher,
                                status = if (isPresent) "অনুপস্থিত" else "উপস্থিত"
                            )
                            Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPresent) Color(0xFFDC2626) else Color(0xFF16A34A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPresent) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPresent) "অনুপস্থিতি" else "উপস্থিত দিন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ------------------- TAB 1: TEACHER MANAGEMENT -------------------
@Composable
fun TeacherManagementTabContent(
    teachers: List<Teacher>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onView: (Teacher) -> Unit,
    onEdit: (Teacher) -> Unit,
    onDelete: (String) -> Unit
) {
    val filteredTeachers = teachers.filter { teacher ->
        teacher.name.contains(searchQuery, ignoreCase = true) ||
                teacher.subject.contains(searchQuery, ignoreCase = true) ||
                teacher.phone.contains(searchQuery, ignoreCase = true) ||
                teacher.designation.contains(searchQuery, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("শিক্ষকের নাম, বিষয় বা ফোন নম্বর দিয়ে খুঁজুন...", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        // Teacher List Cards
        if (filteredTeachers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো শিক্ষকের তথ্য পাওয়া যায়নি",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTeachers, key = { it.id }) { teacher ->
                    TeacherCardItem(
                        teacher = teacher,
                        onView = { onView(teacher) },
                        onEdit = { onEdit(teacher) },
                        onDelete = { onDelete(teacher.id) }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherCardItem(
    teacher: Teacher,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = teacher.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            if (teacher.isSuperAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF7C3AED),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "সুপার অ্যাডমিন",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${teacher.designation} • ${teacher.subject.ifBlank { "সকল বিষয়" }}",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ফোন: ${teacher.phone.ifBlank { "নেই" }}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ইমেইল: ${teacher.email.ifBlank { "নেই" }}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (teacher.permissions.isNotEmpty() || teacher.assignedClasses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    teacher.permissions.take(4).forEach { perm ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = perm,
                                fontSize = 11.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (teacher.permissions.size > 4) {
                        Surface(
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "+${teacher.permissions.size - 4}টি",
                                fontSize = 11.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Teacher Registration Dialog matching Screenshots 1 & 2
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOrEditTeacherDialog(
    title: String,
    initialTeacher: Teacher,
    classList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Teacher) -> Unit
) {
    var photoUrl by remember { mutableStateOf(initialTeacher.photoUrl) }
    var name by remember { mutableStateOf(initialTeacher.name) }
    var designation by remember { mutableStateOf(initialTeacher.designation) }
    var subject by remember { mutableStateOf(initialTeacher.subject) }
    var address by remember { mutableStateOf(initialTeacher.address) }
    var email by remember { mutableStateOf(initialTeacher.email) }
    var phone by remember { mutableStateOf(initialTeacher.phone) }
    var gender by remember { mutableStateOf(initialTeacher.gender) }
    var password by remember { mutableStateOf(initialTeacher.password) }
    var isSuperAdmin by remember { mutableStateOf(initialTeacher.isSuperAdmin) }

    var selectedPermissions by remember { mutableStateOf(initialTeacher.permissions.toSet()) }
    var selectedClasses by remember { mutableStateOf(initialTeacher.assignedClasses.toSet()) }

    var designationExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    val designationOptions = listOf("সাধারণ শিক্ষক", "প্রধান শিক্ষক", "সহকারী শিক্ষক", "প্রধান মুহতামিম", "হিসাবরক্ষক", "মুফতী/কারী")
    val allPermissions = listOf(
        "শিক্ষার্থীরা", "হাজিরা", "শিক্ষক হাজিরা", "ফি ব্যবস্থাপনা",
        "ব্যয় ব্যবস্থাপনা", "পরীক্ষা", "রেজাল্ট", "সময়সূচী",
        "নোটিশ বোর্ড", "রিপোর্ট", "শিক্ষার্থী তথ্য", "ছুটির আবেদন",
        "ইনফরমেশন ডাউনলোড"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with title and X button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Scrollable Form Fields matching Screenshots 1 & 2
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Photo Avatar Placeholder (Screenshot 1)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF93C5FD))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "প্রোফাইল ছবি সেট করুন (ঐচ্ছিক)",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "অথবা ফটোর লিংক ব্যবহার করুন:",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Photo URL
                    item {
                        OutlinedTextField(
                            value = photoUrl,
                            onValueChange = { photoUrl = it },
                            placeholder = { Text("ফটোর URL লিংক দিন...", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }

                    // Full Name
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("পুরো নাম", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Designation Dropdown (Screenshot 1)
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { designationExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = designation, color = Color(0xFF1E293B))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }

                            DropdownMenu(
                                expanded = designationExpanded,
                                onDismissRequest = { designationExpanded = false }
                            ) {
                                designationOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            designation = option
                                            designationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Subject
                    item {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            placeholder = { Text("বিষয়", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Address
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            placeholder = { Text("ঠিকানা", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Login Email
                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("লগইন ইমেইল", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Mobile Number
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("মোবাইল নম্বর", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Gender Dropdown
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { genderExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = gender, color = Color(0xFF1E293B))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }

                            DropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("পুরুষ") },
                                    onClick = {
                                        gender = "পুরুষ"
                                        genderExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("নারী") },
                                    onClick = {
                                        gender = "নারী"
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Login Password
                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("লগইন পাসওয়ার্ড", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Checkbox: সুপার অ্যাডমিন বানান (Screenshots 1 & 2)
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSuperAdmin = !isSuperAdmin }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isSuperAdmin,
                                onCheckedChange = { isSuperAdmin = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "সুপার অ্যাডমিন বানান",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "(সেটিংস, অ্যাডমিন রোল ও শিক্ষকগণ ব্যতীত সকল ফিচার ব্যবহার করতে পারবেন)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }

                    // দায়িত্বসমূহ Section (Screenshot 2)
                    item {
                        Text(
                            text = "দায়িত্বসমূহ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    // Permissions Checkbox Grid (Screenshot 2)
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allPermissions.chunked(2).forEach { rowPerms ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPerms.forEach { perm ->
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedPermissions = if (selectedPermissions.contains(perm)) {
                                                        selectedPermissions - perm
                                                    } else {
                                                        selectedPermissions + perm
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = selectedPermissions.contains(perm),
                                                onCheckedChange = { checked ->
                                                    selectedPermissions = if (checked) {
                                                        selectedPermissions + perm
                                                    } else {
                                                        selectedPermissions - perm
                                                    }
                                                }
                                            )
                                            Text(
                                                text = perm,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                    }
                                    if (rowPerms.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }

                    // শ্রেণীভিত্তিক পাওয়ার (Class-based Power) Section
                    item {
                        Column {
                            Text(
                                text = "শ্রেণীভিত্তিক পাওয়ার (অনুমোদিত শ্রেণীসমূহ)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "নির্দিষ্ট শ্রেণীর দায়িত্ব দিতে পছন্দের শ্রেণী সিলেক্ট করুন:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            classList.forEach { cls ->
                                val isSelected = selectedClasses.contains(cls)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedClasses = if (isSelected) {
                                            selectedClasses - cls
                                        } else {
                                            selectedClasses + cls
                                        }
                                    },
                                    label = { Text(cls, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1D4ED8),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Buttons Row (Screenshot 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল", color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    initialTeacher.copy(
                                        photoUrl = photoUrl,
                                        name = name,
                                        designation = designation,
                                        subject = subject,
                                        address = address,
                                        email = email,
                                        phone = phone,
                                        gender = gender,
                                        password = password,
                                        isSuperAdmin = isSuperAdmin,
                                        permissions = selectedPermissions.toList(),
                                        assignedClasses = selectedClasses.toList()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("শিক্ষক যোগ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherDetailDialog(
    teacher: Teacher,
    onDismiss: () -> Unit,
    onEditRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "শিক্ষকের বিস্তারিত তথ্য",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { com.example.student.DetailRow(label = "নাম", value = teacher.name) }
                    item { com.example.student.DetailRow(label = "পদবী", value = teacher.designation) }
                    item { com.example.student.DetailRow(label = "বিষয়", value = teacher.subject.ifBlank { "সকল বিষয়" }) }
                    item { com.example.student.DetailRow(label = "মোবাইল", value = teacher.phone.ifBlank { "তথ্য নেই" }) }
                    item { com.example.student.DetailRow(label = "ইমেইল", value = teacher.email.ifBlank { "তথ্য নেই" }) }
                    item { com.example.student.DetailRow(label = "ঠিকানা", value = teacher.address.ifBlank { "তথ্য নেই" }) }
                    item { com.example.student.DetailRow(label = "লিঙ্গ", value = teacher.gender) }
                    item { com.example.student.DetailRow(label = "রোল/পাওয়ার", value = if (teacher.isSuperAdmin) "সুপার অ্যাডমিন" else "সাধারণ শিক্ষক") }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("দায়িত্বসমূহ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Text(
                            text = if (teacher.permissions.isEmpty()) "কোনো নির্দিষ্ট দায়িত্ব বরাদ্দ নেই" else teacher.permissions.joinToString(", "),
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("শ্রেণীভিত্তিক পাওয়ার:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Text(
                            text = if (teacher.assignedClasses.isEmpty()) "সকল শ্রেণীর পাওয়ার প্রযোজ্য" else teacher.assignedClasses.joinToString(", "),
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("বন্ধ করুন")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onEditRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সম্পাদনা করুন")
                    }
                }
            }
        }
    }
}
