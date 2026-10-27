package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ProgressRing
import com.example.ui.components.StudyTimerCard
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: StudyViewModel,
  onNavigateToBooks: () -> Unit,
  onNavigateToDeadlines: () -> Unit,
  modifier: Modifier = Modifier
) {
  val userProfile by viewModel.userProfile.collectAsState()
  val books by viewModel.books.collectAsState()
  val deadlines by viewModel.deadlines.collectAsState()
  val isTimerRunning by viewModel.isTimerRunning.collectAsState()
  val timerSeconds by viewModel.timerSeconds.collectAsState()
  val timerCategory by viewModel.timerCategory.collectAsState()

  val todayMinutes = viewModel.getTodayStudyMinutes()
  val targetMinutes = userProfile?.dailyGoalMinutes ?: 45L
  val goalProgress = (todayMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)

  val totalChapters = books.sumOf { it.totalChapters }
  val completedChapters = books.sumOf { it.completedChapters }
  val overallChapterProgress = if (totalChapters > 0) (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f) else 0f

  val weeklyData = viewModel.getWeeklyStudyData()
  val upcomingDeadline = deadlines.filter { !it.isCompleted }.minByOrNull { it.dueDateEpochMs }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Header Card
      HeaderSection(
        userName = userProfile?.displayName ?: "Scholar",
        streakDays = userProfile?.streakDays ?: 1L
      )
    }

    item {
      // Primary Metrics: Today's Goal & Total Chapter Progress
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricCard(
          title = "Today's Study Goal",
          value = "$todayMinutes / $targetMinutes m",
          subtitle = if (goalProgress >= 1f) "Goal Reached! 🌟" else "${(targetMinutes - todayMinutes).coerceAtLeast(0)}m remaining",
          progress = goalProgress,
          progressColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )

        MetricCard(
          title = "Chapters Done",
          value = "$completedChapters / $totalChapters",
          subtitle = "${(overallChapterProgress * 100).toInt()}% completed",
          progress = overallChapterProgress,
          progressColor = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      // Study Session Stopwatch
      StudyTimerCard(
        isTimerRunning = isTimerRunning,
        timerSeconds = timerSeconds,
        timerCategory = timerCategory,
        onStart = { viewModel.startTimer() },
        onPause = { viewModel.pauseTimer() },
        onReset = { viewModel.resetTimer() },
        onSave = { viewModel.saveTimerSession() },
        onCategorySelected = { viewModel.setTimerCategory(it) }
      )
    }

    item {
      // Weekly Learning Analytics Chart
      WeeklyStudyChart(weeklyData = weeklyData)
    }

    if (upcomingDeadline != null) {
      item {
        // Nearest Deadline Countdown Banner
        UpcomingDeadlineBanner(
          deadline = upcomingDeadline,
          onViewAll = onNavigateToDeadlines
        )
      }
    }

    item {
      // Quick Overview of Books
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Materials (${books.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "View All",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.clickable { onNavigateToBooks() }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun HeaderSection(userName: String, streakDays: Long) {
  val dateFormatted = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = "Hello, $userName 👋",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = dateFormatted,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.primaryContainer,
      modifier = Modifier.padding(4.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.LocalFireDepartment,
          contentDescription = "Streak",
          tint = Color(0xFFF59E0B),
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "$streakDays Day Streak",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  progress: Float,
  progressColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(CircleShape),
        color = progressColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun WeeklyStudyChart(weeklyData: List<Pair<String, Long>>) {
  val maxMinutes = (weeklyData.maxOfOrNull { it.second } ?: 60L).coerceAtLeast(60L).toFloat()
  val barColor = MaterialTheme.colorScheme.primary
  val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

  Card(
    modifier = Modifier.fillMaxWidth(),
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
        Column {
          Text(
            text = "Learning Statistics (Last 7 Days)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Total: ${weeklyData.sumOf { it.second }} minutes studied",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Custom Compose Bar Chart
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val canvasWidth = size.width
          val canvasHeight = size.height - 24.dp.toPx() // leave room for labels
          val barWidth = 24.dp.toPx()
          val step = canvasWidth / weeklyData.size

          weeklyData.forEachIndexed { index, (dayLabel, minutes) ->
            val x = (index * step) + (step / 2) - (barWidth / 2)
            val barHeight = if (maxMinutes > 0) (minutes.toFloat() / maxMinutes) * canvasHeight else 0f
            val y = canvasHeight - barHeight

            // Background placeholder bar
            drawRoundRect(
              color = emptyBarColor,
              topLeft = Offset(x, 0f),
              size = Size(barWidth, canvasHeight),
              cornerRadius = CornerRadius(8.dp.toPx())
            )

            // Active study minutes bar
            if (barHeight > 4f) {
              drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx())
              )
            }
          }
        }

        // Labels row below canvas
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          weeklyData.forEach { (label, minutes) ->
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
              )
              Text(
                text = "${minutes}m",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun UpcomingDeadlineBanner(
  deadline: com.example.data.model.Deadline,
  onViewAll: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onViewAll() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Event,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Upcoming Exam / Deadline",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = deadline.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${deadline.bookTitle} • ${deadline.daysRemaining} days remaining",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = "View",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
