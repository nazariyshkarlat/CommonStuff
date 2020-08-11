package com.games.commonappsstuff.connection.backend

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PostService {

    @GET("/")
    suspend fun sendMessage(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("bundle_id", encoded=true) bundleId: String,
        @Query("fbcl_id", encoded=true) fbclId: String?,
        @Query("push_token", encoded=true) pushToken: String) : Response<ResponseEntity>

}