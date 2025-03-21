package com.github.oryanmat.trellowidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.oryanmat.trellowidget.R

internal fun Context.updateAllWidgets() {
    updateWidgetUi(*getWidgetIds())
    updateWidgetCards(*getWidgetIds())
}

internal fun Context.updateWidget(appWidgetId: Int) {
    updateWidgetUi(appWidgetId)
    updateWidgetCards(appWidgetId)
}

private fun Context.updateWidgetUi(vararg appWidgetIds: Int) {
    val intent = Intent(this, TrelloWidgetProvider::class.java)
    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
    sendBroadcast(intent)
}

internal fun Context.updateWidgetCards(vararg appWidgetIds: Int) =
    widgetManager().notifyAppWidgetViewDataChanged(appWidgetIds, R.id.card_list)

fun Context.getWidgetIds(): IntArray = widgetManager().getAppWidgetIds(trelloComponentName())

private fun Context.trelloComponentName() = ComponentName(this, TrelloWidgetProvider::class.java)

internal fun Context.widgetManager() = AppWidgetManager.getInstance(this)