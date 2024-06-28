package com.datastoreapp.claudeproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class, Category::class], version = 3, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Kategori tablosunu oluştur
                database.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, color INTEGER NOT NULL)")

                // Geçici bir tablo oluştur
                database.execSQL("CREATE TABLE tasks_temp (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, isCompleted INTEGER NOT NULL, createdDate INTEGER NOT NULL, categoryId INTEGER, FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL)")

                // Verileri geçici tabloya kopyala
                database.execSQL("INSERT INTO tasks_temp (id, title, isCompleted, createdDate) SELECT id, title, isCompleted, createdDate FROM tasks")

                // Eski tabloyu sil
                database.execSQL("DROP TABLE tasks")

                // Geçici tabloyu yeniden adlandır
                database.execSQL("ALTER TABLE tasks_temp RENAME TO tasks")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}