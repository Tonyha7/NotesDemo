package com.th7.notesdemo

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface NoteApi {
    @GET("notes")
    fun getNotes(): Call<ApiResponse<List<Note>>>

    @POST("notes")
    fun addNote(@Body note: Note): Call<ApiResponse<Boolean>>

    @PUT("notes")
    fun updateNote(@Body note: Note): Call<ApiResponse<Boolean>>

    @DELETE("notes")
    fun deleteNote(@Query("id") id: Int): Call<ApiResponse<Boolean>>

    companion object {
        private const val BASE_URL = "http://192.168.31.31:8080/"

        fun create(): NoteApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(NoteApi::class.java)
        }
    }
}
