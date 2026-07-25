package com.example.attendance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.settings.SettingsManager
import com.example.student.Student
import com.example.student.StudentRepository
import com.example.teacher.TeacherRepository
import com.example.teacher.isClassAllowed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    studentRepository: StudentRepository = remember { StudentRepository() },
    teacherRepository: TeacherRepository = remember { TeacherRepository() },
    attendanceRepository: AttendanceRepository = remember { AttendanceRepository() },
    allowedClasses: List<String>? = null,
    initialTab: Int = 0, // 0: Student Attendance, 1: Teacher GPS Attendance
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val smsSettings by SettingsManager.instance.smsSettings.collectAsState()
    val students by studentRepository.students.collectAsState()
    val teachers by teacherRepository.teachers.collectAsState()
    val repoClassList by studentRepository.classList.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }

    val baseClasses = listOf("হেফজ", "মিশকাত", "শরহে বেকায়া", "নাহবে মীর", "মিজান")
    val combinedClasses = (repoClassList + baseClasses).distinct()

    val effectiveClasses = remember(combinedClasses, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            combinedClasses
        } else {
            val filtered = combinedClasses.filter { isClassAllowed(it, allowedClasses) }
            if (filtered.isNotEmpty()) filtered else allowedClasses
        }
    }

    var selectedClass by remember(effectiveClasses) { mutableStateOf(effectiveClasses.firstOrNull() ?: "হেফজ") }

    // Attendance State: Map<StudentId, Status> where Status is "উপস্থিত", "অনুপস্থিত", "ছুটি"
    val attendanceMap = remember { mutableStateMapOf<String, String>() }

    // Initialize map
    LaunchedEffect(students) {
        students.forEach { st ->
            if (!attendanceMap.containsKey(st.id)) {
                attendanceMap[st.id] = "উপস্থিত"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1D4ED8)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("শিক্ষার্থী হাজিরা", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("শিক্ষক জিপিএস হাজিরা", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            // STUDENT ATTENDANCE SECTION
            val filteredStudents = students.filter { isClassAllowed(it.jamatClass, listOf(selectedClass)) }
            val presentCount = filteredStudents.count { attendanceMap[it.id] == "উপস্থিত" }
            val absentCount = filteredStudents.count { attendanceMap[it.id] == "অনুপস্থিত" }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("শ্রেণি নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        effectiveClasses.forEach { cls ->
                            FilterChip(
                                selected = selectedClass == cls,
                                onClick = { selectedClass = cls },
                                label = { Text(cls, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("উপস্থিতি সামারি:", fontSize = 12.sp, color = Color.Gray)
                        Text("উপস্থিত: $presentCount জন | অনুপস্থিত: $absentCount জন", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("শিক্ষার্থী তালিকা ($selectedClass):", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                Button(
                    onClick = {
                        val today = attendanceRepository.getTodayDateString()
                        val timeNow = attendanceRepository.getCurrentTimeString()
                        val attendanceRecords = filteredStudents.map { st ->
                            StudentAttendance(
                                date = today,
                                time = timeNow,
                                jamatClass = selectedClass,
                                studentId = st.id,
                                studentName = st.name,
                                roll = st.roll,
                                status = attendanceMap[st.id] ?: "উপস্থিত",
                                guardianContact = st.guardianContact
                            )
                        }
                        attendanceRepository.saveBatchStudentAttendance(attendanceRecords) { _, count ->
                            val absentStudents = filteredStudents.filter { attendanceMap[it.id] == "অনুপস্থিত" }
                            val gateway = smsSettings.gatewayName
                            Toast.makeText(
                                context,
                                "ফায়ারস্টোরে $count টি হাজিরা সেভ হয়েছে! $gateway দিয়ে ${absentStudents.size} জন অনুপস্থিত শিক্ষার্থীর অভিভাবককে SMS পাঠানো হয়েছে।",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("হাজিরা সেভ ও SMS পাঠান")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents, key = { it.id }) { student ->
                    val status = attendanceMap[student.id] ?: "উপস্থিত"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
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
                            Column {
                                Text(
                                    text = "${student.roll}. ${student.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "অভিভাবক: ${student.guardianContact.ifBlank { "01700000000" }}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = status == "উপস্থিত",
                                    onClick = { attendanceMap[student.id] = "উপস্থিত" },
                                    label = { Text("উপস্থিত", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFDCFCE7), selectedLabelColor = Color(0xFF15803D))
                                )
                                FilterChip(
                                    selected = status == "অনুপস্থিত",
                                    onClick = { attendanceMap[student.id] = "অনুপস্থিত" },
                                    label = { Text("অনুপস্থিত", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFEE2E2), selectedLabelColor = Color(0xFFB91C1C))
                                )
                                FilterChip(
                                    selected = status == "ছুটি",
                                    onClick = { attendanceMap[student.id] = "ছুটি" },
                                    label = { Text("ছুটি", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFEF3C7), selectedLabelColor = Color(0xFFD97706))
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // TEACHER GPS ATTENDANCE SECTION
            com.example.teacher.TeacherScreen(
                repository = teacherRepository,
                initialTab = 0
            )
        }
    }
}
