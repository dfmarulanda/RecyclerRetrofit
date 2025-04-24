package com.example.myapplication

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


internal interface Routes {

    @GET("/api/people/")
    fun getUsers(@Query("page") page: String?): Call<RequestEstudiantes>
}