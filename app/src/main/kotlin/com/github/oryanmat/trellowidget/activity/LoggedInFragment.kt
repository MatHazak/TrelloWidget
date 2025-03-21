package com.github.oryanmat.trellowidget.activity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.data.remote.Error
import com.github.oryanmat.trellowidget.data.remote.Success
import com.github.oryanmat.trellowidget.databinding.FragmentLoggedInBinding
import com.github.oryanmat.trellowidget.util.Constants.DELAY
import com.github.oryanmat.trellowidget.util.Constants.T_WIDGET_TAG
import com.github.oryanmat.trellowidget.viewmodels.LoggedInViewModel
import com.github.oryanmat.trellowidget.viewmodels.viewModelFactory
import java.util.Timer
import kotlin.concurrent.schedule

class LoggedInFragment : Fragment() {

    private val viewModel: LoggedInViewModel by viewModels {
        viewModelFactory {
            LoggedInViewModel(TrelloWidget.appModule.trelloWidgetRepository)
        }
    }

    private var _binding: FragmentLoggedInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoggedInBinding.inflate(inflater, container, false)

        viewModel.retrieveUser(requireContext())

        viewModel.liveUser.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Success -> {
                    viewModel.storeUser(requireContext(), response.data)
                    setUser()
                }

                is Error -> onErrorResponse(response.error)
            }
        }

        setUser()

        checkInternetAndValidateToken()

        return binding.root
    }

    private fun checkInternetAndValidateToken() {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetworkInfo?.isConnected == true
        }
        if (isConnected) {
            viewModel.tryFetchUser()
        }
    }

    private fun onErrorResponse(error: String) {
        Log.e(T_WIDGET_TAG, error)

        if (error.contains("401 Unauthorized"))
            logout(error)
        else
            Timer().schedule(DELAY) { viewModel.tryFetchUser() }
    }

    private fun logout(error: String) {
        if (isAdded) {
            (activity as MainActivity).logout()
            val text = getString(R.string.login_fail).format(error)
            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun setUser() {
        val signedText = if (viewModel.loggedInUser == null) {
            getString(R.string.default_signed_text)
        } else {
            getString(R.string.singed).format(viewModel.loggedInUser)
        }
        binding.signedText.text = signedText
        binding.loadingPanel.visibility = View.GONE
        binding.signedPanel.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}