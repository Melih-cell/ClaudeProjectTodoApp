package com.datastoreapp.claudeproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datastoreapp.claudeproject.ui.theme.ClaudeProjectTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                setContent {
                    ClaudeProjectTheme {
                        val taskViewModel: TaskViewModel = viewModel()
                        val tasks by taskViewModel.allTasks.collectAsState(initial = emptyList())
                        val categories by taskViewModel.allCategories.collectAsState(initial = emptyList())
                        var newTaskTitle by remember { mutableStateOf("") }
                        var selectedCategory by remember { mutableStateOf<Category?>(null) }
                        val snackbarHostState = remember { SnackbarHostState() }
                        val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("TaskMaster") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    taskViewModel.addTask(newTaskTitle, selectedCategory?.id)
                                    newTaskTitle = ""
                                    selectedCategory = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Task added successfully!")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("New Task") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CategorySelector(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn {
                            items(tasks) { task ->
                                TaskItem(
                                    task = task,
                                    category = categories.find { it.id == task.categoryId },
                                    onCheckedChange = { isChecked ->
                                        taskViewModel.updateTask(task.copy(isCompleted = isChecked))
                                    },
                                    onDelete = {
                                        taskViewModel.deleteTask(task)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Task deleted")
                                        }
                                    },
                                    viewModel = taskViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    category: Category?,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    viewModel: TaskViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                category?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(it.color))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Text(
                    text = "Created: ${viewModel.formatDate(task.createdDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task")
            }
        }
    }
}