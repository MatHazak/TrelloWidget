package com.github.oryanmat.trellowidget.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.widget.updateWidgetCards

class WidgetUpdateWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TrelloWidget.appModule.trelloWidgetRepository
        val widgetEntities = repository.getAllWidgets()
        for (widgetEntity in widgetEntities) {
            val boardListId = widgetEntity.boardListId
            val successFetch = repository.fetchAndStoreBoardList(boardListId)
            if(successFetch)
                updateWidget(widgetEntity.widgetId)
        }
        return Result.success()
    }

    private fun updateWidget(widgetId: Int) {
        applicationContext.updateWidgetCards(widgetId)
    }
}