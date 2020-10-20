package com.games.commonappsstuff.connection.backend

import com.google.gson.annotations.SerializedName

data class ResponseEntity(@SerializedName("result")val result: Boolean, @SerializedName("message")val message: String?)

data class ApiKeysEntity(@SerializedName("result")val result: Boolean,
                         @SerializedName("appsflyer_api_key")val appsFlyerApiKey: String,
                         @SerializedName("user_x_api_key")val userXApiKey: String,
                         @SerializedName("amplitude_api_key")val amplitudeApiKey: String)

data class PopupInfoEntity(@SerializedName("result")val result: Boolean,
                           @SerializedName("popup_text")val popupText: String? = null,
                           @SerializedName("switch_text")val switchText: String? = null,
                           @SerializedName("error_text")val errorText: String? = null)