package com.wisesaint.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wisesaint.R
import com.wisesaint.databinding.ActivityMainBinding
import com.wisesaint.databinding.ActivityResponseChatBinding
import com.wisesaint.util.NetworkModule
import com.wisesaint.util.StreamingRepository
import com.wisesaint.util.StreamingViewModel
import kotlinx.coroutines.launch

class ResponseChat : AppCompatActivity() {

    private lateinit var binding: ActivityResponseChatBinding

    private val repository by lazy { StreamingRepository(NetworkModule.apiService) }
    private val viewModel by viewModels<StreamingViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StreamingViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response_chat)

        binding = ActivityResponseChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()

    }

    private fun setupUI() {
        with(binding) {
            responseTextView.movementMethod = ScrollingMovementMethod.getInstance()

            sendButton.setOnClickListener {
                val message = inputEditText.text.toString()
                if (message.isNotBlank()) {
                    responseTextView.text = ""  // Clear previous content
                    viewModel.startStreaming(message)
                    inputEditText.text?.clear()
                }
            }

            // Handle Enter key press
            inputEditText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    sendButton.performClick()
                    true
                } else false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.streamingText.collect { text ->
                        binding.responseTextView.apply {
                            this.text = text
                            // Smooth auto-scroll
                            post {
                                val scrollAmount = layout.getLineTop(lineCount) - height
                                if (scrollAmount > 0) {
                                    scrollTo(0, scrollAmount)
                                }
                            }
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.sendButton.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@ResponseChat, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }



}