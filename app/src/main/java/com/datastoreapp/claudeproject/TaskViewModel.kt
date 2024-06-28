package com.datastoreapp.claudeproject

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao: TaskDao
    private val categoryDao: CategoryDao
    val allTasks: Flow<List<Task>>
    val allCategories: Flow<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        taskDao = database.taskDao()
        categoryDao = database.categoryDao()
        allTasks = taskDao.getAllTasks()
        allCategories = categoryDao.getAllCategories()

        // Örnek kategoriler ekle
        viewModelScope.launch(Dispatchers.IO) {
            if (categoryDao.getAllCategories().first().isEmpty()) {
                addCategory("Work", -0xff0100) // Kırmızı
                addCategory("Personal", -0xffff01) // Mavi
                addCategory("Shopping", -0x100) // Yeşil
            }
        }
    }

    fun addTask(title: String, categoryId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTask = Task(title = title, categoryId = categoryId)
            taskDao.insertTask(newTask)
            Log.d("TaskViewModel", "Task added: $newTask")
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
        }
    }

    private fun addCategory(name: String, color: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insertCategory(Category(name = name, color = color))
        }
    }

    fun getTasksByCategory(categoryId: Int): Flow<List<Task>> {
        return taskDao.getTasksByCategory(categoryId)
    }

    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
}