package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.ui.components.AddChapterDialog
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ProgressRing
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun BookDetailScreen(
  book: Book,
  viewModel: StudyViewModel,
  onBack: () -> Unit,
  onOpenNotes: (Chapter?) -> Unit,
  isDualPane: Boolean = false,
  modifier: Modifier = Modifier
) {
  val chapters by viewModel.chapters.collectAsState()
  var showAddChapterDialog by remember { mutableStateOf(false) }

  val accentColor = parseHexColor(book.colorTag)
  val completedChaptersCount = chapters.count { it.isCompleted }
  val totalChaptersCount = if (chapters.isNotEmpty()) chapters.size else book.totalChapters.toInt()
  val progressPercent = if (totalChaptersCount > 0) completedChaptersCount.toFloat() / totalChaptersCount.toFloat() else 0f

  Scaffold(
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      // Top Navigation / Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isDualPane) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Books")
          }
        }
        Spacer(modifier = Modifier.width(4.dp))
        CategoryBadge(categoryName = book.category, hexColor = book.colorTag)
      }

      // Book Banner Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          if (book.author.isNotBlank()) {
            Text(
              text = "By ${book.author}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Progress Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "$completedChaptersCount of $totalChaptersCount chapters done",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${(progressPercent * 100).toInt()}%",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = accentColor
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(CircleShape),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Quick Action buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledTonalButton(
              onClick = { showAddChapterDialog = true },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Chapter", fontSize = 12.sp)
            }

            Button(
              onClick = {
                viewModel.setTimerCategory(book.category)
                viewModel.startTimer()
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Study Timer", fontSize = 12.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Chapters Checklist",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Tap to complete",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(chapters, key = { it.id }) { chapter ->
          ChapterItemRow(
            chapter = chapter,
            accentColor = accentColor,
            onToggle = { isChecked ->
              viewModel.toggleChapter(book.id, chapter.id, isChecked)
            },
            onNotesClick = {
              viewModel.selectChapter(chapter)
              onOpenNotes(chapter)
            }
          )
        }
        item {
          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }

  if (showAddChapterDialog) {
    AddChapterDialog(
      onDismiss = { showAddChapterDialog = false },
      onConfirm = { title, pages, estMinutes ->
        viewModel.addChapterToBook(book.id, title, pages, estMinutes)
        showAddChapterDialog = false
      }
    )
  }
}

@Composable
fun ChapterItemRow(
  chapter: Chapter,
  accentColor: Color,
  onToggle: (Boolean) -> Unit,
  onNotesClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (chapter.isCompleted)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      else
        MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = chapter.isCompleted,
        onCheckedChange = onToggle,
        colors = CheckboxDefaults.colors(checkedColor = accentColor),
        modifier = Modifier.testTag("chapter_checkbox_${chapter.id}")
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Ch ${chapter.chapterNumber}: ${chapter.title}",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = if (chapter.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
          textDecoration = if (chapter.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          color = if (chapter.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${chapter.pageCount} pages • ~${chapter.estimatedMinutes} mins",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onNotesClick,
        modifier = Modifier.testTag("chapter_notes_btn_${chapter.id}")
      ) {
        Icon(
          imageVector = Icons.Default.EditNote,
          contentDescription = "Notes for chapter",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
