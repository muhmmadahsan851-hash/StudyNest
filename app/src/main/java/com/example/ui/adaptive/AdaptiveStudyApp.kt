package com.example.ui.adaptive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.BookListScreen
import com.example.ui.screens.ChapterNotesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeadlineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.StudyViewModel

enum class StudyDestination(val title: String, val icon: ImageVector) {
  DASHBOARD("Dashboard", Icons.Default.Dashboard),
  BOOKS("Books", Icons.Default.Book),
  DEADLINES("Deadlines", Icons.Default.Alarm),
  SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AdaptiveStudyApp(
  viewModel: StudyViewModel,
  onSignOut: () -> Unit
) {
  var currentDestination by remember { mutableStateOf(StudyDestination.DASHBOARD) }
  val selectedBook by viewModel.selectedBook.collectAsState()
  val selectedChapter by viewModel.selectedChapter.collectAsState()
  var isViewingNotes by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val toastMessage by viewModel.toastMessage.collectAsState()

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearToast()
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTablet = maxWidth >= 720.dp

    if (isTablet) {
      Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
          modifier = Modifier.fillMaxHeight(),
          containerColor = MaterialTheme.colorScheme.surface,
          header = {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.School,
                  contentDescription = "StudyNest",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "StudyNest",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        ) {
          StudyDestination.values().forEach { destination ->
            NavigationRailItem(
              icon = { Icon(destination.icon, contentDescription = destination.title) },
              label = { Text(destination.title) },
              selected = currentDestination == destination,
              onClick = {
                currentDestination = destination
                if (destination != StudyDestination.BOOKS) {
                  viewModel.selectBook(null)
                  isViewingNotes = false
                }
              },
              modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
        ) {
          when (currentDestination) {
            StudyDestination.DASHBOARD -> {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
                contentAlignment = Alignment.TopCenter
              ) {
                Box(modifier = Modifier.widthIn(max = 840.dp)) {
                  DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToBooks = { currentDestination = StudyDestination.BOOKS },
                    onNavigateToDeadlines = { currentDestination = StudyDestination.DEADLINES }
                  )
                }
              }
            }

            StudyDestination.BOOKS -> {
              Row(modifier = Modifier.fillMaxSize()) {
                Box(
                  modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .padding(8.dp)
                ) {
                  BookListScreen(
                    viewModel = viewModel,
                    onBookClick = { book ->
                      viewModel.selectBook(book)
                      isViewingNotes = false
                    }
                  )
                }

                Surface(
                  modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(),
                  color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {}

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                ) {
                  if (selectedBook != null) {
                    if (isViewingNotes) {
                      ChapterNotesScreen(
                        viewModel = viewModel,
                        book = selectedBook,
                        chapter = selectedChapter,
                        onBack = { isViewingNotes = false },
                        isDualPane = true
                      )
                    } else {
                      BookDetailScreen(
                        book = selectedBook!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectBook(null) },
                        onOpenNotes = { isViewingNotes = true },
                        isDualPane = true
                      )
                    }
                  } else {
                    Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.Center
                    ) {
                      Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.MenuBook,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                          modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                          text = "Select a book to view chapters",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Browse through chapters, mark progress, and author study notes.",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }
                  }
                }
              }
            }

            StudyDestination.DEADLINES -> {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
                contentAlignment = Alignment.TopCenter
              ) {
                Box(modifier = Modifier.widthIn(max = 800.dp)) {
                  DeadlineScreen(viewModel = viewModel)
                }
              }
            }

            StudyDestination.SETTINGS -> {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
                contentAlignment = Alignment.TopCenter
              ) {
                Box(modifier = Modifier.widthIn(max = 700.dp)) {
                  SettingsScreen(viewModel = viewModel, onSignOut = onSignOut)
                }
              }
            }
          }

          SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(16.dp)
          )
        }
      }
    } else {
      BackHandler(enabled = isViewingNotes || selectedBook != null) {
        if (isViewingNotes) {
          isViewingNotes = false
        } else if (selectedBook != null) {
          viewModel.selectBook(null)
        }
      }

      Scaffold(
        bottomBar = {
          if (selectedBook == null && !isViewingNotes) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface
            ) {
              StudyDestination.values().forEach { destination ->
                NavigationBarItem(
                  icon = { Icon(destination.icon, contentDescription = destination.title) },
                  label = { Text(destination.title) },
                  selected = currentDestination == destination,
                  onClick = {
                    currentDestination = destination
                  },
                  modifier = Modifier.testTag("nav_bar_${destination.name.lowercase()}")
                )
              }
            }
          }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
      ) { padding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
          when (currentDestination) {
            StudyDestination.DASHBOARD -> {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToBooks = { currentDestination = StudyDestination.BOOKS },
                onNavigateToDeadlines = { currentDestination = StudyDestination.DEADLINES }
              )
            }

            StudyDestination.BOOKS -> {
              if (selectedBook != null) {
                if (isViewingNotes) {
                  ChapterNotesScreen(
                    viewModel = viewModel,
                    book = selectedBook,
                    chapter = selectedChapter,
                    onBack = { isViewingNotes = false }
                  )
                } else {
                  BookDetailScreen(
                    book = selectedBook!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectBook(null) },
                    onOpenNotes = { isViewingNotes = true }
                  )
                }
              } else {
                BookListScreen(
                  viewModel = viewModel,
                  onBookClick = { book -> viewModel.selectBook(book) }
                )
              }
            }

            StudyDestination.DEADLINES -> {
              DeadlineScreen(viewModel = viewModel)
            }

            StudyDestination.SETTINGS -> {
              SettingsScreen(viewModel = viewModel, onSignOut = onSignOut)
            }
          }
        }
      }
    }
  }
}
