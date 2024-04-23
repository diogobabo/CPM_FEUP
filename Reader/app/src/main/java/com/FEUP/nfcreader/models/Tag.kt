package com.FEUP.nfcreader.models

import org.json.JSONObject

data class TagInfo(
    val tagId: String,
    var status: Boolean,
    val tagType: String,
    val userId: String,
    val payLoad: JSONObject
) {
    companion object {
        fun fromJson(json: String): TagInfo {
            val jsonObject = JSONObject(json)
            return TagInfo(
                jsonObject.getString("tagId"),
                jsonObject.getBoolean("status"),
                jsonObject.getString("tagType"),
                jsonObject.getString("userId"),
                jsonObject.getJSONObject("payLoad")
            )
        }
    }
}

