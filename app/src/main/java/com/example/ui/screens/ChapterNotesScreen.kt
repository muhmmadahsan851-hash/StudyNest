package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.data.model.Note
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.FilterChip
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun ChapterNotesScreen(
  viewModel: StudyViewModel,
  book: Book?,
  chapter: Chapter?,
  onBack: () -> Unit,
  isDualPane: Boolean = false,
  modifier: Modifier = Modifier
) {
  val notes by viewModel.notes.collectAsState()
  val chapters by viewModel.chapters.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }
  var selectedTagFilter by remember { mutableStateOf("All") }

  val tagOptions = listOf("All", "Key Concept", "Definition", "Formula", "Question", "Summary")

  // Filter notes: if a book is selected, filter by book; if a chapter is selected, filter by chapter
  val filteredNotes = notes.filter { note ->
    val matchesBook = book == null || note.bookId == book.id
    val matchesChapter = chapter == null || note.chapterId == chapter.id
    val matchesTag = selectedTagFilter == "All" || note.tag == selectedTagFilter
    matchesBook && matchesChapter && matchesTag
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("add_note_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Note")
      }
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isDualPane) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
          Text(
            text = if (chapter != null) "Notes: Ch ${chapter.chapterNumber}" else (book?.title ?: "Study Notes"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          if (book != null && chapter != null) {
            Text(
              text = "${book.title} • ${chapter.title}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Tag filter row
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(tagOptions) { tag ->
          FilterChip(
            selected = selectedTagFilter == tag,
            onClick = { selectedTagFilter = tag },
            label = { Text(tag) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (filteredNotes.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.NoteAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "No notes found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap the + button to capture key definitions, formulas, or chapter summaries.",
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f)
        ) {
          items(filteredNotes, key = { it.id }) { note ->
            NoteCard(
              note = note,
              onDelete = { viewModel.deleteNote(note.id) }
            )
          }
          item {
            Spacer(modifier = Modifier.height(72.dp))
          }
        }
      }
    }
  }

  if (showAddDialog) {
    val availableChapters = if (chapters.isNotEmpty()) chapters else listOf(
      Chapter(id = "ch_general", title = "General Notes", chapterNumber = 1)
    )
    AddNoteDialog(
      chapters = availableChapters,
      onDismiss = { showAddDialog = false },
      onConfirm = { chapterId, title, content, tag ->
        val targetBookId = book?.id ?: "general_book"
        viewModel.addNote(targetBookId, chapterId, title, content, tag)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun NoteCard(
  note: Note,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = note.tag,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        IconButton(onClick = onDelete) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Note",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = note.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      if (note.content.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = note.content,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
