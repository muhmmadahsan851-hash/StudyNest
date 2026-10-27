package com.example.ui.components

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryTag
import com.example.data.model.Chapter
import com.example.data.model.DefaultCategories

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF4F46E5)): Color {
  return try {
    val clean = hex.removePrefix("#")
    if (clean.length == 6) {
      Color(android.graphics.Color.parseColor("#$clean"))
    } else if (clean.length == 8) {
      Color(android.graphics.Color.parseColor("#$clean"))
    } else {
      defaultColor
    }
  } catch (_: Exception) {
    defaultColor
  }
}

@Composable
fun CategoryBadge(
  categoryName: String,
  hexColor: String,
  modifier: Modifier = Modifier
) {
  val tagColor = parseHexColor(hexColor)
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = tagColor.copy(alpha = 0.15f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(tagColor)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = categoryName,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = tagColor
      )
    }
  }
}

@Composable
fun ProgressRing(
  progress: Float,
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 6.dp,
  color: Color = MaterialTheme.colorScheme.primary,
  trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
  centerContent: @Composable () -> Unit = {}
) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      progress = { progress },
      modifier = Modifier.size(52.dp),
      strokeWidth = strokeWidth,
      color = color,
      trackColor = trackColor,
      strokeCap = StrokeCap.Round
    )
    centerContent()
  }
}

@Composable
fun StudyTimerCard(
  isTimerRunning: Boolean,
  timerSeconds: Long,
  timerCategory: String,
  onStart: () -> Unit,
  onPause: () -> Unit,
  onReset: () -> Unit,
  onSave: () -> Unit,
  onCategorySelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val minutes = timerSeconds / 60
  val seconds = timerSeconds % 60
  val timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Study Stopwatch",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Study Session Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        CategoryBadge(
          categoryName = timerCategory,
          hexColor = DefaultCategories.find { it.name == timerCategory }?.hexColor ?: "#4F46E5"
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = timeText,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary,
          letterSpacing = 2.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (isTimerRunning) {
            FilledTonalButton(
              onClick = onPause,
              modifier = Modifier.testTag("pause_timer_btn")
            ) {
              Icon(Icons.Default.Pause, contentDescription = "Pause")
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pause")
            }
          } else {
            Button(
              onClick = onStart,
              modifier = Modifier.testTag("start_timer_btn")
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = "Start")
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (timerSeconds > 0) "Resume" else "Start")
            }
          }

          if (timerSeconds > 0) {
            IconButton(onClick = onReset) {
              Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
            }

            Button(
              onClick = onSave,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
              ),
              modifier = Modifier.testTag("save_timer_btn")
            ) {
              Icon(Icons.Default.Save, contentDescription = "Save Session")
              Spacer(modifier = Modifier.width(4.dp))
              Text("Log")
            }
          }
        }
      }
    }
  }
}

