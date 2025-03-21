package com.github.oryanmat.trellowidget

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.oryanmat.trellowidget.data.database.WidgetEntity
import com.github.oryanmat.trellowidget.di.AppModule
import com.github.oryanmat.trellowidget.util.cleanUpWidgetData
import com.github.oryanmat.trellowidget.util.getBoard
import com.github.oryanmat.trellowidget.util.getInterval
import com.github.oryanmat.trellowidget.util.getList
import com.github.oryanmat.trellowidget.util.getMigrationStatus
import com.github.oryanmat.trellowidget.util.isUserPreferencesSet
import com.github.oryanmat.trellowidget.widget.AlarmReceiver
import com.github.oryanmat.trellowidget.widget.getWidgetIds
import com.github.oryanmat.trellowidget.worker.WidgetUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

        if (!isUserExisting())
            setMigrationDone()
        else if (!isMigrationDone()) {
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            appScope.launch {
                cancelOldAlarm(this@TrelloWidget)
                migrateSharedPreferences()
                setMigrationDone()
            }
        }
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

    private suspend fun migrateSharedPreferences() {
        val repository = appModule.trelloWidgetRepository
        val widgetIds = getWidgetIds()
        for (widgetId in widgetIds) {
            val board = getBoard(widgetId)
            val boardList = getList(widgetId)
            repository.storeWidget(
                WidgetEntity(
                    widgetId,
                    board.name,
                    board.url,
                    boardList.name,
                    boardList.id
                )
            )
        }
        cleanUpWidgetData(widgetIds)
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

    private fun isUserExisting(): Boolean {
        return isUserPreferencesSet()
    }

    private fun isMigrationDone(): Boolean {
        return getMigrationStatus()
    }

    private fun setMigrationDone() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean(getString(R.string.pref_migration_status_key), true).apply()
    }
}