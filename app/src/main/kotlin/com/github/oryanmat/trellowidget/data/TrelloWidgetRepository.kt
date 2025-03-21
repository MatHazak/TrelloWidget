package com.github.oryanmat.trellowidget.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.oryanmat.trellowidget.data.database.BoardListDao
import com.github.oryanmat.trellowidget.data.database.BoardListEntity.Companion.fromBoardList
import com.github.oryanmat.trellowidget.data.database.WidgetDao
import com.github.oryanmat.trellowidget.data.database.WidgetEntity
import com.github.oryanmat.trellowidget.data.model.Board
import com.github.oryanmat.trellowidget.data.model.User
import com.github.oryanmat.trellowidget.data.remote.ApiResponse
import com.github.oryanmat.trellowidget.data.remote.Success
import com.github.oryanmat.trellowidget.data.remote.TrelloApi
import com.github.oryanmat.trellowidget.util.Constants.T_WIDGET_TAG

class TrelloWidgetRepository(
    private val boardListDao: BoardListDao,
    private val widgetDao: WidgetDao,
    private val trelloApi: TrelloApi
) {

    val user = MutableLiveData<ApiResponse<User>>()
    val boards = MutableLiveData<ApiResponse<List<Board>>>()

    suspend fun fetchUser() =
        user.postValue(trelloApi.getUser())

    suspend fun fetchBoards() =
        boards.postValue(trelloApi.getBoards())

    suspend fun fetchAndStoreBoardList(listId: String): Boolean {
        return try {
            when (val apiResponse = fetchBoardList(listId)) {
                is Success -> {
                    val boardListEntity = fromBoardList(apiResponse.data)
                    boardListDao.insert(boardListEntity)
                    true
                }
                else -> {
                    Log.e(T_WIDGET_TAG, "Error on API response for list with ID $listId")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(T_WIDGET_TAG, "Exception fetching list with ID $listId", e)
            false
        }    }

    private suspend fun fetchBoardList(listId: String) =
        trelloApi.getCards(listId)

    fun getBoardList(listId: String) =
        boardListDao.getBoardListById(listId)?.toBoardList()

    suspend fun storeWidget(widgetEntity: WidgetEntity) =
        widgetDao.insert(widgetEntity)

    fun getWidget(widgetId: Int) =
        widgetDao.getWidgetById(widgetId)

    fun getAllWidgets() =
        widgetDao.getAllWidgets()

    suspend fun deleteWidget(widgetId: Int) {
        val widget = widgetDao.getWidgetById(widgetId) ?: return
        val boardListId = widget.boardListId
        widgetDao.deleteWidgetById(widgetId)
        val boardListCounter = widgetDao.countWidgetsWithBoardList(boardListId)
        if (boardListCounter == 0)
            boardListDao.deleteBoardListById(boardListId)
    }
}