package com.example.attendance

import com.example.firestore.FirestoreSchema
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data model representing a student's daily attendance record in Firestore.
 */
data class StudentAttendance(
    val id: String = UUID.randomUUID().toString(),
    val date: String = "", // Format: YYYY-MM-DD
    val time: String = "", // e.g. "08:30 AM"
    val jamatClass: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val roll: String = "",
    val status: String = "উপস্থিত", // "উপস্থিত", "অনুপস্থিত", "ছুটি", "বিলম্ব"
    val guardianContact: String = "",
    val markedBy: String = "এডমিন (HM.ABDUL ALIM)",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Repository responsible for handling Student & Teacher Attendance operations with Firestore.
 */
class AttendanceRepository {

    private val _studentAttendances = MutableStateFlow<List<StudentAttendance>>(emptyList())
    val studentAttendances: StateFlow<List<StudentAttendance>> = _studentAttendances.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToStudentAttendanceFirestore()
        } catch (_: Exception) {
            // Firestore graceful fallback
        }
    }

    private fun listenToStudentAttendanceFirestore() {
        firestore?.collection(FirestoreSchema.Collections.STUDENT_ATTENDANCE)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (!snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(StudentAttendance::class.java)?.copy(id = doc.id)
                    }
                    _studentAttendances.value = list
                }
            }
    }

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
    }

    fun getCurrentTimeString(): String {
        return SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date())
    }

    /**
     * Save or update a single student attendance record in Firestore.
     */
    fun saveStudentAttendance(record: StudentAttendance) {
        val current = _studentAttendances.value.toMutableList()
        val index = current.indexOfFirst { it.studentId == record.studentId && it.date == record.date }
        if (index != -1) {
            current[index] = record
        } else {
            current.add(record)
        }
        _studentAttendances.value = current

        try {
            val docId = FirestoreSchema.buildStudentAttendanceDocId(record.date, record.studentId)
            firestore?.collection(FirestoreSchema.Collections.STUDENT_ATTENDANCE)
                ?.document(docId)
                ?.set(record.copy(id = docId))
        } catch (_: Exception) {}
    }

    /**
     * Save a batch of student attendance records for a specific class and date using Firestore batch write.
     */
    fun saveBatchStudentAttendance(
        records: List<StudentAttendance>,
        onComplete: (Boolean, Int) -> Unit = { _, _ -> }
    ) {
        if (records.isEmpty()) {
            onComplete(true, 0)
            return
        }

        val current = _studentAttendances.value.toMutableList()
        records.forEach { newRecord ->
            val index = current.indexOfFirst { it.studentId == newRecord.studentId && it.date == newRecord.date }
            if (index != -1) {
                current[index] = newRecord
            } else {
                current.add(newRecord)
            }
        }
        _studentAttendances.value = current

        try {
            val batch = firestore?.batch()
            if (batch != null) {
                records.forEach { rec ->
                    val docId = if (rec.id.contains("_")) rec.id else FirestoreSchema.buildStudentAttendanceDocId(rec.date, rec.studentId)
                    val docRef = firestore!!.collection(FirestoreSchema.Collections.STUDENT_ATTENDANCE).document(docId)
                    batch.set(docRef, rec.copy(id = docId))
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true, records.size) }
                    .addOnFailureListener { onComplete(false, records.size) }
            } else {
                onComplete(true, records.size)
            }
        } catch (_: Exception) {
            onComplete(true, records.size)
        }
    }

    /**
     * Helper to get attendance status map for a given date and class
     */
    fun getAttendanceMapForDateAndClass(date: String, jamatClass: String): Map<String, String> {
        val filtered = _studentAttendances.value.filter { rec ->
            rec.date == date && (rec.jamatClass.contains(jamatClass) || jamatClass.contains(rec.jamatClass))
        }
        return filtered.associate { it.studentId to it.status }
    }
}
