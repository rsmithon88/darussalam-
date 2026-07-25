package com.example.teacher

import com.example.firestore.FirestoreSchema
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class Teacher(
    val id: String = UUID.randomUUID().toString(),
    val photoUrl: String = "",
    val name: String = "",
    val designation: String = "সাধারণ শিক্ষক",
    val subject: String = "",
    val address: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "পুরুষ",
    val password: String = "",
    val isSuperAdmin: Boolean = false,
    val permissions: List<String> = emptyList(),
    val assignedClasses: List<String> = emptyList()
)

data class TeacherAttendance(
    val id: String = UUID.randomUUID().toString(),
    val teacherId: String = "",
    val teacherName: String = "",
    val designation: String = "",
    val date: String = "", // e.g. "2026-07-24"
    val time: String = "", // e.g. "08:30 AM"
    val status: String = "উপস্থিত", // "উপস্থিত", "বিলম্ব", "অনুপস্থিত", "ছুটি"
    val note: String = ""
)

class TeacherRepository {
    private val defaultTeachers = listOf(
        Teacher(
            id = "1",
            name = "মুফতী মোহাম্মদ আব্দুল্লাহ",
            designation = "প্রধান শিক্ষক",
            subject = "হাদীস ও ফিকাহ",
            address = "ঢাকা, বাংলাদেশ",
            email = "headmadrassa@gmail.com",
            phone = "01711223344",
            gender = "পুরুষ",
            isSuperAdmin = true,
            permissions = listOf("শিক্ষার্থীরা", "হাজিরা", "শিক্ষক হাজিরা", "ফি ব্যবস্থাপনা", "ব্যয় ব্যবস্থাপনা", "পরীক্ষা", "রেজাল্ট", "সময়সূচী", "নোটিশ বোর্ড", "রিপোর্ট", "শিক্ষার্থী তথ্য", "ছুটির আবেদন", "ইনফরমেশন ডাউনলোড"),
            assignedClasses = listOf("সকল শ্রেণী")
        ),
        Teacher(
            id = "2",
            name = "মাওলানা কারী ওবায়দুল্লাহ",
            designation = "সহকারী শিক্ষক",
            subject = "কুরআন ও তাজবীদ",
            address = "দারুস সালাম",
            email = "obaid@gmail.com",
            phone = "01811223344",
            gender = "পুরুষ",
            isSuperAdmin = false,
            permissions = listOf("শিক্ষার্থীরা", "হাজিরা", "রেজাল্ট"),
            assignedClasses = listOf("শ্রেণি প্লে", "শ্রেণি ১ম", "নূরানী")
        ),
        Teacher(
            id = "3",
            name = "হাফেজ মাওলানা বিলাল হোসাইন",
            designation = "সহকারী শিক্ষক",
            subject = "হিফজুল কুরআন",
            address = "রাজবাড়ী",
            email = "bilal@gmail.com",
            phone = "01911223344",
            gender = "পুরুষ",
            isSuperAdmin = false,
            permissions = listOf("শিক্ষার্থীরা", "হাজিরা"),
            assignedClasses = listOf("হিফজ", "নাজেরা")
        )
    )

