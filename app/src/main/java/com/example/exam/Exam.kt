package com.example.exam

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class Exam(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val year: String = "২০২৬",
    val date: String = "",
    val targetClasses: List<String> = emptyList(),
    val subjects: List<String> = listOf("কুরআন ও তাজবীদ", "হাদীস শরীফ", "আরবী", "বাংলা", "ইংরেজি", "গণিত"),
    val subjectTotalMarks: Int = 100,
    val passMarks: Int = 33,
    val status: String = "চলমান" // চলমান, প্রকাশিত, খসড়া
)

data class StudentResult(
    val id: String = UUID.randomUUID().toString(),
    val examId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentRoll: String = "",
    val jamatClass: String = "",
    val subjectMarks: Map<String, Double> = emptyMap(), // Subject Name -> Marks
    val totalObtainedMarks: Double = 0.0,
    val totalFullMarks: Double = 0.0,
    val percentage: Double = 0.0,
    val grade: String = "মাকবুল",
    val status: String = "উত্তীর্ণ", // উত্তীর্ণ / অনুত্তীর্ণ
    val position: Int = 0,
    val remarks: String = "মাশাআল্লাহ, ভালো ফলাফল!"
)

class ExamRepository {
    private val defaultExams = listOf(
        Exam(
            id = "exam_1",
            title = "১ম সাময়িক পরীক্ষা ২০২৬",
            year = "২০২৬",
            date = "১৫ মার্চ, ২০২৬",
            targetClasses = listOf("শ্রেণি প্লে", "শ্রেণি নার্সারি", "শ্রেণি প্রথম", "নূরানী", "হিফজ"),
            subjects = listOf("কুরআন ও তাজবীদ", "হাদীস শরীফ", "আরবী", "বাংলা", "ইংরেজি", "গণিত"),
            subjectTotalMarks = 100,
            passMarks = 33,
            status = "প্রকাশিত"
        ),
        Exam(
            id = "exam_2",
            title = "২য় সাময়িক পরীক্ষা ২০২৬",
            year = "২০২৬",
            date = "২০ জুলাই, ২০২৬",
            targetClasses = listOf("শ্রেণি প্লে", "শ্রেণি নার্সারি", "শ্রেণি প্রথম", "নূরানী"),
            subjects = listOf("কুরআন ও তাজবীদ", "হাদীস শরীফ", "আরবী", "বাংলা", "ইংরেজি", "গণিত"),
            subjectTotalMarks = 100,
            passMarks = 33,
            status = "চলমান"
        ),
        Exam(
            id = "exam_3",
            title = "বার্ষিক পরীক্ষা ২০২৬",
            year = "২০২৬",
            date = "১০ নভেম্বর, ২০২৬",
            targetClasses = listOf("শ্রেণি প্লে", "শ্রেণি নার্সারি", "শ্রেণি প্রথম", "নূরানী", "হিফজ"),
            subjects = listOf("কুরআন ও তাজবীদ", "হাদীস শরীফ", "আরবী", "বাংলা", "ইংরেজি", "গণিত"),
            subjectTotalMarks = 100,
            passMarks = 33,
            status = "খসড়া"
        )
    )

    private val defaultResults = mutableListOf(
        StudentResult(
            id = "res_1",
            examId = "exam_1",
            studentId = "1",
            studentName = "মোছাঃ হুমায়রা সওদা",
            studentRoll = "১",
            jamatClass = "শ্রেণি প্লে",
            subjectMarks = mapOf(
                "কুরআন ও তাজবীদ" to 95.0,
                "হাদীস শরীফ" to 92.0,
                "আরবী" to 88.0,
                "বাংলা" to 90.0,
                "ইংরেজি" to 85.0,
                "গণিত" to 94.0
            ),
            totalObtainedMarks = 544.0,
            totalFullMarks = 600.0,
            percentage = 90.66,
            grade = "মুমতাজ (A+)",
            status = "উত্তীর্ণ",
            position = 1,
            remarks = "চমৎকার ও প্রশংসনীয় পারফরম্যান্স!"
        ),
        StudentResult(
            id = "res_2",
            examId = "exam_1",
            studentId = "2",
            studentName = "মোঃ মোস্তাকিম",
            studentRoll = "২",
            jamatClass = "শ্রেণি প্লে",
            subjectMarks = mapOf(
                "কুরআন ও তাজবীদ" to 88.0,
                "হাদীস শরীফ" to 85.0,
                "আরবী" to 82.0,
                "বাংলা" to 80.0,
                "ইংরেজি" to 78.0,
                "গণিত" to 89.0
            ),
            totalObtainedMarks = 502.0,
            totalFullMarks = 600.0,
            percentage = 83.66,
            grade = "জাইয়্যিদ জিদ্দান (A)",
            status = "উত্তীর্ণ",
            position = 2,
            remarks = "বেশ ভালো উন্নতি হয়েছে, মাশাআল্লাহ!"
        )
    )

    private val _exams = MutableStateFlow<List<Exam>>(defaultExams)
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    private val _results = MutableStateFlow<List<StudentResult>>(defaultResults)
    val results: StateFlow<List<StudentResult>> = _results.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            listenToExamsFirestore()
            listenToResultsFirestore()
        } catch (_: Exception) {
            // Firestore optional fallback
        }
    }

    private fun listenToExamsFirestore() {
        firestore?.collection("exams")?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Exam::class.java)
                }
                if (list.isNotEmpty()) {
                    _exams.value = list
                }
            }
        }
    }

    private fun listenToResultsFirestore() {
        firestore?.collection("student_results")?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            if (!snapshot.isEmpty) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(StudentResult::class.java)
                }
                if (list.isNotEmpty()) {
                    _results.value = list
                }
            }
        }
    }

    fun addExam(exam: Exam) {
        val current = _exams.value.toMutableList()
        current.add(0, exam)
        _exams.value = current
        try {
            firestore?.collection("exams")?.document(exam.id)?.set(exam)
        } catch (_: Exception) {}
    }

    fun deleteExam(examId: String) {
        _exams.value = _exams.value.filter { it.id != examId }
        try {
            firestore?.collection("exams")?.document(examId)?.delete()
        } catch (_: Exception) {}
    }

    fun saveStudentResults(newResults: List<StudentResult>) {
        if (newResults.isEmpty()) return
        val current = _results.value.toMutableList()
        newResults.forEach { newRes ->
            val index = current.indexOfFirst { it.examId == newRes.examId && it.studentId == newRes.studentId }
            if (index != -1) {
                current[index] = newRes
            } else {
                current.add(newRes)
            }
        }
        _results.value = current

        try {
            val batch = firestore?.batch()
            if (batch != null) {
                newResults.forEach { res ->
                    val docRef = firestore!!.collection("student_results").document(res.id)
                    batch.set(docRef, res)
                }
                batch.commit()
            }
        } catch (_: Exception) {}
    }

    companion object {
        fun calculateGrade(percentage: Double, isPassed: Boolean): String {
            if (!isPassed) return "রাসিব (F)"
            return when {
                percentage >= 80.0 -> "মুমতাজ (A+)"
                percentage >= 70.0 -> "জাইয়্যিদ জিদ্দান (A)"
                percentage >= 60.0 -> "জাইয়্যিদ (A-)"
                percentage >= 50.0 -> "মাকবুল (B)"
                percentage >= 33.0 -> "রাসিবের ঊর্ধ্বে (C)"
                else -> "রাসিব (F)"
            }
        }
    }
}
