package com.wisesaint.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StreamingViewModel(private val repository: StreamingRepository) : ViewModel() {
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Buffer for collecting text chunks
    private val textBuffer = StringBuilder()

    // For managing the update frequency
    private var lastUpdateJob: Job? = null
    private val updateDelay = 16L // About 60fps

    fun startStreaming(message: String) {
        _streamingText.value = ""
        _error.value = null
        _isLoading.value = true
        textBuffer.clear()

        viewModelScope.launch {
            try {
                repository.getStreamingResponse(message)
                    .catch { e ->
                        _error.value = e.message ?: "An error occurred"
                        _isLoading.value = false
                    }
                    .collect { content ->
                        textBuffer.append(content)

                        // Cancel previous update job if it exists
                        lastUpdateJob?.cancel()

                        // Schedule a new update
                        lastUpdateJob = launch {
                            delay(updateDelay)
                            _streamingText.value = textBuffer.toString()
                        }

                        _isLoading.value = false
                    }
            } finally {
                // Ensure final content is displayed
                _streamingText.value = textBuffer.toString()
                _isLoading.value = false
            }
        }
    }
}