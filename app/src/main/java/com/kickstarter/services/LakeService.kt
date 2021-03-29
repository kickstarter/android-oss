package com.kickstarter.services

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query
import rx.Observable

interface LakeService {

    @Headers("Content-Type: application/json")
    @PUT("record")
    fun track(@Body body: RequestBody, @Query("client_id") clientId: String): Observable<Response<ResponseBody>>
}
