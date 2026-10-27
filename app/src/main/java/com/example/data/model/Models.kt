package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String? = null,
  val dailyGoalMinutes: Long = 45L,
  val streakDays: Long = 1L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Book(
  val id: String = "",
  val userId: String = "",
  val title: String = "",
  val author: String = "",
  val category: String = "General",
  val colorTag: String = "#4F46E5",
  val totalChapters: Long = 0L,
  val completedChapters: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val progressPercent: Float
    get() = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f) else 0f
}

@IgnoreExtraProperties
data class Chapter(
  val id: String = "",
  val bookId: String = "",
  val chapterNumber: Long = 1L,
  val title: String = "",
  val isCompleted: Boolean = false,
  val pageCount: Long = 0L,
  val estimatedMinutes: Long = 30L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Note(
  val id: String = "",
  val userId: String = "",
  val bookId: String = "",
  val chapterId: String = "",
  val title: String = "",
  val content: String = "",
  val tag: String = "Summary", // Key Concept, Definition, Formula, Question, Summary
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Deadline(
  val id: String = "",
  val userId: String = "",
  val title: String = "",
  val bookTitle: String = "",
  val dueDateEpochMs: Long = 0L,
  val isCompleted: Boolean = false,
  val urgency: String = "medium", // low, medium, high
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val isOverdue: Boolean
    get() = !isCompleted && dueDateEpochMs > 0 && dueDateEpochMs < System.currentTimeMillis()

  val daysRemaining: Long
    get() {
      val diff = dueDateEpochMs - System.currentTimeMillis()
      return if (diff > 0) (diff / (1000 * 60 * 60 * 24)) else 0L
    }
}

@IgnoreExtraProperties
data class StudyLog(
  val id: String = "",
  val userId: String = "",
  val dateStr: String = "", // e.g. "2026-10-27"
  val minutesStudied: Long = 0L,
  val category: String = "General",
  @ServerTimestamp val createdAt: Timestamp? = null
)

data class CategoryTag(
  val name: String,
  val hexColor: String
)

val DefaultCategories = listOf(
  CategoryTag("Computer Science", "#4F46E5"),
  CategoryTag("Mathematics", "#10B981"),
  CategoryTag("Physics & Science", "#F59E0B"),
  CategoryTag("Literature", "#F43F5E"),
  CategoryTag("History", "#8B5CF6"),
  CategoryTag("General", "#06B6D4")
)
