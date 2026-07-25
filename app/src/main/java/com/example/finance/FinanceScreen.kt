package com.example.finance

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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.settings.SettingsManager
import com.example.student.Student
import com.example.student.StudentRepository
import com.example.teacher.TeacherRepository
import com.example.teacher.isClassAllowed
import java.text.SimpleDateFormat
import java.util.*

data class FeeRecord(
    val id: String = UUID.randomUUID().toString(),
    val studentId: String,
    val studentName: String,
    val className: String,
    val rollNumber: Int,
    val category: String,
    val amount: Double,
    val status: String, // "পরিশোধিত" or "বকেয়া"
    val date: String,
    val receiptNumber: Int,
    val bookNumber: Int
)

data class ExpenseVoucher(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // আপ্যায়ন, বিদ্যুৎ বিল, মেরামতি, কিতাব ক্রয়, অন্যান্য
    val amount: Double,
    val paidTo: String,
    val date: String
)

data class SalaryRecord(
    val id: String = UUID.randomUUID().toString(),
    val teacherName: String,
    val month: String,
    val baseSalary: Double,
    val bonus: Double,
    val totalPaid: Double,
    val status: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    studentRepository: StudentRepository = remember { StudentRepository() },
    teacherRepository: TeacherRepository = remember { TeacherRepository() },
    allowedClasses: List<String>? = null,
    initialTab: Int = 0, // 0: Fees, 1: Expenses, 2: Salary
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val generalSettings by SettingsManager.instance.generalSettings.collectAsState()
    val rawStudents by studentRepository.students.collectAsState()
    val teachers by teacherRepository.teachers.collectAsState()

    val students = remember(rawStudents, allowedClasses) {
        if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
            rawStudents
        } else {
            rawStudents.filter { isClassAllowed(it.jamatClass, allowedClasses) }
        }
    }

    var selectedTab by remember { mutableStateOf(initialTab) }

    // State for Fee Records
    var feeList by remember {
        mutableStateOf(
            listOf(
                FeeRecord("1", "1", "আব্দুল্লাহ আল মামুন", "হেফজ", 1, "মাসিক বেতন", 1500.0, "পরিশোধিত", "2026-07-20", 12, 1),
                FeeRecord("2", "2", "মোহাম্মদ হাসান", "মিশকাত", 2, "ভর্তি ফি", 3000.0, "পরিশোধিত", "2026-07-22", 13, 1),
                FeeRecord("3", "3", "আবু বকর সিদ্দীক", "শরহে বেকায়া", 3, "পরীক্ষার ফি", 800.0, "বকেয়া", "2026-07-24", 0, 1)
            )
        )
    }

    // State for Expenses
    var expenseList by remember {
        mutableStateOf(
            listOf(
                ExpenseVoucher("e1", "বিদ্যুৎ বিল মে ২০২৬", "বিদ্যুৎ বিল", 4200.0, "ডেসকো", "2026-07-15"),
                ExpenseVoucher("e2", "মেহমান আপ্যায়ন", "আপ্যায়ন", 1250.0, "আল মদিনা হোটেল", "2026-07-18"),
                ExpenseVoucher("e3", "মাদরাসা মেরামত ও রং", "মেরামতি", 8500.0, "রফিক মিস্ত্রী", "2026-07-22")
            )
        )
    }

    // State for Salaries
    var salaryList by remember {
        mutableStateOf(
            listOf(
                SalaryRecord("s1", "মুফতি সামছুল হক", "জুলাই ২০২৬", 25000.0, 2000.0, 27000.0, "পরিশোধিত", "2026-07-05"),
                SalaryRecord("s2", "মাওলানা জহিরুল ইসলাম", "জুলাই ২০২৬", 18000.0, 1000.0, 19000.0, "পরিশোধিত", "2026-07-05")
            )
        )
    }

    // Auto Receipt Book Counters
    var currentReceiptNum by remember { mutableStateOf(14) }
    var currentBookNum by remember { mutableStateOf(1) }

    var showAddFeeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedFeeForReceipt by remember { mutableStateOf<FeeRecord?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1D4ED8),
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ফি আদায় ও বকেয়া", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("ব্যয় ভাউচার", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("শিক্ষক পে-রোল", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        when (selectedTab) {
            0 -> {
                // FEES MANAGEMENT SECTION
                val totalCollected = feeList.filter { it.status == "পরিশোধিত" }.sumOf { it.amount }
                val totalDues = feeList.filter { it.status == "বকেয়া" }.sumOf { it.amount }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "মোট আদায়কৃত ফি",
                        value = "৳ ${totalCollected.toInt()}",
                        bgColor = Color(0xFFDCFCE7),
                        textColor = Color(0xFF15803D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "মোট বকেয়া ফি",
                        value = "৳ ${totalDues.toInt()}",
                        bgColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFB91C1C),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ফি পরিশোধের রেকর্ডসমূহ:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                    Button(
                        onClick = { showAddFeeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন ফি সংগ্রহ")
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(feeList, key = { it.id }) { fee ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${fee.studentName} (${fee.className} - রোল: ${fee.rollNumber})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "খাত: ${fee.category} • তারিখ: ${fee.date}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    if (fee.status == "পরিশোধিত") {
                                        Text(
                                            text = "রশিদ বই: ${fee.bookNumber} • রসিদ নং: #${fee.receiptNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2563EB)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳ ${fee.amount.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (fee.status == "পরিশোধিত") {
                                        OutlinedButton(
                                            onClick = { selectedFeeForReceipt = fee },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("রসিদ প্রিন্ট", fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                currentReceiptNum++
                                                if (currentReceiptNum > 50) {
                                                    currentBookNum++
                                                    currentReceiptNum = 1
                                                }
                                                val updated = fee.copy(
                                                    status = "পরিশোধিত",
                                                    receiptNumber = currentReceiptNum,
                                                    bookNumber = currentBookNum
                                                )
                                                feeList = feeList.map { if (it.id == fee.id) updated else it }
                                                Toast.makeText(context, "ফি গ্রহণ সফল হয়েছে! রসিদ জেনারেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text("আদায় করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // EXPENSE VOUCHER SECTION
                val totalExpense = expenseList.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("চলতি মাসের মোট খরচ:", fontSize = 12.sp, color = Color(0xFFC2410C))
                            Text("৳ ${totalExpense.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                        }
                        Button(
                            onClick = { showAddExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("নতুন খরচ ভাউচার")
                        }
                    }
                }

                Text("ভাউচার তালিকা:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expenseList, key = { it.id }) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    Text("খাত: ${exp.category} • গ্রহণকারী: ${exp.paidTo}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text("তারিখ: ${exp.date}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Text("৳ ${exp.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            2 -> {
                // TEACHER SALARY SECTION
                Text("শিক্ষক বেতন পে-রোল (জুলাই ২০২৬):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(teachers, key = { it.id }) { teacher ->
                        val baseSalary = if (teacher.isSuperAdmin) 25000.0 else 18000.0
                        val currentRecord = salaryList.firstOrNull { it.teacherName == teacher.name }
                        val isPaid = currentRecord != null

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(teacher.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                    Text("${teacher.designation} • বিষয়: ${teacher.subject.ifBlank { "সাধারণ" }}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text("মূল বেতন: ৳ ${baseSalary.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                                }

                                if (isPaid) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(12.dp)) {
                                        Text("পরিশোধিত", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val newRec = SalaryRecord(
                                                teacherName = teacher.name,
                                                month = "জুলাই ২০২৬",
                                                baseSalary = baseSalary,
                                                bonus = 0.0,
                                                totalPaid = baseSalary,
                                                status = "পরিশোধিত",
                                                date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                                            )
                                            salaryList = salaryList + newRec
                                            Toast.makeText(context, "${teacher.name}-এর বেতন সফলভাবে পরিশোধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("বেতন দিন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD FEE DIALOG
    if (showAddFeeDialog) {
        var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
        var category by remember { mutableStateOf("মাসিক বেতন") }
        var amountText by remember { mutableStateOf("1500") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddFeeDialog = false },
            title = { Text("নতুন ফি এন্ট্রি", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("শিক্ষার্থী নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.let { "${it.name} (${it.jamatClass})" } ?: "শিক্ষার্থী নেই",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            students.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text("${st.name} (${st.jamatClass} - রোল: ${st.roll})") },
                                    onClick = {
                                        selectedStudent = st
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("ফি এর খাত (যেমন: মাসিক বেতন)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("টাকার পরিমাণ (৳)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val st = selectedStudent
                        if (st != null) {
                            currentReceiptNum++
                            val newFee = FeeRecord(
                                studentId = st.id,
                                studentName = st.name,
                                className = st.jamatClass,
                                rollNumber = st.roll.toIntOrNull() ?: 1,
                                category = category,
                                amount = amountText.toDoubleOrNull() ?: 1000.0,
                                status = "পরিশোধিত",
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date()),
                                receiptNumber = currentReceiptNum,
                                bookNumber = currentBookNum
                            )
                            feeList = listOf(newFee) + feeList
                            showAddFeeDialog = false
                            Toast.makeText(context, "ফি সংগ্রহ সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                ) {
                    Text("সংগ্রহ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFeeDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // ADD EXPENSE DIALOG
    if (showAddExpenseDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("আপ্যায়ন") }
        var amountText by remember { mutableStateOf("") }
        var paidTo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("নতুন খরচ ভাউচার", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("খরচের বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("খাত (বিদ্যুৎ বিল/আপ্যায়ন/ইত্যাদি)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("টাকার পরিমাণ (৳)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paidTo,
                        onValueChange = { paidTo = it },
                        label = { Text("যাকে পরিশোধ করা হলো") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val exp = ExpenseVoucher(
                                title = title,
                                category = category,
                                amount = amountText.toDoubleOrNull() ?: 0.0,
                                paidTo = paidTo,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                            )
                            expenseList = listOf(exp) + expenseList
                            showAddExpenseDialog = false
                            Toast.makeText(context, "খরচের ভাউচার সেভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = { TextButton(onClick = { showAddExpenseDialog = false }) { Text("বাতিল") } }
        )
    }

    // PRINTABLE MONEY RECEIPT MODAL (Matching Blueprint: 2 Copies - Student Copy & Office Copy)
    selectedFeeForReceipt?.let { fee ->
        AlertDialog(
            onDismissRequest = { selectedFeeForReceipt = null },
            title = {
                Text("মানি রসিদ প্রিভিউ (২ কপি)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Copy 1: Student Copy
                    MoneyReceiptCard(
                        copyTitle = "গ্রাহক / শিক্ষার্থী কপি",
                        fee = fee,
                        madrassaName = generalSettings.madrassaName,
                        district = generalSettings.district
                    )

                    HorizontalDivider(color = Color.Gray, thickness = 1.dp)

                    // Copy 2: Office Copy
                    MoneyReceiptCard(
                        copyTitle = "অফিস কপি",
                        fee = fee,
                        madrassaName = generalSettings.madrassaName,
                        district = generalSettings.district
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "রসিদ প্রিন্ট কমান্ড পাঠানো হয়েছে", Toast.LENGTH_SHORT).show()
                        selectedFeeForReceipt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রিন্ট রসিদ")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFeeForReceipt = null }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
fun MoneyReceiptCard(
    copyTitle: String,
    fee: FeeRecord,
    madrassaName: String,
    district: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(madrassaName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Text("জেলা: $district", fontSize = 10.sp, color = Color.Gray)
                }
                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(4.dp)) {
                    Text(copyTitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("রসিদ বই: #${fee.bookNumber} | রসিদ নং: #${fee.receiptNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("তারিখ: ${fee.date}", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("নাম: ${fee.studentName} (${fee.className} - রোল: ${fee.rollNumber})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text("বাবদ: ${fee.category} • পরিমাণ: ৳ ${fee.amount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("[QR Code]", fontSize = 9.sp, color = Color.Gray)
                Text("হিসাবরক্ষকের স্বাক্ষর: ___________", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 12.sp, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
