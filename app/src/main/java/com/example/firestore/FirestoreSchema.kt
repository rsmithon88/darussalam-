package com.example.firestore

/**
 * Firestore Data Schema Definition for Darussalam Madrassa Management System.
 *
 * This object defines collection names, document keys, field descriptors,
 * and structural guidelines for Firestore database interaction.
 */
object FirestoreSchema {

    object Collections {
        const val STUDENTS = "students"
        const val TEACHERS = "teachers"
        const val STUDENT_ATTENDANCE = "student_attendance"
        const val TEACHER_ATTENDANCE = "teacher_attendance"
        const val CLASSES = "classes"
        const val EXAMS = "exams"
        const val STUDENT_RESULTS = "student_results"
        const val FEE_RECORDS = "fee_records"
        const val EXPENSE_VOUCHERS = "expense_vouchers"
        const val NOTICES = "notices"
    }

    object StudentFields {
        const val ID = "id"
        const val NAME = "name"
        const val FATHER_NAME = "fatherName"
        const val MOTHER_NAME = "motherName"
        const val ROLL = "roll"
        const val JAMAT_CLASS = "jamatClass"
        const val AGE = "age"
        const val GUARDIAN_CONTACT = "guardianContact"
        const val ADDRESS = "address"
        const val GENDER = "gender"
        const val STATUS = "status"
        const val LAST_UPDATED_BY = "lastUpdatedBy"
        const val LAST_UPDATED_AT = "lastUpdatedAt"
    }

    object TeacherFields {
        const val ID = "id"
        const val PHOTO_URL = "photoUrl"
        const val NAME = "name"
        const val DESIGNATION = "designation"
        const val SUBJECT = "subject"
        const val ADDRESS = "address"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val GENDER = "gender"
        const val IS_SUPER_ADMIN = "isSuperAdmin"
        const val PERMISSIONS = "permissions"
        const val ASSIGNED_CLASSES = "assignedClasses"
    }

    object StudentAttendanceFields {
        const val ID = "id"
        const val DATE = "date"
        const val TIME = "time"
        const val JAMAT_CLASS = "jamatClass"
        const val STUDENT_ID = "studentId"
        const val STUDENT_NAME = "studentName"
        const val ROLL = "roll"
        const val STATUS = "status" // "উপস্থিত", "অনুপস্থিত", "ছুটি", "বিলম্ব"
        const val GUARDIAN_CONTACT = "guardianContact"
        const val MARKED_BY = "markedBy"
        const val TIMESTAMP = "timestamp"
    }

    object TeacherAttendanceFields {
        const val ID = "id"
        const val TEACHER_ID = "teacherId"
        const val TEACHER_NAME = "teacherName"
        const val DESIGNATION = "designation"
        const val DATE = "date"
        const val TIME = "time"
        const val STATUS = "status" // "উপস্থিত", "বিলম্ব", "অনুপস্থিত", "ছুটি"
        const val NOTE = "note"
    }

    /**
     * Helper to construct unique composite document ID for attendance records:
     * Format: YYYY-MM-DD_studentId
     */
    fun buildStudentAttendanceDocId(date: String, studentId: String): String {
        return "${date}_$studentId"
    }

    /**
     * Helper to construct unique composite document ID for teacher attendance records:
     * Format: YYYY-MM-DD_teacherId
     */
    fun buildTeacherAttendanceDocId(date: String, teacherId: String): String {
        return "${date}_$teacherId"
    }
}
