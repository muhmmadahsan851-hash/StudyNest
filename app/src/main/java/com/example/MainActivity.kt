package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notification.StudyNotificationHelper
import com.example.ui.adaptive.AdaptiveStudyApp
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.StudyNestTheme
import com.example.ui.viewmodel.StudyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Create notification channel for deadlines & study goals
    StudyNotificationHelper.createNotificationChannel(this)

    setContent {
      var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

      // Listen for Auth state changes
      DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
          currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
          Firebase.auth.removeAuthStateListener(listener)
        }
      }

      if (currentUser == null) {
        StudyNestTheme {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            AuthScreen(
              onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
              }
            )
          }
        }
      } else {
        // Authenticated Study Session
        AuthenticatedContent(
          user = currentUser!!,
          onSignOut = {
            currentUser = null
          }
        )
      }
    }
  }
}

@Composable
fun AuthenticatedContent(
  user: com.google.firebase.auth.FirebaseUser,
  onSignOut: () -> Unit
) {
  val viewModel: StudyViewModel = viewModel()
  val themeMode by viewModel.themeMode.collectAsState()

  // Initialize profile in Firestore
  LaunchedEffect(user.uid) {
    viewModel.initializeUser(
      displayName = user.displayName ?: "Scholar",
      email = user.email
    )
  }

  // Request notification permissions on Android 13+
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Granted or denied */ }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      // Prompt for notifications if not granted
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  val isDark = when (themeMode) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
  }

  StudyNestTheme(darkTheme = isDark) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      AdaptiveStudyApp(
        viewModel = viewModel,
        onSignOut = onSignOut
      )
    }
  }
}
