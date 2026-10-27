package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Book
import com.example.data.model.CategoryTag
import com.example.data.model.Chapter
import com.example.data.model.Deadline
import com.example.data.model.DefaultCategories
import com.example.data.model.Note
import com.example.data.model.StudyLog
import com.example.data.model.UserProfile
import com.example.data.repository.StudyRepository
import com.example.notification.StudyNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StudyViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = StudyRepository(application)
  private val context = application.applicationContext

  // User Profile
  val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile()
    .catch { emit(null) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Books
  val books: StateFlow<List<Book>> = repository.observeBooks()
    .catch { emit(emptyList()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Selected Book for Detail View
  private val _selectedBook = MutableStateFlow<Book?>(null)
  val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

  // Chapters for selected book
  private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
  val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

  // Selected Chapter for notes view
  private val _selectedChapter = MutableStateFlow<Chapter?>(null)
  val selectedChapter: StateFlow<Chapter?> = _selectedChapter.asStateFlow()

  // Notes
  val notes: StateFlow<List<Note>> = repository.observeNotes()
    .catch { emit(emptyList()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Deadlines
  val deadlines: StateFlow<List<Deadline>> = repository.observeDeadlines()
    .catch { emit(emptyList()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Study Logs
  val studyLogs: StateFlow<List<StudyLog>> = repository.observeStudyLogs()
    .catch { emit(emptyList()) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Category filter
  private val _selectedCategoryFilter = MutableStateFlow("All")
  val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

  // Dark mode setting: "system", "dark", "light"
  private val _themeMode = MutableStateFlow("system")
  val themeMode: StateFlow<String> = _themeMode.asStateFlow()

  // Study Timer / Stopwatch
  private val _isTimerRunning = MutableStateFlow(false)
  val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

  private val _timerSeconds = MutableStateFlow(0L)
  val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

  private val _timerCategory = MutableStateFlow(DefaultCategories.first().name)
  val timerCategory: StateFlow<String> = _timerCategory.asStateFlow()

  private var timerJob: Job? = null

  // UI status / banner message
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  fun clearToast() {
    _toastMessage.value = null
  }

  fun setCategoryFilter(category: String) {
    _selectedCategoryFilter.value = category
  }

  fun setThemeMode(mode: String) {
    _themeMode.value = mode
  }

  fun selectBook(book: Book?) {
    _selectedBook.value = book
    _selectedChapter.value = null
    if (book != null) {
      observeChaptersForBook(book.id)
    } else {
      _chapters.value = emptyList()
    }
  }

  fun selectChapter(chapter: Chapter?) {
    _selectedChapter.value = chapter
  }

  private fun observeChaptersForBook(bookId: String) {
    viewModelScope.launch {
      repository.observeChapters(bookId)
        .catch { emit(emptyList()) }
        .collect {
          _chapters.value = it
        }
    }
  }

  // --- ACTIONS ---

  fun initializeUser(displayName: String, email: String?) {
    viewModelScope.launch {
      try {
        repository.ensureUserProfile(displayName, email)
      } catch (_: Exception) {}
    }
  }

  fun addBook(title: String, author: String, category: String, colorTag: String, chaptersCount: Int) {
    viewModelScope.launch {
      try {
        val bookId = repository.addBook(title, author, category, colorTag, chaptersCount)
        _toastMessage.value = "Book '$title' added with $chaptersCount chapters!"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to add book: ${e.message}"
      }
    }
  }

  fun deleteBook(bookId: String) {
    viewModelScope.launch {
      try {
        repository.deleteBook(bookId)
        if (_selectedBook.value?.id == bookId) {
          _selectedBook.value = null
          _chapters.value = emptyList()
        }
        _toastMessage.value = "Book deleted"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to delete: ${e.message}"
      }
    }
  }

  fun addChapterToBook(bookId: String, title: String, pages: Long, estMinutes: Long) {
    viewModelScope.launch {
      try {
        repository.addChapter(bookId, title, pages, estMinutes)
        _toastMessage.value = "Chapter added!"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to add chapter: ${e.message}"
      }
    }
  }

  fun toggleChapter(bookId: String, chapterId: String, completed: Boolean) {
    viewModelScope.launch {
      try {
        repository.toggleChapterCompletion(bookId, chapterId, completed)
      } catch (e: Exception) {
        _toastMessage.value = "Failed to update: ${e.message}"
      }
    }
  }

  fun addNote(bookId: String, chapterId: String, title: String, content: String, tag: String) {
    viewModelScope.launch {
      try {
        repository.addNote(bookId, chapterId, title, content, tag)
        _toastMessage.value = "Note saved!"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to save note: ${e.message}"
      }
    }
  }

  fun deleteNote(noteId: String) {
    viewModelScope.launch {
      try {
        repository.deleteNote(noteId)
        _toastMessage.value = "Note deleted"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to delete note: ${e.message}"
      }
    }
  }

  fun addDeadline(title: String, bookTitle: String, dueDateEpochMs: Long, urgency: String) {
    viewModelScope.launch {
      try {
        val deadlineId = repository.addDeadline(title, bookTitle, dueDateEpochMs, urgency)
        _toastMessage.value = "Deadline '$title' added!"
        // Schedule local alert notification for this deadline
        val daysRemaining = if (dueDateEpochMs > System.currentTimeMillis()) {
          (dueDateEpochMs - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
        } else 0
        StudyNotificationHelper.showNotification(
          context,
          deadlineId.hashCode(),
          "New Exam/Deadline Added: $title",
          "Target date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dueDateEpochMs))} ($daysRemaining days remaining)."
        )
      } catch (e: Exception) {
        _toastMessage.value = "Failed to add deadline: ${e.message}"
      }
    }
  }

  fun toggleDeadline(deadlineId: String, isCompleted: Boolean) {
    viewModelScope.launch {
      try {
        repository.toggleDeadline(deadlineId, isCompleted)
      } catch (e: Exception) {
        _toastMessage.value = "Failed to update deadline: ${e.message}"
      }
    }
  }

  fun deleteDeadline(deadlineId: String) {
    viewModelScope.launch {
      try {
        repository.deleteDeadline(deadlineId)
        _toastMessage.value = "Deadline deleted"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to delete: ${e.message}"
      }
    }
  }

  fun updateDailyGoal(minutes: Long) {
    viewModelScope.launch {
      try {
        repository.updateDailyGoalMinutes(minutes)
        _toastMessage.value = "Daily goal updated to $minutes minutes"
      } catch (e: Exception) {
        _toastMessage.value = "Failed to update goal: ${e.message}"
      }
    }
  }

  // --- TIMER CONTROLS ---

  fun setTimerCategory(category: String) {
    _timerCategory.value = category
  }

  fun startTimer() {
    if (_isTimerRunning.value) return
    _isTimerRunning.value = true
    timerJob = viewModelScope.launch {
      while (_isTimerRunning.value) {
        delay(1000)
        _timerSeconds.value += 1
      }
    }
  }

  fun pauseTimer() {
    _isTimerRunning.value = false
    timerJob?.cancel()
  }

  fun resetTimer() {
    pauseTimer()
    _timerSeconds.value = 0L
  }

  fun saveTimerSession() {
    val totalSec = _timerSeconds.value
    if (totalSec < 30) {
      _toastMessage.value = "Session too short to record (< 30s)"
      resetTimer()
      return
    }
    val minutes = (totalSec + 30) / 60
    val category = _timerCategory.value
    viewModelScope.launch {
      try {
        repository.logStudySession(minutes, category)
        _toastMessage.value = "Recorded $minutes minutes of study for $category!"

        val target = userProfile.value?.dailyGoalMinutes ?: 45L
        val todayLogs = getTodayStudyMinutes()
        if (todayLogs + minutes >= target) {
          StudyNotificationHelper.showNotification(
            context,
            9999,
            "Daily Study Goal Accomplished! 🎓",
            "Great job! You achieved ${todayLogs + minutes} minutes of studying today."
          )
        }
      } catch (e: Exception) {
        _toastMessage.value = "Failed to log session: ${e.message}"
      }
      resetTimer()
    }
  }

  // --- STATS CALCULATIONS ---

  fun getTodayStudyMinutes(): Long {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return studyLogs.value.filter { it.dateStr == today }.sumOf { it.minutesStudied }
  }

  fun getWeeklyStudyData(): List<Pair<String, Long>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val cal = Calendar.getInstance()
    val list = mutableListOf<Pair<String, Long>>()

    for (i in 6 downTo 0) {
      val tempCal = Calendar.getInstance()
      tempCal.add(Calendar.DAY_OF_YEAR, -i)
      val dateStr = sdf.format(tempCal.time)
      val label = dayFormat.format(tempCal.time)
      val minutes = studyLogs.value.filter { it.dateStr == dateStr }.sumOf { it.minutesStudied }
      list.add(label to minutes)
    }
    return list
  }
}
