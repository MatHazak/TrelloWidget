package com.github.oryanmat.trellowidget.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.activity.ConfigActivity
import com.github.oryanmat.trellowidget.util.Constants.TRELLO_PACKAGE_NAME
import com.github.oryanmat.trellowidget.util.Constants.TRELLO_URL
import com.github.oryanmat.trellowidget.util.Constants.T_WIDGET_TAG
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setBackgroundColor
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImage
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImageViewColor
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setTextView
import com.github.oryanmat.trellowidget.util.color.lightDim
import com.github.oryanmat.trellowidget.util.displayBoardName
import com.github.oryanmat.trellowidget.util.getCardBackgroundColor
import com.github.oryanmat.trellowidget.util.getCardForegroundColor
import com.github.oryanmat.trellowidget.util.getTitleBackgroundColor
import com.github.oryanmat.trellowidget.util.getTitleForegroundColor
import com.github.oryanmat.trellowidget.util.isTitleEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val REFRESH_ACTION = "com.github.oryanmat.trellowidget.refreshAction"
private const val WIDGET_ID = "com.github.oryanmat.trellowidget.widgetId"


class TrelloWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            REFRESH_ACTION -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val widgetId = intent.getIntExtra(WIDGET_ID, 0)
                    val repository = TrelloWidget.appModule.trelloWidgetRepository
                    val widgetEntity = repository.getWidget(widgetId)
                    if (widgetEntity == null) {
                        Log.e(T_WIDGET_TAG, "Can't find widget with ID $widgetId.")
                        return@launch
                    }
                    val successFetch = repository.fetchAndStoreBoardList(widgetEntity.boardListId)
                    if (!successFetch) {
                        Log.e(
                            T_WIDGET_TAG,
                            "widget for ${widgetEntity.boardName}/${widgetEntity.boardListName} failed to fetch list."
                        )
                        return@launch
                    }
                    context.updateWidgetCards(widgetEntity.widgetId)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        val repository = TrelloWidget.appModule.trelloWidgetRepository
        CoroutineScope(Dispatchers.IO).launch {
            for (widgetId in appWidgetIds) {
                repository.deleteWidget(widgetId)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.trello_widget)
        updateTitleBar(appWidgetId, context, views) {
            updateCardList(appWidgetId, context, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun updateTitleBar(appWidgetId: Int, context: Context, views: RemoteViews, onComplete: () -> Unit) {
        @ColorInt val foregroundColor = context.getTitleForegroundColor()
        val repository = TrelloWidget.appModule.trelloWidgetRepository

        CoroutineScope(Dispatchers.IO).launch {
            val widget = repository.getWidget(appWidgetId)
            if (widget == null) {
                Log.e(T_WIDGET_TAG, "Failed to update widget's title bar: No widget found for ID $appWidgetId.")
                withContext(Dispatchers.Main) { onComplete() }
                return@launch
            }

            withContext(Dispatchers.Main) {
                setTextView(context, views, R.id.board_name, widget.boardName + " / ", foregroundColor, R.dimen.widget_title_text)
                setTextView(context, views, R.id.list_name, widget.boardListName, foregroundColor, R.dimen.widget_title_text)
                views.setOnClickPendingIntent(R.id.list_title, getTitleIntent(context, widget.boardUrl))
                setBackgroundColor(views, R.id.title_bar, context.getTitleBackgroundColor())
                views.setViewVisibility(R.id.board_name,
                    if (context.displayBoardName()) View.VISIBLE else View.GONE
                )
                setImage(context, views, R.id.refreshButt, R.drawable.ic_refresh_white_24dp)
                setImage(context, views, R.id.configButt, R.drawable.ic_settings_white_24dp)
                setImageViewColor(views, R.id.refreshButt, foregroundColor.lightDim())
                setImageViewColor(views, R.id.configButt, foregroundColor.lightDim())
                views.setOnClickPendingIntent(R.id.refreshButt, getRefreshPendingIntent(context, appWidgetId))
                views.setOnClickPendingIntent(R.id.configButt, getReconfigPendingIntent(context, appWidgetId))
                setImageViewColor(views, R.id.divider, foregroundColor)
                onComplete()
            }
        }
    }

    private fun updateCardList(appWidgetId: Int, context: Context, views: RemoteViews) {
        setBackgroundColor(views, R.id.card_frame, context.getCardBackgroundColor())
        views.setTextColor(R.id.empty_card_list, context.getCardForegroundColor())
        views.setEmptyView(R.id.card_list, R.id.empty_card_list)
        views.setPendingIntentTemplate(R.id.card_list, getCardPendingIntent(context))
        views.setRemoteAdapter(R.id.card_list, getRemoteAdapterIntent(context, appWidgetId))
    }

    private fun getRemoteAdapterIntent(context: Context, appWidgetId: Int): Intent {
        val intent = Intent(context, WidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        return intent
    }

    private fun getRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val refreshIntent = Intent(context, TrelloWidgetProvider::class.java)
        refreshIntent.action = REFRESH_ACTION
        refreshIntent.putExtra(WIDGET_ID, appWidgetId)
        return PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, FLAG_IMMUTABLE)
    }

    private fun getReconfigPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val reconfigIntent = Intent(context, ConfigActivity::class.java)
        reconfigIntent.action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        reconfigIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return PendingIntent.getActivity(context, appWidgetId, reconfigIntent, FLAG_IMMUTABLE)
    }

    @SuppressLint("MutableImplicitPendingIntent")
    private fun getCardPendingIntent(context: Context): PendingIntent {
        // individual card URIs are set in a RemoteViewsFactory.setOnClickFillInIntent
        return PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_VIEW), FLAG_MUTABLE)
    }

    private fun getTitleIntent(context: Context, boardUrl: String): PendingIntent {
        val intent = if (context.isTitleEnabled()) getBoardIntent(context, boardUrl) else Intent()
        return PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
    }

    private fun getBoardIntent(context: Context, boardUrl: String) = if (boardUrl.isNotEmpty()) {
        Intent(Intent.ACTION_VIEW, Uri.parse(boardUrl))
    } else {
        getTrelloIntent(context)
    }

    private fun getTrelloIntent(context: Context): Intent {
        // try to find trello's app if installed. otherwise just open the website.
        val intent = context.packageManager.getLaunchIntentForPackage(TRELLO_PACKAGE_NAME)
        return intent ?: Intent(Intent.ACTION_VIEW, Uri.parse(TRELLO_URL))
    }
}