package com.example.reports

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("সার্বিক রিপোর্ট ও বিবরণী কেন্দ্র", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                    Text("পিডিএফ ও এক্সেল ফরম্যাটে রিপোর্ট ডাউনলোড করুন", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ReportCard(
                    title = "মাসিক অর্থ আদায় ও ব্যয় রিপোর্ট",
                    description = "চলতি মাসের সমস্ত ফি আদায়, অন্যান্য আয় ও খরচের সম্পূর্ণ খতিয়ান।",
                    icon = Icons.Default.Payments,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = Color(0xFF15803D),
                    onDownloadPdf = { Toast.makeText(context, "অর্থ আদায় রিপোর্ট PDF ডাউনলোড করা হয়েছে", Toast.LENGTH_SHORT).show() },
                    onDownloadExcel = { Toast.makeText(context, "অর্থ আদায় রিপোর্ট Excel ডাউনলোড করা হয়েছে", Toast.LENGTH_SHORT).show() }
                )
            }
            item {
                ReportCard(
                    title = "শিক্ষার্থী উপস্থিতি ও অনুপস্থিতি সামারি",
                    description = "শ্রেণিভিত্তিক উপস্থিতি শতকরা হার ও অভিভাবকদের নোটিফিকেশন তথ্য।",
                    icon = Icons.Default.EventAvailable,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    onDownloadPdf = { Toast.makeText(context, "উপস্থিতি সামারি PDF জেনারেট হয়েছে", Toast.LENGTH_SHORT).show() },
                    onDownloadExcel = { Toast.makeText(context, "উপস্থিতি Excel ফাইল জেনারেট হয়েছে", Toast.LENGTH_SHORT).show() }
                )
            }
            item {
                ReportCard(
                    title = "শিক্ষক বেতন পে-রোল ও ভাতা বিবরণী",
                    description = "শিক্ষকদের মূল বেতন, বোনাস ও বাৎসরিক আয়কর/ভাতা তথ্য।",
                    icon = Icons.Default.AccountBalance,
                    iconBg = Color(0xFFFAF5FF),
                    iconTint = Color(0xFF9333EA),
                    onDownloadPdf = { Toast.makeText(context, "পে-রোল সামারি PDF ডাউনলোড হয়েছে", Toast.LENGTH_SHORT).show() },
                    onDownloadExcel = { Toast.makeText(context, "পে-রোল Excel ডাউনলোড হয়েছে", Toast.LENGTH_SHORT).show() }
                )
            }
            item {
                ReportCard(
                    title = "মেধাক্রম ও ফলাফল সামারি রিপোর্ট",
                    description = "পরীক্ষার শ্রেণিভিত্তিক সেরা মেধা তালিকা ও জিপিএ পরিসংখ্যান।",
                    icon = Icons.Default.Assessment,
                    iconBg = Color(0xFFFFF7ED),
                    iconTint = Color(0xFFEA580C),
                    onDownloadPdf = { Toast.makeText(context, "মেধা তালিকা PDF জেনারেট করা হয়েছে", Toast.LENGTH_SHORT).show() },
                    onDownloadExcel = { Toast.makeText(context, "মেধা তালিকা Excel ফাইল প্রস্তুত", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    onDownloadPdf: () -> Unit,
    onDownloadExcel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = iconBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text(description, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onDownloadExcel, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDownloadPdf, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF রিপোর্ট", fontSize = 11.sp)
                }
            }
        }
    }
}
