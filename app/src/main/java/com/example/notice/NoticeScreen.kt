package com.example.notice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.settings.SettingsManager
import java.text.SimpleDateFormat
import java.util.*

data class NoticeItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetAudience: String, // "সকলের জন্য", "অভিভাবকগণ", "শিক্ষকমণ্ডলী"
    val date: String,
    val isSmsSent: Boolean = false
)

@Composable
fun NoticeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val smsSettings by SettingsManager.instance.smsSettings.collectAsState()

    val notices by NoticeRepository.notices.collectAsState()

    var showAddNoticeDialog by remember { mutableStateOf(false) }
    var selectedNoticeForSmsPreview by remember { mutableStateOf<NoticeItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner Card
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ডিজিটাল নোটিশ বোর্ড ও SMS ব্রডকাস্ট", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF0F172A))
                    }
                    Text("SMS গেটওয়ে: ${smsSettings.gatewayName}", fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Button(
                    onClick = { showAddNoticeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন নোটিশ")
                }
            }
        }

        Text("প্রকাশিত নোটিশসমূহ:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

        if (notices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "বর্তমানে কোনো নোটিশ নেই",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "এডমিন প্যানেল থেকে 'নতুন নোটিশ' বোতামে চাপ দিয়ে নোটিশ প্রকাশ করতে পারেন।",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notices, key = { it.id }) { notice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                Surface(color = Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp)) {
                                    Text(notice.targetAudience, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }

                            Text(notice.description, fontSize = 13.sp, color = Color(0xFF334155))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("তারিখ: ${notice.date}", fontSize = 11.sp, color = Color(0xFF94A3B8))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { NoticeRepository.deleteNotice(notice.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "মুছে ফেলুন", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }

                                    OutlinedButton(
                                        onClick = { selectedNoticeForSmsPreview = notice },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS ব্রডকাস্ট", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD NOTICE DIALOG
    if (showAddNoticeDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var audience by remember { mutableStateOf("সকলের জন্য") }

        AlertDialog(
            onDismissRequest = { showAddNoticeDialog = false },
            title = { Text("নতুন নোটিশ তৈরি", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("নোটিশের শিরোনাম") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("বিবরণ") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("প্রাপক শ্রেণি:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = audience == "সকলের জন্য", onClick = { audience = "সকলের জন্য" }, label = { Text("সকলের জন্য", fontSize = 11.sp) })
                        FilterChip(selected = audience == "অভিভাবকগণ", onClick = { audience = "অভিভাবকগণ" }, label = { Text("অভিভাবকগণ", fontSize = 11.sp) })
                        FilterChip(selected = audience == "শিক্ষকমণ্ডলী", onClick = { audience = "শিক্ষকমণ্ডলী" }, label = { Text("শিক্ষকমণ্ডলী", fontSize = 11.sp) })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newItem = NoticeItem(
                                title = title,
                                description = desc,
                                targetAudience = audience,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                            )
                            NoticeRepository.addNotice(newItem)
                            showAddNoticeDialog = false
                            Toast.makeText(context, "নোটিশ প্রকাশ করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                ) {
                    Text("প্রকাশ করুন")
                }
            },
            dismissButton = { TextButton(onClick = { showAddNoticeDialog = false }) { Text("বাতিল") } }
        )
    }

    // SMS BROADCAST LIVE PREVIEW MODAL (Matching Blueprint)
    selectedNoticeForSmsPreview?.let { notice ->
        val smsCharCount = notice.description.length
        val recipientCount = if (notice.targetAudience == "শিক্ষকমণ্ডলী") 15 else 280

        AlertDialog(
            onDismissRequest = { selectedNoticeForSmsPreview = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF059669))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SMS ব্রডকাস্ট লাইভ প্রিভিউ", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("প্রাক-দর্শন মেসেজ টেক্সট:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("\"${notice.title}: ${notice.description}\"", fontSize = 13.sp, color = Color(0xFF0F172A))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট প্রাপক: $recipientCount জন (${notice.targetAudience})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("মেসেজ লেন্থ: $smsCharCount অক্ষর", fontSize = 12.sp, color = Color.Gray)
                    }

                    Text("SMS প্রোভাইডার: ${smsSettings.gatewayName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                    if (smsSettings.gatewayName.contains("Texbee")) {
                        Text("Texbee SIM 1 দিয়ে সরাসরি আপনার মোবাইল ফোন থেকে সম্পূর্ণ ফ্রিতে SMS পাঠানো হইবে।", fontSize = 11.sp, color = Color(0xFF15803D))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "${smsSettings.gatewayName} গেটওয়ে দিয়ে $recipientCount জন এর নিকট SMS পাঠানো শুরু হয়েছে!", Toast.LENGTH_LONG).show()
                        selectedNoticeForSmsPreview = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("SMS ব্রডকাস্ট নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNoticeForSmsPreview = null }) { Text("বাতিল") }
            }
        )
    }
}
