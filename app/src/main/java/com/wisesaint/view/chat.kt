package com.wisesaint.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wisesaint.R
import com.wisesaint.adapter.ChatAdapter
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonPlugin
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class chat : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView
    private var currentStreamingJob: Job? = null

    // Initialize Markwon with the correct LaTeX plugin initialization
//    private val markwon by lazy {
//        val textSize = resources.displayMetrics.density * 16 // Convert 16dp to pixels
//        Markwon.builder(this)
//            .usePlugin(MarkwonInlineParserPlugin.create())
//            .usePlugin(JLatexMathPlugin.create(textSize)) // Pass the text size in pixels
//            .build()
//    }

    private val markwon by lazy {
        val textSize = resources.displayMetrics.density * 16 // Convert 16dp to pixels
        Markwon.builder(this)
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(textSize) {
                it.inlinesEnabled(true) // Enable inline parsing
            })
            .build()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Initialize the adapter with an empty list of messages
//        chatAdapter = ChatAdapter(mutableListOf())
//        recyclerView.adapter = chatAdapter

        setupRecyclerView()

        // Example: simulate a message being streamed
//        simulateMessageStream("Pythagoras of Samos[a] (Ancient Greek: Πυθαγόρας; c. 570 – c. 495 BC)[b] was an ancient Ionian Greek philosopher, polymath, and the eponymous founder of Pythagoreanism. His political and religious teachings were well known in Magna Graecia and influenced the philosophies of Plato, Aristotle, and, through them, the West in general. Knowledge of his life is clouded by legend; modern scholars disagree regarding Pythagoras's education and influences, but they do agree that, around 530 BC, he travelled to Croton in southern Italy, where he founded a school in which initiates " +
//                "were sworn to secrecy and lived a communal, ascetic lifestyle.\n")

        // Simulate streaming with Markdown and LaTeX
       // simulateMessageStream("Here is some markdown: **bold text**, and LaTeX: \\(a^2 + b^2 = c^2\\)")


        // Simple example messages to test different formats
        // Example messages with correct LaTeX syntax
        // Test messages with correct LaTeX syntax
//        val testMessages = listOf(
//            "Regular text message",
//            "**Bold** and *italic* markdown",
//            "Inline equation: \\(E=mc^2\\)",  // Use \( and \) for inline equations
//            """
//            # Examples of equations:
//
//            Inline: \\(x^2 + y^2 = r^2\\)
//
//            Block equation:
//            $$
//            \frac{-b \pm \sqrt{b^2-4ac}}{2a}
//            $$
//            """.trimIndent()
//        )

        val testMessages = listOf(
            "*Sine*: \\( \\sin(\\theta) = \\frac{\\text{Opposite}}{\\text{Hypotenuse}} \\)",
            "Example 2: Equation with fractions \\[ f(x) = \\frac{1}{2\\pi i} \\oint_C \\frac{f(z)}{z-z_0} \\, dz \\]",
            "Example 3: Quadratic formula \\(x = \\frac{{-b \\pm \\sqrt{{b^2 - 4ac}}}}{{2a}}\\)",
            "Example 4: Maxwell's equations \\[\n" +
                    "  \\begin{aligned}\n" +
                    "  \\nabla \\cdot \\mathbf{E} &= \\frac{\\rho}{\\varepsilon_0} \\\\\n" +
                    "  \\nabla \\cdot \\mathbf{B} &= 0 \\\\\n" +
                    "  \\nabla \\times \\mathbf{E} &= -\\frac{\\partial \\mathbf{B}}{\\partial t} \\\\\n" +
                    "  \\nabla \\times \\mathbf{B} &= \\mu_0 \\mathbf{J} + \\mu_0\\varepsilon_0 \\frac{\\partial \\mathbf{E}}{\\partial t}\n" +
                    "  \\end{aligned}\n" +
                    "\\]",
            "Example 5: Dirac equation \\(\\gamma^\\mu (i\\hbar\\partial_\\mu - e A_\\mu) \\psi - m c \\psi = 0\\)"
        )









        // Start streaming messages
        streamMessages(testMessages)


    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true  // Messages stack from bottom
        }
        chatAdapter = ChatAdapter(mutableListOf(), markwon)
        recyclerView.adapter = chatAdapter
    }

    private fun streamMessages(messages: List<String>) {
        var messageIndex = 0

        CoroutineScope(Dispatchers.Main).launch {
            messages.forEach { message ->
                streamSingleMessage(message)
                delay(1000) // Delay between messages
            }
        }
    }

    private fun streamSingleMessage(fullMessage: String) {
        // Cancel any existing streaming job
        currentStreamingJob?.cancel()

        currentStreamingJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                chatAdapter.addMessage("")  // Add empty message placeholder

                var currentMessage = ""
                fullMessage.forEach { char ->
                    delay(10)  // Adjust streaming speed as needed
                    currentMessage += char

                    withContext(Dispatchers.Main) {
                        chatAdapter.updateLastMessage(currentMessage)
                        // Ensure recycler view scrolls to bottom
                        recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }
                }

                // Final render with complete message
                withContext(Dispatchers.Main) {
                    chatAdapter.updateLastMessage(fullMessage, true)  // true indicates final render
                    recyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentStreamingJob?.cancel()  // Clean up coroutine when activity is destroyed
    }
}