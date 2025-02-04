package com.github.oryanmat.trellowidget

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.oryanmat.trellowidget.di.AppModule
import com.github.oryanmat.trellowidget.util.getInterval
import com.github.oryanmat.trellowidget.widget.AlarmReceiver
import com.github.oryanmat.trellowidget.worker.WidgetUpdateWorker
import java.util.concurrent.TimeUnit

private const val DEBUG = false

class TrelloWidget : Application() {

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        if (DEBUG) StrictMode.enableDefaults()
        super.onCreate()
        appModule = AppModule(this)

        cancelOldAlarm(this)

        scheduleWidgetUpdateWork(this)
    }

    private fun cancelOldAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleWidgetUpdateWork(context: Context) {
        val interval = context.getInterval().toLong()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            interval, TimeUnit.MILLISECONDS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WidgetUpdateWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}