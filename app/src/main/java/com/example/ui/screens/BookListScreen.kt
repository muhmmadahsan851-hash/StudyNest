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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.DefaultCategories
import com.example.ui.components.AddBookDialog
import com.example.ui.components.CategoryBadge
import com.example.ui.components.FilterChip
import com.example.ui.components.ProgressRing
import com.example.ui.components.parseHexColor
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun BookListScreen(
  viewModel: StudyViewModel,
  onBookClick: (Book) -> Unit,
  modifier: Modifier = Modifier
) {
  val books by viewModel.books.collectAsState()
  val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredBooks = books.filter { book ->
    val matchesCategory = selectedCategory == "All" || book.category == selectedCategory
    val matchesSearch = searchQuery.isBlank() ||
      book.title.contains(searchQuery, ignoreCase = true) ||
      book.author.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("add_book_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Book")
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

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search books, authors, courses...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().testTag("search_books_input"),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(12.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          FilterChip(
            selected = selectedCategory == "All",
            onClick = { viewModel.setCategoryFilter("All") },
            label = { Text("All Books (${books.size})") }
          )
        }
        items(DefaultCategories) { cat ->
          val isSelected = selectedCategory == cat.name
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) parseHexColor(cat.hexColor) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { viewModel.setCategoryFilter(cat.name) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) Color.White else parseHexColor(cat.hexColor))
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = cat.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (filteredBooks.isEmpty()) {
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
            Box(
              modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (books.isEmpty()) "No books added yet" else "No matching books found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Tap the + button below to add your first textbook, syllabus, or reading material.",
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
          items(filteredBooks, key = { it.id }) { book ->
            BookCard(
              book = book,
              onClick = { onBookClick(book) },
              onDelete = { viewModel.deleteBook(book.id) }
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
    AddBookDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { title, author, category, colorTag, chaptersCount ->
        viewModel.addBook(title, author, category, colorTag, chaptersCount)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun BookCard(
  book: Book,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = parseHexColor(book.colorTag)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("book_item_${book.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ProgressRing(
        progress = book.progressPercent,
        color = accentColor,
        modifier = Modifier.size(54.dp)
      ) {
        Text(
          text = "${(book.progressPercent * 100).toInt()}%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        CategoryBadge(categoryName = book.category, hexColor = book.colorTag)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = book.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
        if (book.author.isNotBlank()) {
          Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${book.completedChapters} of ${book.totalChapters} chapters completed",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onDelete) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Book",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
