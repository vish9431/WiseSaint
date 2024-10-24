package com.wisesaint.util

import android.content.Context
import android.text.method.Touch.scrollTo
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnimatedStreamTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var currentText = ""
    private var pendingText = ""
    private var isAnimating = false
    private var animationJob: Job? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Adjustable animation speed (characters per second)
    var animationSpeed = 50L

    fun appendTextWithAnimation(newText: String) {
        pendingText += newText
        if (!isAnimating) {
            startAnimation()
        }
    }

    private fun startAnimation() {
        isAnimating = true
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            while (pendingText.isNotEmpty()) {
                val nextChar = pendingText.first()
                pendingText = pendingText.drop(1)
                currentText += nextChar
                text = currentText

                // Auto-scroll to bottom
                post {
                    val scrollAmount = layout?.getLineTop(lineCount) ?: 0 - height
                    if (scrollAmount > 0) {
                        scrollTo(0, scrollAmount)
                    }
                }

                delay(1000L / animationSpeed) // Adjust delay for smooth animation
            }
            isAnimating = false
        }
    }

    fun clear() {
        currentText = ""
        pendingText = ""
        text = ""
        animationJob?.cancel()
        isAnimating = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
    }
}