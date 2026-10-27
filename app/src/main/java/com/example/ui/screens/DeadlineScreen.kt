package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Deadline
import com.example.ui.components.AddDeadlineDialog
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeadlineScreen(
  viewModel: StudyViewModel,
  modifier: Modifier = Modifier
) {
  val deadlines by viewModel.deadlines.collectAsState()
  val books by viewModel.books.collectAsState()
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Upcoming, 1: Completed, 2: All
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredDeadlines = when (selectedTab) {
    0 -> deadlines.filter { !it.isCompleted }
    1 -> deadlines.filter { it.isCompleted }
    else -> deadlines
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("add_deadline_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Deadline")
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
      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Exam & Assignment Deadlines",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Automated reminders keep you prepared before exam dates.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(14.dp))

      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Upcoming (${deadlines.count { !it.isCompleted }})") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Completed (${deadlines.count { it.isCompleted }})") }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("All (${deadlines.size})") }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (filteredDeadlines.isEmpty()) {
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
              imageVector = Icons.Default.Alarm,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (selectedTab == 0) "No upcoming deadlines 🎉" else "No deadlines here",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Add target exam dates, quizzes, or project deadlines to receive timely notifications.",
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
          items(filteredDeadlines, key = { it.id }) { deadline ->
            DeadlineCard(
              deadline = deadline,
              onToggle = { isChecked ->
                viewModel.toggleDeadline(deadline.id, isChecked)
              },
              onDelete = {
                viewModel.deleteDeadline(deadline.id)
              }
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
    AddDeadlineDialog(
      books = books,
      onDismiss = { showAddDialog = false },
      onConfirm = { title, bookTitle, epoch, urgency ->
        viewModel.addDeadline(title, bookTitle, epoch, urgency)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun DeadlineCard(
  deadline: Deadline,
  onToggle: (Boolean) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(deadline.dueDateEpochMs))
  val urgencyColor = when (deadline.urgency) {
    "high" -> Color(0xFFF43F5E)
    "medium" -> Color(0xFFF59E0B)
    else -> Color(0xFF10B981)
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (deadline.isCompleted)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      else
        MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = deadline.isCompleted,
        onCheckedChange = onToggle,
        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.testTag("deadline_checkbox_${deadline.id}")
      )

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = urgencyColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = deadline.urgency.uppercase(Locale.getDefault()),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = urgencyColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontSize = 10.sp
            )
          }

          if (deadline.bookTitle.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = deadline.bookTitle,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = deadline.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          textDecoration = if (deadline.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          color = if (deadline.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "Due: $dateFormatted (${deadline.daysRemaining} days remaining)",
          style = MaterialTheme.typography.bodySmall,
          color = if (deadline.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onDelete) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Deadline",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
