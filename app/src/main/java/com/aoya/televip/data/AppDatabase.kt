package com.aoya.televip.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeletedMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deletedMessageDao(): DeletedMessageDao

    companion object {
        @Volatile
        private var dbInstance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            dbInstance ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "televip",
                        ).build()
                dbInstance = instance
                instance
            }
    }
}
