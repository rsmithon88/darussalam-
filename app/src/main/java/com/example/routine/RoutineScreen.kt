package com.example.routine

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RoutinePeriod(
    val id: String,
    val periodName: String, // "১ম পিরিয়ড", "২য় পিরিয়ড"
    val timeSlot: String,   // "০৮:০০ AM - ০৮:৪৫ AM"
    val subject: String,
    val teacherName: String,
    val className: String
)

@Composable
fun RoutineScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedClass by remember { mutableStateOf("হেফজ") }
    val classes = listOf("হেফজ", "মিশকাত", "শরহে বেকায়া", "নাহবে মীর")

    var routineList by remember {
        mutableStateOf(
            listOf(
                RoutinePeriod("1", "১ম পিরিয়ড", "০৮:০০ AM - ০৮:৪৫ AM", "কুরআন মজিদ হেফজ", "মাওলানা জহিরুল ইসলাম", "হেফজ"),
                RoutinePeriod("2", "২য় পিরিয়ড", "০৮:৪৫ AM - ০৯:৩০ AM", "তাজবীদুল কুরআন", "মুফতি সামছুল হক", "হেফজ"),
                RoutinePeriod("3", "৩য় পিরিয়ড", "০৯:৩০ AM - ১০:১৫ AM", "হাদিস শরীফ", "মুফতি আব্দুল করিম", "মিশকাত"),
                RoutinePeriod("4", "৪র্থ পিরিয়ড", "১০:১৫ AM - ১১:০০ AM", "ফিকহ (শরহে বেকায়া)", "মাওলানা আব্দুর রহমান", "শরহে বেকায়া")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("শ্রেণিভিত্তিক পিরিয়ড ও রুটিন", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পিরিয়ড যোগ করুন")
                }
            }
        }

        // Class Selection Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            classes.forEach { cls ->
                FilterChip(
                    selected = selectedClass == cls,
                    onClick = { selectedClass = cls },
                    label = { Text(cls, fontSize = 12.sp) }
                )
            }
        }

        val filteredRoutine = routineList.filter { it.className == selectedClass }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredRoutine, key = { it.id }) { period ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                                    Text(period.periodName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(period.timeSlot, fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(period.subject, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            Text("শিক্ষক: ${period.teacherName}", fontSize = 12.sp, color = Color(0xFF475569))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var periodName by remember { mutableStateOf("১ম পিরিয়ড") }
        var timeSlot by remember { mutableStateOf("০৮:০০ AM - ০৮:৪৫ AM") }
        var subject by remember { mutableStateOf("") }
        var teacherName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("নতুন পিরিয়ড যুক্ত করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = periodName,
                        onValueChange = { periodName = it },
                        label = { Text("পিরিয়ডের নাম (যেমন: ১ম পিরিয়ড)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("সময়সীমা (যেমন: ০৮:০০ AM - ০৮:৪৫ AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("বিষয় / কিতাবের নাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("দায়িত্বপ্রাপ্ত শিক্ষক") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank()) {
                            val newPeriod = RoutinePeriod(
                                id = System.currentTimeMillis().toString(),
                                periodName = periodName,
                                timeSlot = timeSlot,
                                subject = subject,
                                teacherName = teacherName,
                                className = selectedClass
                            )
                            routineList = routineList + newPeriod
                            showAddDialog = false
                            Toast.makeText(context, "রুটিনে পিরিয়ড সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("বাতিল") } }
        )
    }
}
