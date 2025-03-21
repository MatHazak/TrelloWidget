package com.github.oryanmat.trellowidget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_table")
data class WidgetEntity(
    @PrimaryKey val widgetId: Int,
    val boardName: String,
    val boardUrl: String,
    val boardListName: String,
    val boardListId: String
)