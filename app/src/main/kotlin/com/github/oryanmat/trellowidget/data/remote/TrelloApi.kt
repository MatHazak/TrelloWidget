package com.github.oryanmat.trellowidget.data.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.oryanmat.trellowidget.data.model.Board
import com.github.oryanmat.trellowidget.data.model.Board.Companion.LIST_OF_BOARDS_TYPE
import com.github.oryanmat.trellowidget.data.model.BoardList
import com.github.oryanmat.trellowidget.data.model.User
import com.github.oryanmat.trellowidget.util.Constants.API_VERSION
import com.github.oryanmat.trellowidget.util.Constants.BASE_URL
import com.github.oryanmat.trellowidget.util.Constants.BOARDS_PATH
import com.github.oryanmat.trellowidget.util.Constants.KEY
import com.github.oryanmat.trellowidget.util.Constants.LIST_CARDS_PATH
import com.github.oryanmat.trellowidget.util.Constants.TOKEN_PREF_KEY
import com.github.oryanmat.trellowidget.util.Constants.USER_PATH
import com.github.oryanmat.trellowidget.util.Json
import com.github.oryanmat.trellowidget.util.preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.lang.reflect.Type

class TrelloApi(appContext: Context) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(appContext.applicationContext)
    }
    private val preferences = appContext.preferences()

    private fun buildURL(query: String) =
        "$BASE_URL$API_VERSION$query$KEY&${preferences.getString(TOKEN_PREF_KEY, "")}"

    suspend fun getCards(id: String): ApiResponse<BoardList> = withContext(Dispatchers.IO) {
        val url = buildURL(LIST_CARDS_PATH.format(id))
        getSynchronously(url, BoardList::class.java, BoardList.error())
    }

    suspend fun getUser(): ApiResponse<User> {
        val url = buildURL(USER_PATH)
        return getAsynchronously(url, User::class.java, User())
    }

    suspend fun getBoards(): ApiResponse<List<Board>> {
        val url = buildURL(BOARDS_PATH)
        return getAsynchronously(url, LIST_OF_BOARDS_TYPE, emptyList())
    }

    private fun <T> getSynchronously(url: String, type: Type, defaultValue: T): ApiResponse<T> {

        val future = RequestFuture.newFuture<String>()
        requestQueue.add(StringRequest(Request.Method.GET, url, future, future))

        return try {
            val json = future.get()
            val data = Json.tryParseJson(json, type, defaultValue)
            Success(data)
        } catch (e: Exception) {
            val msg = "HTTP request to Trello failed: ${e.stackTraceToString()}"
            Error(msg)
        }
    }

    private suspend fun <T> getAsynchronously(
        url: String,
        type: Type,
        defaultValue: T,
    ): ApiResponse<T> = suspendCancellableCoroutine { cont ->
        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                val data = Json.tryParseJson(response, type, defaultValue)
                cont.resume(Success(data))
            },
            { error ->
                cont.resume(Error("Network error: ${error.message}"))
            }
        )
        requestQueue.add(request)

        cont.invokeOnCancellation { request.cancel() }
    }
}