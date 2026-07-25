package com.example.exam

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.student.Student
import com.example.student.StudentRepository
import com.example.teacher.isClassAllowed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    examRepository: ExamRepository,
    studentRepository: StudentRepository,
    allowedClasses: List<String>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exams by examRepository.exams.collectAsState()
    val allResults by examRepository.results.collectAsState()
    val rawStudents by studentRepository.students.collectAsState()
    val rawClassList by studentRepository.classList.collectAsState()

    val effectiveClassList = remember(rawClassList, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            rawClassList
        } else {
            val filtered = rawClassList.filter { isClassAllowed(it, allowedClasses) }
            if (filtered.isNotEmpty()) filtered else allowedClasses
        }
    }

    val classList = effectiveClassList

    val allStudents = remember(rawStudents, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            rawStudents
        } else {
            rawStudents.filter { isClassAllowed(it.jamatClass, allowedClasses) }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Exams, 1: Mark Entry, 2: Result Sheet
    var showAddExamDialog by remember { mutableStateOf(false) }

    // Navigation trigger from Exam card to Mark Entry or Result Sheet
    var selectedExamForEntry by remember { mutableStateOf<Exam?>(null) }
    var selectedClassForEntry by remember { mutableStateOf<String>("") }
    var viewingStudentResult by remember { mutableStateOf<StudentResult?>(null) }
    var viewingAdmitCardStudent by remember { mutableStateOf<Student?>(null) }
    var viewingAdmitCardExam by remember { mutableStateOf<Exam?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Top Header Title & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "পরীক্ষা ও রেজাল্ট ব্যবস্থাপনা",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "পরীক্ষা তৈরি, নম্বর ইনপুট, রেজাল্ট সিট ও প্রবেশপত্র",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (selectedTab == 0) {
                Button(
                    onClick = { showAddExamDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন পরীক্ষা", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF2563EB),
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
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পরীক্ষা", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নম্বর এন্ট্রি", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রেজাল্ট সিট", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রবেশপত্র", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Contents
        when (selectedTab) {
            0 -> ExamListTab(
                exams = exams,
                onAddExamClick = { showAddExamDialog = true },
                onMarkEntryClick = { exam ->
                    selectedExamForEntry = exam
                    if (exam.targetClasses.isNotEmpty()) {
                        selectedClassForEntry = exam.targetClasses.first()
                    }
                    selectedTab = 1
                },
                onResultSheetClick = { exam ->
                    selectedExamForEntry = exam
                    if (exam.targetClasses.isNotEmpty()) {
                        selectedClassForEntry = exam.targetClasses.first()
                    }
                    selectedTab = 2
                },
                onAdmitCardClick = { exam ->
                    selectedExamForEntry = exam
                    if (exam.targetClasses.isNotEmpty()) {
                        selectedClassForEntry = exam.targetClasses.first()
                    }
                    selectedTab = 3
                },
                onDeleteExam = { examId ->
                    examRepository.deleteExam(examId)
                    Toast.makeText(context, "পরীক্ষা মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            )

            1 -> MarkEntryTab(
                exams = exams,
                classList = classList,
                allStudents = allStudents,
                allResults = allResults,
                initialExam = selectedExamForEntry,
                initialClass = selectedClassForEntry,
                onSaveResults = { newResults ->
                    examRepository.saveStudentResults(newResults)
                    Toast.makeText(context, "রেজাল্ট সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_LONG).show()
                    selectedTab = 2 // Move to result sheet
                }
            )

            2 -> ResultSheetTab(
                exams = exams,
                classList = classList,
                allStudents = allStudents,
                allResults = allResults,
                initialExam = selectedExamForEntry,
                initialClass = selectedClassForEntry,
                onViewStudentCard = { result ->
                    viewingStudentResult = result
                }
            )

            3 -> AdmitCardTab(
                exams = exams,
                classList = classList,
                allStudents = allStudents,
                initialExam = selectedExamForEntry,
                initialClass = selectedClassForEntry,
                onViewAdmitCard = { student, exam ->
                    viewingAdmitCardStudent = student
                    viewingAdmitCardExam = exam
                }
            )
        }
    }

    // Add Exam Dialog
    if (showAddExamDialog) {
        AddExamDialog(
            classList = classList,
            onDismiss = { showAddExamDialog = false },
            onConfirm = { newExam ->
                examRepository.addExam(newExam)
                Toast.makeText(context, "নতুন পরীক্ষা সফলভাবে তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                showAddExamDialog = false
            }
        )
    }

    // Individual Student Result Card / Marksheet Dialog
    viewingStudentResult?.let { result ->
        val activeExam = exams.find { it.id == result.examId }
        StudentMarksheetDialog(
            result = result,
            exam = activeExam,
            onDismiss = { viewingStudentResult = null }
        )
    }

    // Individual Student Admit Card Dialog
    viewingAdmitCardStudent?.let { student ->
        AdmitCardDialog(
            student = student,
            exam = viewingAdmitCardExam,
            onDismiss = { viewingAdmitCardStudent = null }
        )
    }
}

// ------------------- TAB 1: EXAM LIST -------------------
@Composable
fun ExamListTab(
    exams: List<Exam>,
    onAddExamClick: () -> Unit,
    onMarkEntryClick: (Exam) -> Unit,
    onResultSheetClick: (Exam) -> Unit,
    onAdmitCardClick: (Exam) -> Unit,
    onDeleteExam: (String) -> Unit
) {
    if (exams.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("কোনো পরীক্ষা খুঁজে পাওয়া যায়নি", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAddExamClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                    Text("নতুন পরীক্ষা যোগ করুন")
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(exams) { exam ->
                ExamCard(
                    exam = exam,
                    onMarkEntryClick = { onMarkEntryClick(exam) },
                    onResultSheetClick = { onResultSheetClick(exam) },
                    onAdmitCardClick = { onAdmitCardClick(exam) },
                    onDeleteExam = { onDeleteExam(exam.id) }
                )
            }
        }
    }
}

@Composable
fun ExamCard(
    exam: Exam,
    onMarkEntryClick: () -> Unit,
    onResultSheetClick: () -> Unit,
    onAdmitCardClick: () -> Unit,
    onDeleteExam: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = exam.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "তারিখ: ${exam.date} • সেশন: ${exam.year}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF64748B))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("পরীক্ষা মুছুন", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDeleteExam()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "বিষয়সমূহ (${exam.subjects.size}টি):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = exam.subjects.joinToString(", "),
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        maxLines = 1
                    )
                }

                Surface(
                    color = when (exam.status) {
                        "প্রকাশিত" -> Color(0xFFDCFCE7)
                        "চলমান" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = exam.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (exam.status) {
                            "প্রকাশিত" -> Color(0xFF15803D)
                            "চলমান" -> Color(0xFFB45309)
                            else -> Color(0xFF475569)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onMarkEntryClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নম্বর", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onResultSheetClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("রেজাল্ট", fontSize = 12.sp)
                }

                Button(
                    onClick = onAdmitCardClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("প্রবেশপত্র", fontSize = 12.sp)
                }
            }
        }
    }
}

// ------------------- TAB 2: MARK ENTRY -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkEntryTab(
    exams: List<Exam>,
    classList: List<String>,
    allStudents: List<Student>,
    allResults: List<StudentResult>,
    initialExam: Exam?,
    initialClass: String,
    onSaveResults: (List<StudentResult>) -> Unit
) {
    if (exams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("প্রথমে একটি পরীক্ষা যোগ করুন।", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
        return
    }

    var selectedExam by remember { mutableStateOf(initialExam ?: exams.first()) }
    var selectedClass by remember { mutableStateOf(if (initialClass.isNotBlank()) initialClass else (selectedExam.targetClasses.firstOrNull() ?: classList.firstOrNull() ?: "শ্রেণি প্লে")) }

    var examDropdownExpanded by remember { mutableStateOf(false) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Students in selected class
    val classStudents = remember(selectedClass, allStudents) {
        allStudents.filter { it.jamatClass == selectedClass }
    }

    // Mutable state for marks entry: Student ID -> Map<SubjectName, StringMarks>
    val studentMarksState = remember { mutableStateMapOf<String, MutableMap<String, String>>() }

    // Populate existing marks if available
    LaunchedEffect(selectedExam, selectedClass, allResults, classStudents) {
        studentMarksState.clear()
        classStudents.forEach { student ->
            val existing = allResults.find { it.examId == selectedExam.id && it.studentId == student.id }
            val map = mutableStateMapOf<String, String>()
            selectedExam.subjects.forEach { subject ->
                val score = existing?.subjectMarks?.get(subject)
                map[subject] = if (score != null) score.toInt().toString() else ""
            }
            studentMarksState[student.id] = map
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection Bar (Exam & Class Filters)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("পরীক্ষা ও শ্রেণী নির্বাচন করুন:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Exam Dropdown
                    ExposedDropdownMenuBox(
                        expanded = examDropdownExpanded,
                        onExpandedChange = { examDropdownExpanded = !examDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = selectedExam.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("পরীক্ষা") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = examDropdownExpanded,
                            onDismissRequest = { examDropdownExpanded = false }
                        ) {
                            exams.forEach { ex ->
                                DropdownMenuItem(
                                    text = { Text(ex.title) },
                                    onClick = {
                                        selectedExam = ex
                                        if (ex.targetClasses.isNotEmpty()) {
                                            selectedClass = ex.targetClasses.first()
                                        }
                                        examDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Class Dropdown
                    ExposedDropdownMenuBox(
                        expanded = classDropdownExpanded,
                        onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("শ্রেণী") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = classDropdownExpanded,
                            onDismissRequest = { classDropdownExpanded = false }
                        ) {
                            classList.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        selectedClass = cls
                                        classDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (classStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("'$selectedClass'-এ কোনো শিক্ষার্থী ভর্তি নেই।", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "শিক্ষার্থী তালিকা (${classStudents.size} জন):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Button(
                    onClick = {
                        val newResults = mutableListOf<StudentResult>()
                        val fullMarks = (selectedExam.subjects.size * selectedExam.subjectTotalMarks).toDouble()

                        classStudents.forEach { student ->
                            val marksMapState = studentMarksState[student.id] ?: return@forEach
                            val subjectMarksMap = mutableMapOf<String, Double>()
                            var totalObtained = 0.0
                            var isPassed = true

                            selectedExam.subjects.forEach { subj ->
                                val score = marksMapState[subj]?.toDoubleOrNull() ?: 0.0
                                subjectMarksMap[subj] = score
                                totalObtained += score
                                if (score < selectedExam.passMarks) {
                                    isPassed = false
                                }
                            }

                            val percentage = if (fullMarks > 0) (totalObtained / fullMarks) * 100.0 else 0.0
                            val grade = ExamRepository.calculateGrade(percentage, isPassed)

                            newResults.add(
                                StudentResult(
                                    id = "res_${selectedExam.id}_${student.id}",
                                    examId = selectedExam.id,
                                    studentId = student.id,
                                    studentName = student.name,
                                    studentRoll = student.roll,
                                    jamatClass = student.jamatClass,
                                    subjectMarks = subjectMarksMap,
                                    totalObtainedMarks = totalObtained,
                                    totalFullMarks = fullMarks,
                                    percentage = percentage,
                                    grade = grade,
                                    status = if (isPassed) "উত্তীর্ণ" else "অনুত্তীর্ণ",
                                    remarks = if (isPassed) "উত্তীর্ণ, মাশাআল্লাহ!" else "পুনরায় বিষয়ভিত্তিক চেষ্টা প্রয়োজন"
                                )
                            )
                        }

                        // Calculate class positions based on total marks
                        val sortedResults = newResults.sortedByDescending { it.totalObtainedMarks }
                        val finalizedResults = sortedResults.mapIndexed { index, res ->
                            res.copy(position = index + 1)
                        }

                        onSaveResults(finalizedResults)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("রেজাল্ট সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(classStudents) { student ->
                    val marksMap = studentMarksState[student.id]
                    StudentMarkEntryCard(
                        student = student,
                        subjects = selectedExam.subjects,
                        marksMap = marksMap,
                        passMarks = selectedExam.passMarks,
                        onMarkChange = { subject, value ->
                            if (marksMap != null) {
                                marksMap[subject] = value
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentMarkEntryCard(
    student: Student,
    subjects: List<String>,
    marksMap: MutableMap<String, String>?,
    passMarks: Int,
    onMarkChange: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Student Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF2563EB),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.roll.ifBlank { "১" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "পিতা: ${student.fatherName.ifBlank { "তথ্য নেই" }}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    color = if (student.gender == "ছাত্রী") Color(0xFFFCE7F3) else Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = student.gender,
                        fontSize = 11.sp,
                        color = if (student.gender == "ছাত্রী") Color(0xFFBE185D) else Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calculated Totals
            var totalMarks = 0.0
            var allFilled = true
            var isFail = false

            subjects.forEach { subj ->
                val v = marksMap?.get(subj)
                val d = v?.toDoubleOrNull()
                if (d != null) {
                    totalMarks += d
                    if (d < passMarks) isFail = true
                } else {
                    allFilled = false
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট নম্বর: ${totalMarks.toInt()} / ${subjects.size * 100}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                if (allFilled) {
                    val p = (totalMarks / (subjects.size * 100)) * 100.0
                    val g = ExamRepository.calculateGrade(p, !isFail)
                    Text(
                        text = "গ্রেড: $g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFail) Color.Red else Color(0xFF059669)
                    )
                } else {
                    Text(
                        text = "ইনপুট দিন...",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Input Fields Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                subjects.chunked(2).forEach { rowSubjs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSubjs.forEach { subj ->
                            val currentVal = marksMap?.get(subj) ?: ""
                            OutlinedTextField(
                                value = currentVal,
                                onValueChange = { newVal ->
                                    if (newVal.isEmpty() || (newVal.toIntOrNull() != null && newVal.toInt() <= 100)) {
                                        onMarkChange(subj, newVal)
                                    }
                                },
                                label = { Text(subj, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        if (rowSubjs.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ------------------- TAB 3: RESULT SHEET -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultSheetTab(
    exams: List<Exam>,
    classList: List<String>,
    allStudents: List<Student>,
    allResults: List<StudentResult>,
    initialExam: Exam?,
    initialClass: String,
    onViewStudentCard: (StudentResult) -> Unit
) {
    if (exams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("প্রথমে একটি পরীক্ষা যোগ করুন।", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
        return
    }

    var selectedExam by remember { mutableStateOf(initialExam ?: exams.first()) }
    var selectedClass by remember { mutableStateOf(if (initialClass.isNotBlank()) initialClass else (selectedExam.targetClasses.firstOrNull() ?: classList.firstOrNull() ?: "শ্রেণি প্লে")) }

    var examDropdownExpanded by remember { mutableStateOf(false) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    // Filter results for active exam and class
    val classResults = remember(selectedExam, selectedClass, allResults) {
        allResults.filter { it.examId == selectedExam.id && it.jamatClass == selectedClass }
            .sortedBy { it.position }
    }

    val totalExamStudents = remember(selectedClass, allStudents) {
        allStudents.filter { it.jamatClass == selectedClass }.size
    }

    val passedCount = classResults.count { it.status == "উত্তীর্ণ" }
    val failedCount = classResults.count { it.status == "অনুত্তীর্ণ" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Exam & Class Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = examDropdownExpanded,
                onExpandedChange = { examDropdownExpanded = !examDropdownExpanded },
                modifier = Modifier.weight(1.2f)
            ) {
                OutlinedTextField(
                    value = selectedExam.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("পরীক্ষা") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = examDropdownExpanded,
                    onDismissRequest = { examDropdownExpanded = false }
                ) {
                    exams.forEach { ex ->
                        DropdownMenuItem(
                            text = { Text(ex.title) },
                            onClick = {
                                selectedExam = ex
                                if (ex.targetClasses.isNotEmpty()) {
                                    selectedClass = ex.targetClasses.first()
                                }
                                examDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = classDropdownExpanded,
                onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedClass,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("শ্রেণী") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = classDropdownExpanded,
                    onDismissRequest = { classDropdownExpanded = false }
                ) {
                    classList.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls) },
                            onClick = {
                                selectedClass = cls
                                classDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metrics Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF024BB0))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${selectedExam.title} - $selectedClass",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "মেধাক্রম অনুযায়ী প্রস্তুতকৃত ফলাফল তালিকা",
                        fontSize = 12.sp,
                        color = Color(0xFF93C5FD)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${classResults.size}/$totalExamStudents", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("এন্ট্রি", fontSize = 10.sp, color = Color(0xFFBFDBFE))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$passedCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        Text("পাস", fontSize = 10.sp, color = Color(0xFFBFDBFE))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$failedCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                        Text("ফেল", fontSize = 10.sp, color = Color(0xFFBFDBFE))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (classResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "এই শ্রেণীর জন্য এখনও কোনো রেজাল্ট এনট্রি করা হয়নি।",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "'রেজাল্ট এন্ট্রি' ট্যাবে গিয়ে নম্বর প্রদান করুন।",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(classResults) { res ->
                    ResultRowCard(
                        result = res,
                        onViewCard = { onViewStudentCard(res) }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRowCard(
    result: StudentResult,
    onViewCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewCard() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Position Badge
                Surface(
                    color = when (result.position) {
                        1 -> Color(0xFFFEF3C7) // Gold
                        2 -> Color(0xFFE2E8F0) // Silver
                        3 -> Color(0xFFFFEDD5) // Bronze
                        else -> Color(0xFFF1F5F9)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${result.position}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (result.position) {
                                1 -> Color(0xFFB45309)
                                2 -> Color(0xFF475569)
                                3 -> Color(0xFFC2410C)
                                else -> Color(0xFF334155)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "${result.studentRoll}. ${result.studentName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "প্রাপ্ত নম্বর: ${result.totalObtainedMarks.toInt()} / ${result.totalFullMarks.toInt()} (${String.format("%.1f", result.percentage)}%)",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (result.status == "উত্তীর্ণ") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = result.grade,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.status == "উত্তীর্ণ") Color(0xFF15803D) else Color(0xFFB91C1C),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "মার্কশীট দেখুন →",
                    fontSize = 11.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ------------------- DIALOGS -------------------

@Composable
fun AddExamDialog(
    classList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Exam) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("১৫ মার্চ, ২০২৬") }
    var subjectsText by remember { mutableStateOf("কুরআন ও তাজবীদ, হাদীস শরীফ, আরবী, বাংলা, ইংরেজি, গণিত") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নতুন পরীক্ষা যোগ করুন",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পরীক্ষার নাম (যেমন: ১ম সাময়িক পরীক্ষা ২০২৬)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("পরীক্ষার তারিখ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = subjectsText,
                    onValueChange = { subjectsText = it },
                    label = { Text("বিষয়সমূহ (কমা দিয়ে লিখুন)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("বাতিল") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val subjects = subjectsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                onConfirm(
                                    Exam(
                                        title = title,
                                        date = date,
                                        subjects = if (subjects.isNotEmpty()) subjects else listOf("কুরআন", "বাংলা", "গণিত"),
                                        targetClasses = classList
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("তৈরি করুন")
                    }
                }
            }
        }
    }
}

// Student Marksheet / Report Card Dialog
@Composable
fun StudentMarksheetDialog(
    result: StudentResult,
    exam: Exam?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF024BB0), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("দারুসসালাম মডেল মাদ্রাসা", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("একাডেমিক ট্রান্সক্রিপ্ট / নম্বরপত্র", fontSize = 12.sp, color = Color(0xFFBFDBFE))
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color(0xFF2563EB), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            text = exam?.title ?: "সাময়িক পরীক্ষা ২০২৬",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                // Student Info Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("শিক্ষার্থীর নাম: ${result.studentName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                            Text("রোল: ${result.studentRoll}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                        }
                        Text("শ্রেণী: ${result.jamatClass}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                Text("বিষয়ভিত্তিক প্রাপ্ত নম্বর:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))

                // Subject Marks Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("বিষয়", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                            Text("পূর্ণমান", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("প্রাপ্ত", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        result.subjectMarks.forEach { (subj, score) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(subj, fontSize = 12.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1.5f))
                                Text("১০০", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("${score.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }

                // Summary Result Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মোট প্রাপ্তি: ${result.totalObtainedMarks.toInt()} / ${result.totalFullMarks.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                        Text("মেধাক্রম: ${result.position}ম স্থান", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("গ্রেড: ${result.grade}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF059669))
                        Text(result.status, fontSize = 11.sp, color = Color(0xFF166534))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("বন্ধ করুন")
                    }
                    Button(
                        onClick = {
                            Toast.makeText(context, "মার্কশীট প্রিন্ট / পিডিএফ ডাউনলোড কমান্ড পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রিন্ট / ডাউনলোড")
                    }
                }
            }
        }
    }
}

// ------------------- TAB 4: ADMIT CARD -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmitCardTab(
    exams: List<Exam>,
    classList: List<String>,
    allStudents: List<Student>,
    initialExam: Exam?,
    initialClass: String,
    onViewAdmitCard: (Student, Exam?) -> Unit
) {
    val context = LocalContext.current
    var selectedExam by remember(exams, initialExam) {
        mutableStateOf(initialExam ?: exams.firstOrNull())
    }
    var selectedClass by remember(classList, initialClass) {
        mutableStateOf(if (initialClass.isNotBlank()) initialClass else classList.firstOrNull() ?: "")
    }
    var searchQuery by remember { mutableStateOf("") }

    var examExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    // Filter students
    val classStudents = remember(allStudents, selectedClass, searchQuery) {
        allStudents.filter { student ->
            (selectedClass.isBlank() || student.jamatClass == selectedClass) &&
            (searchQuery.isBlank() || student.name.contains(searchQuery, ignoreCase = true) || student.roll.contains(searchQuery))
        }.sortedBy { it.roll.toIntOrNull() ?: 999 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Exam Dropdown
                    ExposedDropdownMenuBox(
                        expanded = examExpanded,
                        onExpandedChange = { examExpanded = !examExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedExam?.title ?: "পরীক্ষা নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("পরীক্ষা", fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = examExpanded,
                            onDismissRequest = { examExpanded = false }
                        ) {
                            exams.forEach { exam ->
                                DropdownMenuItem(
                                    text = { Text(exam.title) },
                                    onClick = {
                                        selectedExam = exam
                                        examExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Class Dropdown
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (selectedClass.isBlank()) "সকল শ্রেণী" else selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("শ্রেণী", fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("সকল শ্রেণী") },
                                onClick = {
                                    selectedClass = ""
                                    classExpanded = false
                                }
                            )
                            classList.forEach { cls ->
                                DropdownMenuItem(
                                    text = { Text(cls) },
                                    onClick = {
                                        selectedClass = cls
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("শিক্ষার্থীর নাম বা রোল খুঁজুন...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (classStudents.isEmpty()) {
                                Toast.makeText(context, "কোনো শিক্ষার্থী পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "${classStudents.size} জন শিক্ষার্থীর সকল প্রবেশপত্র প্রিন্ট কমান্ড সফলভাবে পাঠানো হয়েছে", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সকলের প্রিন্ট", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (classStudents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো শিক্ষার্থী পাওয়া যায়নি। অনুগ্রহ করে শ্রেণী নির্বাচন করুন।",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(classStudents) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFF3E8FF),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = student.roll.ifBlank { "১" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF7C3AED)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "শ্রেণী: ${student.jamatClass} • পিতা: ${student.fatherName.ifBlank { "তথ্য নেই" }}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Button(
                                onClick = { onViewAdmitCard(student, selectedExam) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("প্রবেশপত্র দেখুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------- ADMIT CARD DIALOG (NO PHOTO, DUAL SIGNATURES) -------------------
@Composable
fun AdmitCardDialog(
    student: Student,
    exam: Exam?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Document Border Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF1E3A8A), RoundedCornerShape(12.dp))
                        .background(Color(0xFFFCFDFD))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Header
                        Text(
                            text = "দারুচ্ছালাম মাদরাসা",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "ডাকঘর: বাগমারা, থানা: রাজবাড়ী সদর, জেলা: রাজবাড়ী",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color(0xFF1E3A8A),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "প্রবেশপত্র (ADMIT CARD)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exam?.title ?: "১ম সাময়িক পরীক্ষা - ২০২৬",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "শিক্ষাবর্ষ: ${exam?.year ?: "২০২৬"} • পরীক্ষা কেন্দ্র: মাদরাসা ক্যাম্পাস",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Student Info Grid (EXPLICITLY NO PHOTO AS REQUESTED)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AdmitInfoRow(label1 = "শিক্ষার্থীর নাম:", val1 = student.name, label2 = "রোল নম্বর:", val2 = student.roll.ifBlank { "১" })
                            AdmitInfoRow(label1 = "জামাআত / শ্রেণী:", val1 = student.jamatClass, label2 = "লিঙ্গ / বিভাগ:", val2 = student.gender)
                            AdmitInfoRow(label1 = "পিতার নাম:", val1 = student.fatherName.ifBlank { "তথ্য নেই" }, label2 = "মাতার নাম:", val2 = student.motherName.ifBlank { "তথ্য নেই" })
                            AdmitInfoRow(label1 = "আইডি নম্বর:", val1 = "DSM-${student.id.takeLast(5).uppercase()}", label2 = "যোগাযোগ:", val2 = student.guardianContact.ifBlank { "তথ্য নেই" })
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subject Table Header
                        Text(
                            text = "পরীক্ষার বিষয়সমূহ:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val subjects = exam?.subjects ?: listOf("কুরআন ও তাজবীদ", "হাদীস শরীফ", "আরবী", "বাংলা", "ইংরেজি", "গণিত")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE2E8F0))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("নং", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(0.3f))
                                Text("বিষয়", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(1.5f))
                                Text("পূর্ণমান", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                            }
                            HorizontalDivider(color = Color(0xFFCBD5E1))
                            subjects.forEachIndexed { idx, subj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp, horizontal = 8.dp)
                                ) {
                                    Text("${idx + 1}", fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.3f))
                                    Text(subj, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.5f))
                                    Text("${exam?.subjectTotalMarks ?: 100}", fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                                }
                                if (idx < subjects.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rules Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), shape = RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("পরীক্ষার্থীদের জন্য বিশেষ নির্দেশাবলী:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text("১. পরীক্ষা শুরুর ১৫ মিনিট পূর্বে আসন গ্রহণ করতে হবে।", fontSize = 10.sp, color = Color(0xFF7F1D1D))
                            Text("২. পরীক্ষা কক্ষে প্রবেশপত্র প্রদর্শন বাধ্যতামূলক।", fontSize = 10.sp, color = Color(0xFF7F1D1D))
                            Text("৩. কোনো প্রকার অসদুপায় অবলম্বন সম্পূর্ণ নিষিদ্ধ।", fontSize = 10.sp, color = Color(0xFF7F1D1D))
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // DUAL SIGNATURE SECTION (CRITICAL REQUIREMENT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Left Signature: Assistant Teacher (সহকারী শিক্ষক)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("-----------------------------", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "সহকারী শিক্ষক",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text("দারুচ্ছালাম মাদরাসা", fontSize = 10.sp, color = Color(0xFF64748B))
                            }

                            // Right Signature: Muhtamim (মুহতামিম)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Seal",
                                    tint = Color(0xFF1E3A8A),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text("-----------------------------", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "মুহতামিম",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text("দারুচ্ছালাম মাদরাসা", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বন্ধ করুন")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "${student.name}-এর প্রবেশপত্র প্রিন্ট / ডাউনলোড সফল হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রিন্ট / ডাউনলোড")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdmitInfoRow(label1: String, val1: String, label2: String, val2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label1, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = val1, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label2, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = val2, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
    }
}
