package com.github.oryanmat.trellowidget.data.remote

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.data.model.BoardList
import com.github.oryanmat.trellowidget.util.Json
import com.github.oryanmat.trellowidget.util.preferences
import java.util.concurrent.ExecutionException

const val TOKEN_PREF_KEY = "com.oryanmat.trellowidget.usertoken"
const val APP_KEY = "b250ef70ccf79ea5e107279a91045e6e"
const val BASE_URL = "https://api.trello.com/"
const val API_VERSION = "1/"
const val KEY = "&key=$APP_KEY"
const val AUTH_URL = "$BASE_URL$API_VERSION" + "authorize" +
        "?name=TrelloWidget" +
        KEY +
        "&expiration=never" +
        "&callback_method=fragment" +
        "&return_url=trello-widget://callback"

const val USER_PATH = "members/me?fields=fullName,username"
const val BOARDS_PATH = "members/me/boards?filter=open&fields=id,name,url&lists=open"
const val LIST_CARDS_PATH = "lists/%s?cards=open&card_fields=name,badges,labels,url"

const val ERROR_MESSAGE = "HTTP request to Trello failed: %s"

class TrelloApi(context: Context) {
    private val queue: RequestQueue by lazy { Volley.newRequestQueue(context) }
    private val preferences = context.preferences()
    private fun buildURL(query: String) =
        "$BASE_URL$API_VERSION$query$KEY&${preferences.getString(TOKEN_PREF_KEY, "")}"

    fun getCards(id: String): BoardList {
        val json = get(buildURL(LIST_CARDS_PATH.format(id)))

        return Json.tryParseJson(json, BoardList::class.java, BoardList.error(json))
    }

    fun getUserAsync(listener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        getAsync(buildURL(USER_PATH), listener, errorListener)
    }

    fun getBoardsAsync(listener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        getAsync(buildURL(BOARDS_PATH), listener, errorListener)
    }

    private fun getAsync(
        url: String,
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {
        queue.add(StringRequest(Request.Method.GET, url, listener, errorListener))
    }

    private fun get(url: String): String {
        val future = RequestFuture.newFuture<String>()
        queue.add(StringRequest(Request.Method.GET, url, future, future))

        return try {
            future.get()
        } catch (e: ExecutionException) {
            logException(e)
        } catch (e: InterruptedException) {
            logException(e)
        }
    }

    private fun logException(e: Exception): String {
        val msg = ERROR_MESSAGE.format(e)
        Log.e(T_WIDGET, msg)
        return msg
    }
}