package com.example.student

import com.example.firestore.FirestoreSchema
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class Student(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val roll: String = "",
    val jamatClass: String = "শ্রেণি প্লে",
    val age: String = "",
    val guardianContact: String = "",
    val address: String = "",
    val gender: String = "ছাত্র", // ছাত্র / ছাত্রী
    val status: String = "সক্রিয়", // সক্রিয় / নিষ্ক্রিয়
    val lastUpdatedBy: String = "এডমিন (HM.ABDUL ALIM)",
    val lastUpdatedAt: String = ""
)

class StudentRepository {
    private val defaultStudents = listOf(
        Student(id = "1", name = "মোছাঃ হুমায়রা সওদা", roll = "১", jamatClass = "শ্রেণি প্লে", gender = "ছাত্রী"),
        Student(id = "2", name = "মোঃ মোস্তাকিম", roll = "২", jamatClass = "শ্রেণি প্লে", gender = "ছাত্র"),
        Student(id = "3", name = "মোছাঃ মাইমুনা", roll = "৩", jamatClass = "শ্রেণি প্লে", gender = "ছাত্রী"),
        Student(id = "4", name = "মোঃ আব্দুল আহাদ", roll = "৪", jamatClass = "শ্রেণি প্লে", gender = "ছাত্র"),
        Student(id = "5", name = "মোঃ ওমর ফারুক", roll = "৫", jamatClass = "শ্রেণি প্লে", gender = "ছাত্র"),
        Student(id = "6", name = "মোঃ সামিউল ইসলাম", roll = "৬", jamatClass = "শ্রেণি প্লে", gender = "ছাত্র")
    )

    private val defaultClasses = listOf(
        "শ্রেণি প্লে", "শ্রেণি নার্সারি", "শ্রেণি প্রথম", "নূরানী", "নাজেরা", "হিফজ", "ইবতেদায়ী", "দাখিল"
    )

    private val _students = MutableStateFlow<List<Student>>(defaultStudents)
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _classList = MutableStateFlow<List<String>>(defaultClasses)
    val classList: StateFlow<List<String>> = _classList.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToFirestore()
            listenToClassesFirestore()
        } catch (_: Exception) {
            // Firestore fallback
        }
    }

    private fun listenToFirestore() {
        firestore?.collection(FirestoreSchema.Collections.STUDENTS)?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(id = doc.id)
                }
                _students.value = list
            }
        }
    }

    private fun listenToClassesFirestore() {
        firestore?.collection(FirestoreSchema.Collections.CLASSES)?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { it.getString("name") }
                if (list.isNotEmpty()) {
                    _classList.value = list
                }
            }
        }
    }

    fun addClass(className: String) {
        if (className.isBlank() || _classList.value.contains(className)) return
        val current = _classList.value.toMutableList()
        current.add(className)
        _classList.value = current

        try {
            val docId = className.replace(" ", "_")
            firestore?.collection(FirestoreSchema.Collections.CLASSES)?.document(docId)?.set(mapOf("name" to className))
        } catch (_: Exception) {}
    }

    fun removeClass(className: String) {
        val current = _classList.value.toMutableList()
        current.remove(className)
        _classList.value = current

        try {
            val docId = className.replace(" ", "_")
            firestore?.collection(FirestoreSchema.Collections.CLASSES)?.document(docId)?.delete()
        } catch (_: Exception) {}
    }

    fun addStudent(student: Student) {
        val current = _students.value.toMutableList()
        current.add(student)
        _students.value = current

        try {
            firestore?.collection(FirestoreSchema.Collections.STUDENTS)?.document(student.id)?.set(student)
        } catch (_: Exception) {}
    }

    fun updateStudent(student: Student) {
        val current = _students.value.toMutableList()
        val index = current.indexOfFirst { it.id == student.id }
        if (index != -1) {
            current[index] = student
            _students.value = current
        }

        try {
            firestore?.collection(FirestoreSchema.Collections.STUDENTS)?.document(student.id)?.set(student)
        } catch (_: Exception) {}
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }

        try {
            firestore?.collection(FirestoreSchema.Collections.STUDENTS)?.document(studentId)?.delete()
        } catch (_: Exception) {}
    }

    fun addStudents(newStudents: List<Student>, onComplete: (Int) -> Unit = {}) {
        if (newStudents.isEmpty()) {
            onComplete(0)
            return
        }
        val current = _students.value.toMutableList()
        current.addAll(0, newStudents)
        _students.value = current

        try {
            val batch = firestore?.batch()
            if (batch != null) {
                newStudents.forEach { student ->
                    val docRef = firestore!!.collection(FirestoreSchema.Collections.STUDENTS).document(student.id)
                    batch.set(docRef, student)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(newStudents.size) }
                    .addOnFailureListener { onComplete(newStudents.size) }
            } else {
                onComplete(newStudents.size)
            }
        } catch (_: Exception) {
            onComplete(newStudents.size)
        }
    }
}

