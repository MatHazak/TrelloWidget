package com.github.oryanmat.trellowidget.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WidgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widget: WidgetEntity)

    @Query("SELECT * FROM widget_table")
    fun getAllWidgets(): List<WidgetEntity>

    @Query("SELECT * FROM widget_table WHERE widgetId = :id")
    fun getWidgetById(id: Int): WidgetEntity?

    @Query("SELECT COUNT(*) FROM widget_table WHERE boardListId = :boardListId")
    suspend fun countWidgetsWithBoardList(boardListId: String): Int

    @Query("DELETE FROM widget_table WHERE widgetId = :id")
    suspend fun deleteWidgetById(id: Int)
}
