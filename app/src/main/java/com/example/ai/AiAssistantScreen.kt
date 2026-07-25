package com.example.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("1", "ai", "আসসালামু আলাইকুম! আমি গুগল জেমিনাই চালিত দারুস সালাম মাদরাসা এআই অ্যাসিস্ট্যান্ট। আমি আপনাকে পরীক্ষার প্রশ্নপত্র ড্রাফট করা, নোটিশ তৈরি, আরবি ব্যাকরণ ও পাঠ পরিকল্পনা তৈরিতে সাহায্য করতে পারি। বলুন কীভাবে সাহায্য করব?")
            )
        )
    }

    val quickPrompts = listOf(
        "বার্ষিক মাহফিলের দাওয়াতনামা ড্রাফট করো",
        "হাদিস শরীফের পাঠ পরিকল্পনা বানিয়ে দাও",
        "শরহে বেকায়া পরীক্ষার ১০টি প্রশ্ন তৈরি করো"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color(0xFFFAF5FF), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("জেমিনাই এআই শিক্ষক হেলপার", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                    Text("স্মার্ট টেক্সট, নোটিশ ও লেসন প্ল্যান জেনারেটর", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }

        // Quick Prompts Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                SuggestionChip(
                    onClick = { inputText = prompt },
                    label = { Text(prompt, fontSize = 11.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Chat Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Surface(
                        color = if (isUser) Color(0xFF1D4ED8) else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) Color.White else Color(0xFF1E293B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (isThinking) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("জেমিনাই এআই উত্তর তৈরি করছে...", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Input Field Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("এআই অ্যাসিস্ট্যান্টকে প্রশ্ন করুন...") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val userMsg = ChatMessage(System.currentTimeMillis().toString(), "user", inputText)
                        messages = messages + userMsg
                        val query = inputText
                        inputText = ""
                        isThinking = true

                        // Simulate AI Response
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isThinking = false
                            val aiReplyText = when {
                                query.contains("মাহফিল") || query.contains("নোটিশ") ->
                                    "সম্মানিত অভিভাবকবৃন্দ,\nআসসালামু আলাইকুম। অত্যন্ত আনন্দের সাথে জানানো যাচ্ছে যে, আগামী ১০ আগস্ট ২০২৬ ইং তারিখে মাদরাসা প্রাঙ্গণে বার্ষিক ওয়াজ ও দোয়া মাহফিল অনুষ্ঠিত হবে। আপনাদের সকলের সদয় উপস্থিতি কাম্য।"
                                query.contains("প্রশ্ন") ->
                                    "১. মিরাছ সম্পর্কিত মূল নীতিগুলো বর্ণনা করো।\n২. নাহবে মীর কিতাবের আলোকে আমেল শব্দের পরিচয় দাও।\n৩. হাদিস শরীফের বিশুদ্ধতার শর্তসমূহ আলোচনা করো।"
                                else ->
                                    "আপনার জিজ্ঞাসার উত্তরে জানানো যাচ্ছে যে, দারুস সালাম মাদরাসার শিক্ষা কারিকুলাম ও নিয়ম অনুযায়ী আপনার প্রদত্ত নির্দেশনাটি সুন্দরভাবে প্রসেস করা সম্ভব।"
                            }
                            val aiMsg = ChatMessage((System.currentTimeMillis() + 1).toString(), "ai", aiReplyText)
                            messages = messages + aiMsg
                        }, 1200)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}