    private val _teachers = MutableStateFlow<List<Teacher>>(defaultTeachers)
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _attendances = MutableStateFlow<List<TeacherAttendance>>(emptyList())
    val attendances: StateFlow<List<TeacherAttendance>> = _attendances.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToFirestore()
            listenToAttendanceFirestore()
        } catch (_: Exception) {
            // Firestore fallback
        }
        // Initialize default sample attendance for today if empty
        if (_attendances.value.isEmpty()) {
            initSampleAttendance()
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }

    private fun getCurrentTimeString(): String {
        return SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())
    }

    private fun initSampleAttendance() {
        val today = getTodayDateString()
        val sampleList = listOf(
            TeacherAttendance(
                id = "att_1",
                teacherId = "1",
                teacherName = "মুফতী মোহাম্মদ আব্দুল্লাহ",
                designation = "প্রধান শিক্ষক",
                date = today,
                time = "08:15 AM",
                status = "উপস্থিত"
            )
        )
        _attendances.value = sampleList
    }

    private fun listenToFirestore() {
        firestore?.collection(FirestoreSchema.Collections.TEACHERS)?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Teacher::class.java)?.copy(id = doc.id)
                }
                _teachers.value = list
            }
        }
    }

    private fun listenToAttendanceFirestore() {
        firestore?.collection(FirestoreSchema.Collections.TEACHER_ATTENDANCE)?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TeacherAttendance::class.java)?.copy(id = doc.id)
                }
                _attendances.value = list
            }
        }
    }

    fun isAttendanceWindowActive(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24 hour format (0-23)
        val minute = calendar.get(Calendar.MINUTE)
        // Active from 8:00 AM (08:00) to 4:00 PM (16:00)
        return (hour in 8..15) || (hour == 16 && minute == 0)
    }

    fun markSingleAttendance(
        teacher: Teacher,
        status: String = "উপস্থিত",
        date: String = getTodayDateString(),
        note: String = "",
        bypassTimeCheck: Boolean = false
    ): Pair<Boolean, String> {
        if (!bypassTimeCheck && !isAttendanceWindowActive()) {
            return Pair(false, "দুঃখিত! শিক্ষক হাজিরা গ্রহণের সময় সকাল ০৮:০০ টা থেকে বিকাল ০৪:০০ টা পর্যন্ত। বর্তমানে হাজিরা দেয়া সম্ভব নয়।")
        }

        val currentList = _attendances.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.teacherId == teacher.id && it.date == date }
        val timeNow = getCurrentTimeString()

        val record = TeacherAttendance(
            id = if (existingIndex != -1) currentList[existingIndex].id else UUID.randomUUID().toString(),
            teacherId = teacher.id,
            teacherName = teacher.name,
            designation = teacher.designation,
            date = date,
            time = timeNow,
            status = status,
            note = note
        )

        if (existingIndex != -1) {
            currentList[existingIndex] = record
        } else {
            currentList.add(record)
        }

        _attendances.value = currentList

        try {
            firestore?.collection(FirestoreSchema.Collections.TEACHER_ATTENDANCE)?.document(record.id)?.set(record)
        } catch (_: Exception) {}

        return Pair(true, "${teacher.name}-এর হাজিরা সফলভাবে '$status' হিসেবে গৃহীত হয়েছে। ($timeNow)")
    }

    fun markAllTeachersPresent(
        date: String = getTodayDateString(),
        bypassTimeCheck: Boolean = false
    ): Pair<Boolean, String> {
        if (!bypassTimeCheck && !isAttendanceWindowActive()) {
            return Pair(false, "দুঃখিত! শিক্ষক হাজিরা গ্রহণের সময় সকাল ০৮:০০ টা থেকে বিকাল ০৪:০০ টা পর্যন্ত।")
        }

        val allT = _teachers.value
        val currentList = _attendances.value.toMutableList()
        val timeNow = getCurrentTimeString()
        var count = 0

        allT.forEach { teacher ->
            val existingIndex = currentList.indexOfFirst { it.teacherId == teacher.id && it.date == date }
            val record = TeacherAttendance(
                id = if (existingIndex != -1) currentList[existingIndex].id else UUID.randomUUID().toString(),
                teacherId = teacher.id,
                teacherName = teacher.name,
                designation = teacher.designation,
                date = date,
                time = timeNow,
                status = "উপস্থিত"
            )
            if (existingIndex != -1) {
                currentList[existingIndex] = record
            } else {
                currentList.add(record)
            }
            count++

            try {
                firestore?.collection(FirestoreSchema.Collections.TEACHER_ATTENDANCE)?.document(record.id)?.set(record)
            } catch (_: Exception) {}
        }

        _attendances.value = currentList
        return Pair(true, "সকল $count জন শিক্ষকের হাজিরা এক ক্লিকে 'উপস্থিত' করা হয়েছে। ($timeNow)")
    }

    fun addTeacher(teacher: Teacher) {
        val current = _teachers.value.toMutableList()
        current.add(teacher)
        _teachers.value = current

        try {
            firestore?.collection(FirestoreSchema.Collections.TEACHERS)?.document(teacher.id)?.set(teacher)
        } catch (_: Exception) {}
    }

    fun updateTeacher(teacher: Teacher) {
        val current = _teachers.value.toMutableList()
        val index = current.indexOfFirst { it.id == teacher.id }
        if (index != -1) {
            current[index] = teacher
            _teachers.value = current

            try {
                firestore?.collection(FirestoreSchema.Collections.TEACHERS)?.document(teacher.id)?.set(teacher)
            } catch (_: Exception) {}
        }
    }

    fun deleteTeacher(id: String) {
        val current = _teachers.value.toMutableList()
        current.removeAll { it.id == id }
        _teachers.value = current

        try {
            firestore?.collection(FirestoreSchema.Collections.TEACHERS)?.document(id)?.delete()
        } catch (_: Exception) {}
    }
}

fun isClassAllowed(studentClass: String, allowedClasses: List<String>?): Boolean {
    if (allowedClasses.isNullOrEmpty() || allowedClasses.contains("সকল শ্রেণী") || allowedClasses.contains("সমস্ত শ্রেণী")) {
        return true
    }
    if (allowedClasses.contains(studentClass)) return true

    fun normalize(cls: String): String {
        return cls.trim()
            .replace("শ্রেণি", "")
            .replace("শ্রেণী", "")
            .replace("১ম", "প্রথম")
            .replace("২য়", "দ্বিতীয়")
            .replace("৩য়", "তৃতীয়")
            .replace("৪র্থ", "চতুর্থ")
            .replace("৫ম", "পঞ্চম")
            .replace("হেফজ", "হিফজ")
            .trim()
    }

    val normalizedStudentClass = normalize(studentClass)
    return allowedClasses.any { allowed ->
        val normalizedAllowed = normalize(allowed)
        normalizedAllowed.equals(normalizedStudentClass, ignoreCase = true) ||
                normalizedAllowed.contains(normalizedStudentClass, ignoreCase = true) ||
                normalizedStudentClass.contains(normalizedAllowed, ignoreCase = true)
    }
}

