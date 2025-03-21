package com.github.oryanmat.trellowidget.di

import android.content.Context
import com.github.oryanmat.trellowidget.data.TrelloWidgetRepository
import com.github.oryanmat.trellowidget.data.database.TrelloWidgetDatabase
import com.github.oryanmat.trellowidget.data.remote.TrelloApi

class AppModule(val appContext: Context) {

    private val database by lazy {
        TrelloWidgetDatabase.getDatabase(appContext)
    }

    private val trelloApi by lazy {
        TrelloApi(appContext)
    }

    val trelloWidgetRepository: TrelloWidgetRepository by lazy {
        TrelloWidgetRepository(
            boardListDao = database.boardListDao(),
            widgetDao = database.widgetDao(),
            trelloApi = trelloApi
        )
    }
}