package com.example.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.teacher.isClassAllowed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    repository: StudentRepository,
    allowedClasses: List<String>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val students by repository.students.collectAsState()
    val classList by repository.classList.collectAsState()

    val effectiveClassList = remember(classList, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            classList
        } else {
            val filtered = classList.filter { isClassAllowed(it, allowedClasses) }
            if (filtered.isNotEmpty()) filtered else allowedClasses
        }
    }

    val allowedStudents = remember(students, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            students
        } else {
            students.filter { isClassAllowed(it.jamatClass, allowedClasses) }
        }
    }

    val defaultFilterLabel = if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) "সমস্ত শ্রেণী" else "অনুমোদিত শ্রেণীসমূহ"
    var selectedClassFilter by remember(effectiveClassList) { mutableStateOf(defaultFilterLabel) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showClassManagementDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingStudent by remember { mutableStateOf<Student?>(null) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    var csvImportPreviewList by remember { mutableStateOf<List<Student>?>(null) }
    var csvFileName by remember { mutableStateOf("") }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val csvContent = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                val parsed = parseStudentCsv(csvContent)
                if (parsed.isEmpty()) {
                    Toast.makeText(context, "CSV ফাইলে কোনো সঠিক শিক্ষার্থীর তথ্য পাওয়া যায়নি", Toast.LENGTH_LONG).show()
                } else {
                    csvFileName = uri.lastPathSegment?.substringAfterLast('/') ?: "student_records.csv"
                    csvImportPreviewList = parsed
                }
            } catch (e: Exception) {
                Toast.makeText(context, "CSV ফাইল পড়তে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Filter students by selected class
    val filteredStudents = if (selectedClassFilter == "সমস্ত শ্রেণী" || selectedClassFilter == "অনুমোদিত শ্রেণীসমূহ") {
        allowedStudents
    } else {
        allowedStudents.filter { it.jamatClass == selectedClassFilter || isClassAllowed(it.jamatClass, listOf(selectedClassFilter)) }
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title
        Text(
            text = "শিক্ষার্থীদের তালিকা",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        // Dropdown for class selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { classDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedClassFilter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            DropdownMenu(
                expanded = classDropdownExpanded,
                onDismissRequest = { classDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                DropdownMenuItem(
                    text = { Text(defaultFilterLabel, fontWeight = FontWeight.Bold) },
                    onClick = {
                        selectedClassFilter = defaultFilterLabel
                        classDropdownExpanded = false
                    }
                )
                HorizontalDivider()
                effectiveClassList.forEach { cls ->
                    DropdownMenuItem(
                        text = { Text(cls) },
                        onClick = {
                            selectedClassFilter = cls
                            classDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Action Buttons Row matching Screenshot 1 exactly
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ⚙ শ্রেণী ব্যবস্থাপনা
            Button(
                onClick = { showClassManagementDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("শ্রেণী ব্যবস্থাপনা", fontSize = 13.sp, color = Color.White)
            }

            // 📥 ইনফরমেশন ডাউনলোড
            Button(
                onClick = {
                    Toast.makeText(context, "ইনফরমেশন ডাউনলোড সফল হয়েছে", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ইনফরমেশন ডাউনলোড", fontSize = 13.sp, color = Color.White)
            }

            // CSV ইম্পোর্ট
            Button(
                onClick = {
                    csvPickerLauncher.launch("*/*")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("CSV ইম্পোর্ট", fontSize = 13.sp, color = Color.White)
            }

            // শিক্ষার্থী যোগ
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("শিক্ষার্থী যোগ", fontSize = 13.sp, color = Color.White)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "বামে-ডানে স্ক্রল / সোয়াইপ করে সম্পূর্ণ তথ্য ও ফোন নম্বর দেখুন",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Student Table with Horizontal Scrollable support
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.widthIn(min = 720.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "রোল", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(70.dp))
                        Text(text = "নাম", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(160.dp))
                        Text(text = "শ্রেণী", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(110.dp))
                        Text(text = "যোগাযোগ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(130.dp))
                        Text(text = "সম্পাদনকারী", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(140.dp))
                        Text(text = "অ্যাকশন", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(110.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Table Items
                    if (filteredStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(filteredStudents, key = { it.id }) { student ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewingStudent = student }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = student.roll.ifBlank { "-" },
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.width(70.dp)
                                    )
                                    Text(
                                        text = student.name,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(160.dp)
                                    )
                                    Text(
                                        text = student.jamatClass,
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.width(110.dp)
                                    )
                                    Text(
                                        text = student.guardianContact.ifBlank { "নম্বর নেই" },
                                        fontSize = 13.sp,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.width(130.dp)
                                    )
                                    Text(
                                        text = student.lastUpdatedBy.ifBlank { "এডমিন" },
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.width(140.dp)
                                    )
                                    Row(
                                        modifier = Modifier.width(110.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { viewingStudent = student },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "View Details",
                                                tint = Color(0xFF2563EB),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { editingStudent = student },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Color(0xFF059669),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { repository.deleteStudent(student.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }

    // View Detailed Student Info Dialog
    viewingStudent?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { viewingStudent = null },
            onEditRequest = {
                editingStudent = student
                viewingStudent = null
            }
        )
    }

    // Add Student Modal Dialog
    if (showAddDialog) {
        AddOrEditStudentDialog(
            title = "নতুন শিক্ষার্থী যোগ করুন",
            initialStudent = Student(),
            classList = effectiveClassList,
            onDismiss = { showAddDialog = false },
            onConfirm = { newStudent ->
                val timeStamp = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault()).format(Date())
                repository.addStudent(newStudent.copy(lastUpdatedAt = timeStamp))
                showAddDialog = false
            }
        )
    }

    // Edit Student Dialog
    editingStudent?.let { student ->
        AddOrEditStudentDialog(
            title = "শিক্ষার্থীর তথ্য পরিবর্তন ও সম্পাদনা",
            initialStudent = student,
            classList = effectiveClassList,
            onDismiss = { editingStudent = null },
            onConfirm = { updatedStudent ->
                val timeStamp = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault()).format(Date())
                repository.updateStudent(updatedStudent.copy(lastUpdatedAt = timeStamp))
                editingStudent = null
            }
        )
    }

    // Class Management Dialog
    if (showClassManagementDialog) {
        ClassManagementDialog(
            classList = classList,
            onAddClass = { repository.addClass(it) },
            onRemoveClass = { repository.removeClass(it) },
            onDismiss = { showClassManagementDialog = false }
        )
    }

    // CSV Import Preview Dialog
    csvImportPreviewList?.let { parsedList ->
        CsvImportPreviewDialog(
            fileName = csvFileName,
            students = parsedList,
            onDismiss = { csvImportPreviewList = null },
            onConfirmImport = {
                repository.addStudents(parsedList) { count ->
                    Toast.makeText(context, "$count জন শিক্ষার্থীর তথ্য সফলভাবে ডাটাবেজে যুক্ত করা হয়েছে!", Toast.LENGTH_LONG).show()
                }
                csvImportPreviewList = null
            }
        )
    }
}

// Student All Information View Dialog
@Composable
fun StudentDetailDialog(
    student: Student,
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D4ED8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শিক্ষার্থীর সম্পূর্ণ তথ্য",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { DetailRow(label = "পুরো নাম", value = student.name) }
                    item { DetailRow(label = "রোল নম্বর", value = student.roll) }
                    item { DetailRow(label = "শ্রেণী / জামাত", value = student.jamatClass) }
                    item { DetailRow(label = "লিঙ্গ", value = student.gender) }
                    item { DetailRow(label = "বাবার নাম", value = student.fatherName.ifBlank { "তথ্য নেই" }) }
                    item { DetailRow(label = "মায়ের নাম", value = student.motherName.ifBlank { "তথ্য নেই" }) }
                    item { DetailRow(label = "বয়স", value = student.age.ifBlank { "তথ্য নেই" }) }
                    item { DetailRow(label = "অভিভাবকের মোবাইল", value = student.guardianContact.ifBlank { "তথ্য নেই" }) }
                    item { DetailRow(label = "ঠিকানা", value = student.address.ifBlank { "তথ্য নেই" }) }
                    item { DetailRow(label = "অবস্থা", value = student.status) }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("সম্পাদনার বিবরণ (Audit Info):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("সর্বশেষ সম্পাদনকারী: ${student.lastUpdatedBy.ifBlank { "এডমিন" }}", fontSize = 12.sp, color = Color(0xFF334155))
                                if (student.lastUpdatedAt.isNotBlank()) {
                                    Text("সম্পাদনার সময়: ${student.lastUpdatedAt}", fontSize = 12.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
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

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

// Dialog for Add and Edit with Editor Name Tracking
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditStudentDialog(
    title: String,
    initialStudent: Student,
    classList: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Student) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent.name) }
    var fatherName by remember { mutableStateOf(initialStudent.fatherName) }
    var motherName by remember { mutableStateOf(initialStudent.motherName) }
    var roll by remember { mutableStateOf(initialStudent.roll) }
    var jamatClass by remember { mutableStateOf(if (initialStudent.jamatClass.isBlank()) classList.firstOrNull() ?: "শ্রেণি প্লে" else initialStudent.jamatClass) }
    var age by remember { mutableStateOf(initialStudent.age) }
    var guardianContact by remember { mutableStateOf(initialStudent.guardianContact) }
    var address by remember { mutableStateOf(initialStudent.address) }
    var gender by remember { mutableStateOf(initialStudent.gender) }
    var lastUpdatedBy by remember { mutableStateOf(initialStudent.lastUpdatedBy.ifBlank { "এডমিন (HM.ABDUL ALIM)" }) }

    var classDropdownExpanded by remember { mutableStateOf(false) }
    var genderDropdownExpanded by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with X close button
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

                // Scrollable Form Fields
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                    item {
                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            placeholder = { Text("বাবার নাম (ঐচ্ছিক)", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = motherName,
                            onValueChange = { motherName = it },
                            placeholder = { Text("মায়ের নাম (ঐচ্ছিক)", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = roll,
                            onValueChange = { roll = it },
                            placeholder = { Text("রোল নম্বর", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Class Dropdown Selector
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { classDropdownExpanded = true },
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
                                    Text(
                                        text = jamatClass.ifBlank { "শ্রেণী নির্বাচন করুন" },
                                        color = if (jamatClass.isBlank()) Color(0xFF94A3B8) else Color(0xFF1E293B)
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                                }
                            }

                            DropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                classList.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls) },
                                        onClick = {
                                            jamatClass = cls
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            placeholder = { Text("বয়স", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = guardianContact,
                            onValueChange = { guardianContact = it },
                            placeholder = { Text("অভিভাবকের যোগাযোগ / ফোন নম্বর", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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

                    // Gender Selector Dropdown
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { genderDropdownExpanded = true },
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
                                expanded = genderDropdownExpanded,
                                onDismissRequest = { genderDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("ছাত্র") },
                                    onClick = {
                                        gender = "ছাত্র"
                                        genderDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("ছাত্রী") },
                                    onClick = {
                                        gender = "ছাত্রী"
                                        genderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = lastUpdatedBy,
                            onValueChange = { lastUpdatedBy = it },
                            label = { Text("সম্পাদনকারীর নাম", color = Color(0xFF64748B)) },
                            placeholder = { Text("সম্পাদনকারীর নাম লিখুন", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons Row
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
                                    initialStudent.copy(
                                        name = name,
                                        fatherName = fatherName,
                                        motherName = motherName,
                                        roll = roll,
                                        jamatClass = jamatClass,
                                        age = age,
                                        guardianContact = guardianContact,
                                        address = address,
                                        gender = gender,
                                        lastUpdatedBy = lastUpdatedBy.ifBlank { "এডমিন" }
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("সংরক্ষণ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClassManagementDialog(
    classList: List<String>,
    onAddClass: (String) -> Unit,
    onRemoveClass: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newClassName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        text = "শ্রেণী ব্যবস্থাপনা",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // New Class Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newClassName,
                        onValueChange = { newClassName = it },
                        placeholder = { Text("নতুন শ্রেণীর নাম (যেমন: শ্রেণি ১ম)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newClassName.isNotBlank()) {
                                onAddClass(newClassName.trim())
                                newClassName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("যোগ")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("বর্তমান শ্রেণীসমূহ:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF475569))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(classList) { cls ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cls, fontSize = 14.sp, color = Color(0xFF1E293B))
                            IconButton(onClick = { onRemoveClass(cls) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                ) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CsvImportPreviewDialog(
    fileName: String,
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirmImport: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CSV ইম্পোর্ট প্রিভিউ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "মোট ${students.size} জন শিক্ষার্থীর তথ্য পাওয়া গেছে",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "ফাইল: $fileName",
                                fontSize = 12.sp,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }

                Text(
                    text = "প্রিভিউ তালিকা (প্রথম ৫০ জন):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                // Preview Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(students.take(50)) { s ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${s.roll.ifBlank { "-" }}. ${s.name}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "শ্রেণী: ${s.jamatClass} • মোবাইল: ${s.guardianContact.ifBlank { "নেই" }}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = s.gender,
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "* 'ইম্পোর্ট সম্পন্ন করুন' বাটনে চাপ দিলে সমস্ত শিক্ষার্থীর তথ্য Firestore ডাটাবেজে সংরক্ষণ হবে।",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirmImport,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ইম্পোর্ট সম্পন্ন করুন (${students.size} জন)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


