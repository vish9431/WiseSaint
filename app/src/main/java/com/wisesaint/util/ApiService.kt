package com.wisesaint.util

import com.wisesaint.model.StreamRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface ApiService {
    @Streaming
    @POST("ask")
    suspend fun getStreamingResponse(@Body request: StreamRequest): ResponseBody
}