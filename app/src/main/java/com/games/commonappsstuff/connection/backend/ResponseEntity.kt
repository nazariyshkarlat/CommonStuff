package com.games.commonappsstuff.connection.backend

import com.google.gson.annotations.SerializedName

data class ResponseEntity(@SerializedName("result")val result: Boolean, @SerializedName("message")val message: String)