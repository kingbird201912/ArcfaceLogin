package com.kingbird.arcfacelogin.utils

import android.content.Context
import android.content.SharedPreferences
import com.kingbird.arcfacelogin.application.FaceApplication

/**
 * 数据存储类
 *
 * @author panyingdao
 * @date 2020-9-19.
 */
object SpUtil {

    private val sharePreferences: SharedPreferences
        get() = FaceApplication.getContext()
            .getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)

    fun readBoolean(key: String?): Boolean {
        return sharePreferences.getBoolean(key, false)
    }

    fun readBooleanByTrueDefualt(key: String?): Boolean {
        return sharePreferences.getBoolean(key, true)
    }

    @JvmStatic
    fun writeBoolean(key: String?, value: Boolean) {
        sharePreferences.edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun readInt(key: String?): Int {
        return sharePreferences.getInt(key, 0)
    }

    @JvmStatic
    fun writeInt(key: String?, value: Int) {
        sharePreferences.edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun readString(key: String?): String? {
        return sharePreferences.getString(key, "")
    }

    @JvmStatic
    fun writeString(key: String?, value: String?) {
        sharePreferences.edit().putString(key, value).apply()
    }

}