// --- DIALOGS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, author: String, category: String, colorTag: String, chaptersCount: Int) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var author by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(DefaultCategories.first()) }
  var chaptersCountText by remember { mutableStateOf("10") }
  var expandedCategory by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add New Book / Material") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Book Title") },
          placeholder = { Text("e.g. Modern Operating Systems") },
          modifier = Modifier.fillMaxWidth().testTag("book_title_input")
        )

        OutlinedTextField(
          value = author,
          onValueChange = { author = it },
          label = { Text("Author / Professor") },
          placeholder = { Text("e.g. Andrew S. Tanenbaum") },
          modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
          expanded = expandedCategory,
          onExpandedChange = { expandedCategory = !expandedCategory }
        ) {
          OutlinedTextField(
            value = selectedCategory.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = expandedCategory,
            onDismissRequest = { expandedCategory = false }
          ) {
            DefaultCategories.forEach { category ->
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(category.hexColor))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(category.name)
                  }
                },
                onClick = {
                  selectedCategory = category
                  expandedCategory = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = chaptersCountText,
          onValueChange = { if (it.all { ch -> ch.isDigit() }) chaptersCountText = it },
          label = { Text("Number of Chapters") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val count = chaptersCountText.toIntOrNull()?.coerceIn(1, 500) ?: 10
            onConfirm(title.trim(), author.trim(), selectedCategory.name, selectedCategory.hexColor, count)
          }
        },
        enabled = title.isNotBlank(),
        modifier = Modifier.testTag("confirm_add_book_btn")
      ) {
        Text("Add Book")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AddChapterDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, pages: Long, estMinutes: Long) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var pagesText by remember { mutableStateOf("25") }
  var estMinutesText by remember { mutableStateOf("30") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Chapter") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Chapter Title") },
          placeholder = { Text("e.g. Memory Management & Paging") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = pagesText,
          onValueChange = { if (it.all { ch -> ch.isDigit() }) pagesText = it },
          label = { Text("Page Count") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = estMinutesText,
          onValueChange = { if (it.all { ch -> ch.isDigit() }) estMinutesText = it },
          label = { Text("Estimated Study Minutes") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val pages = pagesText.toLongOrNull() ?: 20L
            val est = estMinutesText.toLongOrNull() ?: 30L
            onConfirm(title.trim(), pages, est)
          }
        },
        enabled = title.isNotBlank()
      ) {
        Text("Save Chapter")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AddDeadlineDialog(
  books: List<com.example.data.model.Book>,
  onDismiss: () -> Unit,
  onConfirm: (title: String, bookTitle: String, dueDateEpochMs: Long, urgency: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var selectedBookTitle by remember { mutableStateOf(books.firstOrNull()?.title ?: "General") }
  var daysFromNowText by remember { mutableStateOf("7") }
  var urgency by remember { mutableStateOf("medium") } // low, medium, high

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Exam / Deadline Reminder") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Exam or Assignment Name") },
          placeholder = { Text("e.g. Midterm Exam - Algorithms") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = selectedBookTitle,
          onValueChange = { selectedBookTitle = it },
          label = { Text("Subject / Book Reference") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = daysFromNowText,
          onValueChange = { if (it.all { ch -> ch.isDigit() }) daysFromNowText = it },
          label = { Text("Days Until Due / Exam") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Priority / Urgency:", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("low" to "Low", "medium" to "Medium", "high" to "High").forEach { (key, label) ->
            val isSelected = urgency == key
            FilterChip(
              selected = isSelected,
              onClick = { urgency = key },
              label = { Text(label) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            val days = daysFromNowText.toLongOrNull() ?: 7L
            val epoch = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
            onConfirm(title.trim(), selectedBookTitle.trim(), epoch, urgency)
          }
        },
        enabled = title.isNotBlank()
      ) {
        Text("Set Reminder")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNoteDialog(
  chapters: List<Chapter>,
  onDismiss: () -> Unit,
  onConfirm: (chapterId: String, title: String, content: String, tag: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var selectedChapterId by remember { mutableStateOf(chapters.firstOrNull()?.id ?: "") }
  var selectedTag by remember { mutableStateOf("Key Concept") }

  val tagOptions = listOf("Key Concept", "Definition", "Formula", "Question", "Summary")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Chapter Study Note") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Note Title") },
          placeholder = { Text("e.g. Master Theorem Notes") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Tag Type:", style = MaterialTheme.typography.labelSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          tagOptions.forEach { tag ->
            val isSelected = selectedTag == tag
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { selectedTag = tag }
                .padding(bottom = 4.dp)
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Note Content / Takeaways") },
          minLines = 4,
          maxLines = 8,
          placeholder = { Text("Write formulas, proofs, or summary points here...") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(selectedChapterId, title.trim(), content.trim(), selectedTag)
          }
        },
        enabled = title.isNotBlank()
      ) {
        Text("Save Note")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun FilterChip(
  selected: Boolean,
  onClick: () -> Unit,
  label: @Composable () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
      label()
    }
  }
}
