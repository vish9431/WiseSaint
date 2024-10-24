package com.wisesaint.util

import com.google.gson.Gson
import com.wisesaint.model.StreamRequest
import com.wisesaint.model.StreamResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader


class StreamingRepository(private val apiService: ApiService) {
    private val gson = Gson()

    fun getStreamingResponse(message: String) = callbackFlow {
        val channel = Channel<String>(Channel.UNLIMITED)

        try {
            val request = StreamRequest(message)
            val response = apiService.getStreamingResponse(request)

            val reader = BufferedReader(InputStreamReader(response.byteStream()))
            var buffer = ""
            val charArray = CharArray(1)

            val readerThread = Thread {
                try {
                    while (reader.read(charArray) != -1) {
                        buffer += charArray[0]
                        if (charArray[0] == '\n') {
                            if (buffer.startsWith("data: ")) {
                                try {
                                    val jsonString = buffer.substring(6)
                                    val streamResponse = gson.fromJson(jsonString, StreamResponse::class.java)
                                    streamResponse.content?.let { content ->
                                        trySendBlocking(content).getOrNull()
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed JSON
                                }
                            }
                            buffer = ""
                        }
                    }

                    if (buffer.startsWith("data: ")) {
                        try {
                            val jsonString = buffer.substring(6)
                            val streamResponse = gson.fromJson(jsonString, StreamResponse::class.java)
                            streamResponse.content?.let { content ->
                                trySendBlocking(content).getOrNull()
                            }
                        } catch (e: Exception) {
                            // Skip malformed JSON
                        }
                    }
                } catch (e: Exception) {
                    close(e)
                } finally {
                    close()
                }
            }

            readerThread.start()

            awaitClose {
                try {
                    reader.close()
                    readerThread.interrupt()
                } catch (e: Exception) {
                    // Handle cleanup errors
                }
            }

        } catch (e: Exception) {
            close(e)
        }
    }
}



//class StreamingRepository(private val apiService: ApiService) {
//    private val gson = Gson()
//
//    fun getStreamingResponse(message: String): Flow<String> = flow {
//        try {
//            val request = StreamRequest(message)
//            val response = apiService.getStreamingResponse(request)
//
//            val reader = BufferedReader(InputStreamReader(response.byteStream()))
//            var line: String?
//
//            while (reader.readLine().also { line = it } != null) {
//                line?.let { rawLine ->
//                    if (rawLine.startsWith("data: ")) {
//                        // Remove "data: " prefix and parse the JSON
//                        val jsonLine = rawLine.substring(6)
//                        try {
//                            val streamResponse = gson.fromJson(jsonLine, StreamResponse::class.java)
//                            emit(streamResponse.content)
//                        } catch (e: Exception) {
//                            // Skip malformed JSON lines
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//}