package com.games.commonappsstuff.connection.backend

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BackendService {

    @GET("/")
    suspend fun sendFirstOpenMessage(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("bundle_id", encoded=true) bundleId: String,
        @Query("push_token", encoded=true) pushToken: String,
        @Query("campaign_name", encoded=true) campaignName: String?,
        @Query("campaign_id", encoded=true) campaignId: String?,
        @Query("adset_name", encoded=true) adsetName: String?,
        @Query("adset_id", encoded=true) adsetId: String?,
        @Query("adgroup_name", encoded=true) adgroupName: String?,
        @Query("adgroup_id", encoded=true) adgroupId: String?,
        @Query("af_channel", encoded=true) af_channel: String?,
        @Query("user_country", encoded=true) user_country: String?) : Response<ResponseEntity>

    @GET("/event")
    suspend fun setNotificationOpenEvent(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("push_open") pushOpen: Int = 1)
}