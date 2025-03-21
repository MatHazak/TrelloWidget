package com.github.oryanmat.trellowidget.util

import android.content.Context
import com.github.oryanmat.trellowidget.data.model.Board
import com.github.oryanmat.trellowidget.data.model.BoardList
import com.github.oryanmat.trellowidget.util.Constants.BOARD_KEY
import com.github.oryanmat.trellowidget.util.Constants.INTERNAL_PREFS
import com.github.oryanmat.trellowidget.util.Constants.LIST_KEY

internal fun Context.getList(appWidgetId: Int): BoardList =
        get(appWidgetId, LIST_KEY, BoardList.NULL_JSON, BoardList::class.java)

internal fun Context.getBoard(appWidgetId: Int): Board =
        get(appWidgetId, BOARD_KEY, Board.NULL_JSON, Board::class.java)

internal fun Context.cleanUpWidgetData(widgetIds: IntArray) {
        val preferenceEditor = preferences().edit()
        for (widgetId in widgetIds) {
                val boardKey = prefKey(widgetId, BOARD_KEY)
                val listKey = prefKey(widgetId, LIST_KEY)
                preferenceEditor.remove(boardKey).remove(listKey)
        }
        preferenceEditor.apply()
}

private fun <T> Context.get(appWidgetId: Int, key: String, nullObject: String, c: Class<T>): T =
        Json.fromJson(preferences().getString(prefKey(appWidgetId, key), nullObject)!!, c)

internal fun Context.preferences() = getSharedPreferences(INTERNAL_PREFS, Context.MODE_PRIVATE)

private fun prefKey(appWidgetId: Int, key: String) = appWidgetId.toString() + key