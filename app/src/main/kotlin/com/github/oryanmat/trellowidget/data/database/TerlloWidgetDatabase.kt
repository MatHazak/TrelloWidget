package com.github.oryanmat.trellowidget.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.oryanmat.trellowidget.util.Converters

@Database(
    entities = [BoardListEntity::class, WidgetEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrelloWidgetDatabase : RoomDatabase() {
    abstract fun boardListDao(): BoardListDao
    abstract fun widgetDao(): WidgetDao

    companion object {
        @Volatile
        private var INSTANCE: TrelloWidgetDatabase? = null

        fun getDatabase(context: Context): TrelloWidgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrelloWidgetDatabase::class.java,
                    "trello_widget_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}