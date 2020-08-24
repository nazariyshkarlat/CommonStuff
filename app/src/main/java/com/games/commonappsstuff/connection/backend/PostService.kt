package com.games.commonappsstuff.connection.backend

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PostService {

    @GET("/")
    suspend fun sendMessage(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("bundle_id", encoded=true) bundleId: String,
        @Query("push_token", encoded=true) pushToken: String,
        @Query("campaign_name", encoded=true) campaignName: String? = null,
        @Query("campaign_id", encoded=true) campaignId: String? = null,
        @Query("adset_name", encoded=true) adsetName: String? = null,
        @Query("adset_id", encoded=true) adsetId: String? = null,
        @Query("adgroup_name", encoded=true) adgroupName: String? = null,
        @Query("adgroup_id", encoded=true) adgroupId: String? = null,
        @Query("af_channel", encoded=true) af_channel: String? = null,
        @Query("user_country", encoded=true) user_country: String? = null) : Response<ResponseEntity>

}