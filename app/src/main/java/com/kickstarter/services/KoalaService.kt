package com.kickstarter.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query
import rx.Observable

interface KoalaService {

    @POST("/track")
    fun track(@Query("data") data: String) : Observable<Response<ResponseBody>>
}