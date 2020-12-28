package com.games.commonappsstuff.connection.backend

import retrofit2.Response
import retrofit2.http.*

interface BackendService {

    @GET("/")
    suspend fun sendFirstOpenMessage(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("bundle_id", encoded=true) bundleId: String,
        @Query("push_token", encoded=true) pushToken: String,
        @Query("campaign", encoded=true) campaignName: String?,
        @Query("adset", encoded=true) adsetName: String?,
        @Query("adgroup", encoded=true) adgroupName: String?,
        @Query("af_status", encoded=true) afStatus: String?,
        @Query("user_country", encoded=true) user_country: String?,
        @Query("battery_level") batteryLevel: Int?,
        @Query("is_charging") isCharging: Boolean?,
        @Query("advertising_id", encoded=true) advertisingId: String?) : Response<ResponseEntity>

    @GET("/event")
    suspend fun setNotificationOpenEvent(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("notification_type", encoded=true) notificationType: String?,
        @Query("event_name") eventName: String = "push_open")

    @GET("/api_keys")
    suspend fun getApiKeys(
        @Query("bundle_id", encoded=true) bundleId: String) : Response<ApiKeysEntity>

    @GET("/popup_info")
    suspend fun getPopupInfo(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String) : Response<PopupInfoEntity>

    @GET("/event")
    suspend fun setPaymentPageOpenEvent(
        @Query("appsflyer_id", encoded=true) appsFlyerId: String,
        @Query("event_name") eventName: String = "payment_page_open")

}