fun parseStudentCsv(csvContent: String): List<Student> {
    val lines = csvContent.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()

    val delimiter = if (lines.first().contains("\t")) "\t" else if (lines.first().contains(";")) ";" else ","

    fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (char in line) {
            if (char == '"') {
                inQuotes = !inQuotes
            } else if (char.toString() == delimiter && !inQuotes) {
                result.add(sb.toString().trim().removeSurrounding("\""))
                sb.clear()
            } else {
                sb.append(char)
            }
        }
        result.add(sb.toString().trim().removeSurrounding("\""))
        return result
    }

    val parsedLines = lines.map { parseLine(it) }
    if (parsedLines.isEmpty()) return emptyList()

    val header = parsedLines.first().map { it.lowercase().replace(" ", "").replace("_", "") }
    val hasHeader = header.any { 
        it.contains("name") || it.contains("নাম") || 
        it.contains("roll") || it.contains("রোল") || 
        it.contains("class") || it.contains("শ্রেণী") || it.contains("শ্রেণি") 
    }

    val startIndex = if (hasHeader) 1 else 0

    var rollIdx = -1
    var nameIdx = -1
    var classIdx = -1
    var fatherIdx = -1
    var motherIdx = -1
    var contactIdx = -1
    var addressIdx = -1
    var genderIdx = -1

    if (hasHeader) {
        header.forEachIndexed { idx, col ->
            when {
                col.contains("roll") || col.contains("রোল") -> rollIdx = idx
                col.contains("name") || col.contains("নাম") && !col.contains("father") && !col.contains("mother") && !col.contains("পিতা") && !col.contains("মাতা") -> nameIdx = idx
                col.contains("father") || col.contains("পিতা") || col.contains("বাবা") -> fatherIdx = idx
                col.contains("mother") || col.contains("মাতা") || col.contains("মা") -> motherIdx = idx
                col.contains("class") || col.contains("শ্রেণী") || col.contains("শ্রেণি") || col.contains("জামাত") -> classIdx = idx
                col.contains("contact") || col.contains("phone") || col.contains("mobile") || col.contains("মোবাইল") || col.contains("ফোন") || col.contains("যোগাযোগ") -> contactIdx = idx
                col.contains("address") || col.contains("ঠিকানা") -> addressIdx = idx
                col.contains("gender") || col.contains("লিঙ্গ") || col.contains("জেন্ডার") -> genderIdx = idx
            }
        }
    }

    val timeStamp = java.text.SimpleDateFormat("dd MMM, yyyy hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
    val students = mutableListOf<Student>()

    for (i in startIndex until parsedLines.size) {
        val row = parsedLines[i]
        if (row.all { it.isBlank() }) continue

        fun getVal(idx: Int, defaultPos: Int): String {
            val targetIdx = if (hasHeader && idx != -1) idx else defaultPos
            return if (targetIdx < row.size) row[targetIdx].trim() else ""
        }

        val roll = getVal(rollIdx, 0)
        val name = getVal(nameIdx, 1)
        val jamatClass = getVal(classIdx, 2).ifBlank { "শ্রেণি প্লে" }
        val fatherName = getVal(fatherIdx, 3)
        val motherName = getVal(motherIdx, 4)
        val contact = getVal(contactIdx, 5)
        val address = getVal(addressIdx, 6)
        val genderVal = getVal(genderIdx, 7)
        val gender = if (genderVal.contains("নারী") || genderVal.contains("ছাত্রী") || genderVal.lowercase().contains("female")) "ছাত্রী" else "ছাত্র"

        if (name.isNotBlank() || roll.isNotBlank()) {
            students.add(
                Student(
                    roll = roll,
                    name = name.ifBlank { "শিক্ষার্থী ${i + 1}" },
                    fatherName = fatherName,
                    motherName = motherName,
                    jamatClass = jamatClass,
                    guardianContact = contact,
                    address = address,
                    gender = gender,
                    lastUpdatedBy = "CSV Batch Import",
                    lastUpdatedAt = timeStamp
                )
            )
        }
    }

    return students
}

