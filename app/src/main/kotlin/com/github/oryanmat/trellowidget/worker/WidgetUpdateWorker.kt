package com.github.oryanmat.trellowidget.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.oryanmat.trellowidget.widget.updateAllWidgets

class WidgetUpdateWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        context.updateAllWidgets()
        return Result.success()
    }
}