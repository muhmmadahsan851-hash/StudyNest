package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.data.model.Deadline
import com.example.data.model.Note
import com.example.data.model.StudyLog
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StudyRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ).apply {
      // Configure local persistent disk cache for seamless offline access during commutes
      try {
        val settings = FirebaseFirestoreSettings.Builder()
          .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
          .build()
        firestoreSettings = settings
      } catch (_: Exception) {
        // Cache settings already applied or running
      }
    }
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google to access StudyNest data.")
  }

  // --- USER PROFILE & SETTINGS ---
  fun observeUserProfile(): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = snapshot.id) else it
            }
          } else null
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
          throw error
        }
    )
  }

  suspend fun ensureUserProfile(displayName: String, email: String?) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val snapshot = docRef.get().await()
    if (!snapshot.exists()) {
      val profile = hashMapOf(
        "userId" to uid,
        "displayName" to displayName,
        "email" to email,
        "dailyGoalMinutes" to 45L,
        "streakDays" to 1L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(profile).await()
    }
  }

  suspend fun updateDailyGoalMinutes(minutes: Long) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    docRef.update(
      mapOf(
        "dailyGoalMinutes" to minutes,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  // --- BOOKS ---
  fun observeBooks(): Flow<List<Book>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/books"
    emitAll(
      db.collection("users").document(uid).collection("books")
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { doc ->
            doc.toObject(Book::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = doc.id) else it
            }
          }
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun addBook(
    title: String,
    author: String,
    category: String,
    colorTag: String,
    chapterCount: Int
  ): String {
    val uid = requireUserId()
    val bookId = UUID.randomUUID().toString()
    val bookRef = db.collection("users").document(uid).collection("books").document(bookId)

    val bookData = hashMapOf(
      "id" to bookId,
      "userId" to uid,
      "title" to title,
      "author" to author,
      "category" to category,
      "colorTag" to colorTag,
      "totalChapters" to chapterCount.toLong(),
      "completedChapters" to 0L,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    val batch = db.batch()
    batch.set(bookRef, bookData)

    // Pre-create chapter documents
    val chaptersRef = bookRef.collection("chapters")
    for (i in 1..chapterCount) {
      val chapterId = UUID.randomUUID().toString()
      val chapterDoc = chaptersRef.document(chapterId)
      val chapterData = hashMapOf(
        "id" to chapterId,
        "bookId" to bookId,
        "chapterNumber" to i.toLong(),
        "title" to "Chapter $i",
        "isCompleted" to false,
        "pageCount" to 20L,
        "estimatedMinutes" to 30L,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      batch.set(chapterDoc, chapterData)
    }

    try {
      batch.commit().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, bookRef.path)
      throw e
    }
    return bookId
  }

  suspend fun updateBook(book: Book) {
    val uid = requireUserId()
    val bookRef = db.collection("users").document(uid).collection("books").document(book.id)
    try {
      bookRef.update(
        mapOf(
          "title" to book.title,
          "author" to book.author,
          "category" to book.category,
          "colorTag" to book.colorTag,
          "totalChapters" to book.totalChapters,
          "completedChapters" to book.completedChapters,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, bookRef.path)
      throw e
    }
  }

  suspend fun deleteBook(bookId: String) {
    val uid = requireUserId()
    val bookRef = db.collection("users").document(uid).collection("books").document(bookId)
    try {
      bookRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, bookRef.path)
      throw e
    }
  }

  // --- CHAPTERS ---
  fun observeChapters(bookId: String): Flow<List<Chapter>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/books/$bookId/chapters"
    emitAll(
      db.collection("users").document(uid).collection("books").document(bookId).collection("chapters")
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { doc ->
            doc.toObject(Chapter::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = doc.id) else it
            }
          }.sortedBy { it.chapterNumber }
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun addChapter(bookId: String, title: String, pageCount: Long, estimatedMinutes: Long) {
    val uid = requireUserId()
    val bookRef = db.collection("users").document(uid).collection("books").document(bookId)
    val chaptersColl = bookRef.collection("chapters")

    val existing = chaptersColl.get().await()
    val nextNum = (existing.size() + 1).toLong()
    val chapterId = UUID.randomUUID().toString()

    val chapterData = hashMapOf(
      "id" to chapterId,
      "bookId" to bookId,
      "chapterNumber" to nextNum,
      "title" to title,
      "isCompleted" to false,
      "pageCount" to pageCount,
      "estimatedMinutes" to estimatedMinutes,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    val batch = db.batch()
    batch.set(chaptersColl.document(chapterId), chapterData)
    batch.update(
      bookRef,
      mapOf(
        "totalChapters" to nextNum,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    )
    batch.commit().await()
  }

  suspend fun toggleChapterCompletion(bookId: String, chapterId: String, isCompleted: Boolean) {
    val uid = requireUserId()
    val bookRef = db.collection("users").document(uid).collection("books").document(bookId)
    val chapterRef = bookRef.collection("chapters").document(chapterId)

    val batch = db.batch()
    batch.update(
      chapterRef,
      mapOf(
        "isCompleted" to isCompleted,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    )

    // Recalculate completed chapters
    val allChapters = bookRef.collection("chapters").get().await().documents.mapNotNull { doc ->
      doc.toObject(Chapter::class.java)?.let {
        if (it.id.isBlank()) it.copy(id = doc.id) else it
      }
    }
    val completedCount = allChapters.count {
      if (it.id == chapterId) isCompleted else it.isCompleted
    }.toLong()

    batch.update(
      bookRef,
      mapOf(
        "completedChapters" to completedCount,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    )

    try {
      batch.commit().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, chapterRef.path)
      throw e
    }
  }

  // --- NOTES ---
  fun observeNotes(): Flow<List<Note>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/notes"
    emitAll(
      db.collection("users").document(uid).collection("notes")
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { doc ->
            doc.toObject(Note::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = doc.id) else it
            }
          }
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun addNote(bookId: String, chapterId: String, title: String, content: String, tag: String): String {
    val uid = requireUserId()
    val noteId = UUID.randomUUID().toString()
    val docRef = db.collection("users").document(uid).collection("notes").document(noteId)

    val payload = hashMapOf(
      "id" to noteId,
      "userId" to uid,
      "bookId" to bookId,
      "chapterId" to chapterId,
      "title" to title,
      "content" to content,
      "tag" to tag,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(payload).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
    return noteId
  }

  suspend fun updateNote(noteId: String, title: String, content: String, tag: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
    try {
      docRef.update(
        mapOf(
          "title" to title,
          "content" to content,
          "tag" to tag,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteNote(noteId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- DEADLINES & EXAMS ---
  fun observeDeadlines(): Flow<List<Deadline>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/deadlines"
    emitAll(
      db.collection("users").document(uid).collection("deadlines")
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { doc ->
            doc.toObject(Deadline::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = doc.id) else it
            }
          }.sortedBy { it.dueDateEpochMs }
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun addDeadline(title: String, bookTitle: String, dueDateEpochMs: Long, urgency: String): String {
    val uid = requireUserId()
    val deadlineId = UUID.randomUUID().toString()
    val docRef = db.collection("users").document(uid).collection("deadlines").document(deadlineId)

    val payload = hashMapOf(
      "id" to deadlineId,
      "userId" to uid,
      "title" to title,
      "bookTitle" to bookTitle,
      "dueDateEpochMs" to dueDateEpochMs,
      "isCompleted" to false,
      "urgency" to urgency,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(payload).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
    return deadlineId
  }

  suspend fun toggleDeadline(deadlineId: String, isCompleted: Boolean) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("deadlines").document(deadlineId)
    try {
      docRef.update(
        mapOf(
          "isCompleted" to isCompleted,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteDeadline(deadlineId: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("deadlines").document(deadlineId)
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- STUDY LOGS & TIME TRACKING ---
  fun observeStudyLogs(): Flow<List<StudyLog>> = flow {
    val uid = requireUserId()
    val path = "users/$uid/studyLogs"
    emitAll(
      db.collection("users").document(uid).collection("studyLogs")
        .snapshots()
        .map { snapshot ->
          snapshot.documents.mapNotNull { doc ->
            doc.toObject(StudyLog::class.java)?.let {
              if (it.id.isBlank()) it.copy(id = doc.id) else it
            }
          }
        }
        .catch { error ->
          if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
          throw error
        }
    )
  }

  suspend fun logStudySession(minutes: Long, category: String) {
    val uid = requireUserId()
    val logId = UUID.randomUUID().toString()
    val docRef = db.collection("users").document(uid).collection("studyLogs").document(logId)
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val payload = hashMapOf(
      "id" to logId,
      "userId" to uid,
      "dateStr" to todayStr,
      "minutesStudied" to minutes,
      "category" to category,
      "createdAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.set(payload).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }
}
