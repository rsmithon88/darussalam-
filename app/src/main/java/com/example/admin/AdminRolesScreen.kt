package com.example.admin

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.teacher.TeacherRepository

data class LeaveApplication(
    val id: String,
    val teacherName: String,
    val reason: String,
    val startDate: String,
    val endDate: String,
    var status: String // "আবেদনকৃত", "অনুমোদিত", "বাতিল"
)

data class ActivityLogItem(
    val id: String,
    val userName: String,
    val action: String,
    val time: String
)

@Composable
fun AdminRolesScreen(
    teacherRepository: TeacherRepository = remember { TeacherRepository() },
    initialTab: Int = 0, // 0: Roles & Permissions, 1: Leave Management, 2: Activity Logs
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teachers by teacherRepository.teachers.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }

    var leaveApplications by remember {
        mutableStateOf(
            listOf(
                LeaveApplication("1", "মাওলানা জহিরুল ইসলাম", "জরুরি পারিবারিক বিষয়", "2026-07-26", "2026-07-28", "আবেদনকৃত"),
                LeaveApplication("2", "মুফতি আব্দুল করিম", "অসুস্থতা জনিত ছুটি", "2026-07-20", "2026-07-22", "অনুমোদিত")
            )
        )
    }

    val activityLogs = remember {
        listOf(
            ActivityLogItem("l1", "এডমিন সামছুল হক", "নতুন শিক্ষার্থী ভর্তি সম্পন্ন করেছেন (#104)", "১০:১৫ AM"),
            ActivityLogItem("l2", "মাওলানা জহিরুল ইসলাম", "হেফজ ক্লাসের হাজিরা সেভ করেছেন", "০৯:৩০ AM"),
            ActivityLogItem("l3", "এডমিন সামছুল হক", "Texbee SMS ব্রডকাস্ট পাঠিয়েছেন", "গতকাল ০৮:০০ PM")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1D4ED8)
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("পারমিশন কন্ট্রোল", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("ছুটির আবেদন", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("কার্যকলাপ লগ", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
        }

        when (selectedTab) {
            0 -> {
                Text("শিক্ষকদের পেজ এক্সেস ও রোল পারমিশন:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(teachers, key = { it.id }) { teacher ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(teacher.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                        Text("পদবী: ${teacher.designation}", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text("একটিভ শিক্ষক", fontSize = 11.sp, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("অনুমোদিত পেজ: হাজিরা, ফলাফল ইনপুট, রুটিন ভিউ", fontSize = 12.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                Text("শিক্ষকদের ছুটির আবেদনসমূহ:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(leaveApplications, key = { it.id }) { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.teacherName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Surface(
                                        color = when(app.status) {
                                            "অনুমোদিত" -> Color(0xFFDCFCE7)
                                            "বাতিল" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(app.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }

                                Text("কারণ: ${app.reason}", fontSize = 13.sp, color = Color(0xFF334155))
                                Text("সময়কাল: ${app.startDate} হতে ${app.endDate}", fontSize = 12.sp, color = Color(0xFF64748B))

                                if (app.status == "আবেদনকৃত") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                leaveApplications = leaveApplications.map {
                                                    if (it.id == app.id) it.copy(status = "অনুমোদিত") else it
                                                }
                                                Toast.makeText(context, "ছুটি অনুমোদন করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("অনুমোদন দিন", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                leaveApplications = leaveApplications.map {
                                                    if (it.id == app.id) it.copy(status = "বাতিল") else it
                                                }
                                                Toast.makeText(context, "ছুটির আবেদন বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("বাতিল করুন", fontSize = 11.sp, color = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                Text("সিস্টেম ব্যবহারের কার্যকলাপ লগ (Activity Logs):", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activityLogs, key = { it.id }) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Text(log.action, fontSize = 12.sp, color = Color(0xFF475569))
                                }
                                Text(log.time, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
