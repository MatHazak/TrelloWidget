package com.github.oryanmat.trellowidget.activity

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.data.model.Board
import com.github.oryanmat.trellowidget.data.model.BoardList
import com.github.oryanmat.trellowidget.data.remote.Error
import com.github.oryanmat.trellowidget.data.remote.Success
import com.github.oryanmat.trellowidget.databinding.ActivityConfigBinding
import com.github.oryanmat.trellowidget.util.Constants.T_WIDGET_TAG
import com.github.oryanmat.trellowidget.util.OnItemSelectedAdapter
import com.github.oryanmat.trellowidget.viewmodels.ConfigViewModel
import com.github.oryanmat.trellowidget.viewmodels.viewModelFactory

class ConfigActivity : AppCompatActivity(), OnItemSelectedAdapter {

    private val viewModel: ConfigViewModel by viewModels {
        viewModelFactory {
            ConfigViewModel(
                TrelloWidget.appModule.trelloWidgetRepository,
                TrelloWidget.appModule.appContext
            )
        }
    }
    private var _binding: ActivityConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener { finish() }
        binding.okButton.setOnClickListener { onOkClicked() }
        setWidgetId()

        viewModel.loadPresentConfig()
        viewModel.getBoards()
        viewModel.boards.observe(this) { response ->
            when (response) {
                is Success -> onSuccessResponse(response.data)
                is Error -> onErrorResponse(response.error)
            }
        }
    }

    private fun setWidgetId() {
        val extras = intent.extras

        if (extras != null) {
            viewModel.appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        if (viewModel.appWidgetId == INVALID_APPWIDGET_ID) {
            finish()
        }
    }

    private fun onSuccessResponse(boards: List<Board>) {
        binding.progressBar.visibility = View.GONE
        binding.content.visibility = View.VISIBLE
        val index = boards.indexOfFirst { board: Board -> board.name == viewModel.boardName }
        setSpinner(binding.boardSpinner, boards, this, index)
    }

    private fun onErrorResponse(error: String) {
        finish()

        Log.e(T_WIDGET_TAG, error)
        val text = getString(R.string.board_load_fail)
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent) {
            binding.boardSpinner -> {
                val board = parent.getItemAtPosition(position) as Board
                viewModel.boardName = board.name
                viewModel.boardUrl = board.url
                val index =
                    board.lists.indexOfFirst { boardList: BoardList -> boardList.name == viewModel.listName }
                setSpinner(binding.listSpinner, board.lists, this, index)
            }

            binding.listSpinner -> {
                val list = parent.getItemAtPosition(position) as BoardList
                viewModel.listName = list.name
                viewModel.listId = list.id
            }
        }
    }

    private fun <T> setSpinner(
        spinner: Spinner, lists: List<T>,
        listener: AdapterView.OnItemSelectedListener, selectedIndex: Int
    ): Spinner {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lists)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
        spinner.setSelection(if (selectedIndex > -1) selectedIndex else 0)
        return spinner
    }

    private fun onOkClicked() {
        if (viewModel.isConfigInvalid()) return
        viewModel.updateConfig()
        returnOk()
    }

    private fun returnOk() {
        val resultValue = Intent()
        resultValue.putExtra(EXTRA_APPWIDGET_ID, viewModel.appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.boards.removeObservers(this)
        _binding = null
    }
}