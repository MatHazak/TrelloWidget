package com.github.oryanmat.trellowidget.viewmodels

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.oryanmat.trellowidget.data.TrelloWidgetRepository
import com.github.oryanmat.trellowidget.data.database.WidgetEntity
import com.github.oryanmat.trellowidget.data.model.Board
import com.github.oryanmat.trellowidget.data.remote.ApiResponse
import com.github.oryanmat.trellowidget.widget.updateWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ConfigViewModel(
    private val repository: TrelloWidgetRepository,
    private val appContext: Context
) : ViewModel() {
    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    var boardName: String = ""
    var boardUrl: String = ""
    var listName: String = ""
    var listId: String = ""

    val boards: LiveData<ApiResponse<List<Board>>> = repository.boards

    fun getBoards() = viewModelScope.launch {
        repository.fetchBoards()
    }

    fun loadPresentConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            val widget = repository.getWidget(appWidgetId)
            if (widget != null) {
                boardName = widget.boardName
                listName = widget.boardListName
            }
        }
    }

    fun isConfigInvalid(): Boolean = boardName.isEmpty() || listName.isEmpty()

    fun updateConfig() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            repository.storeWidget(WidgetEntity(appWidgetId, boardName, boardUrl, listName, listId))
            repository.fetchAndStoreBoardList(listId)
            appContext.updateWidget(appWidgetId)
        }
    }
}