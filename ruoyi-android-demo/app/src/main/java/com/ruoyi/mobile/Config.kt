package com.ruoyi.mobile

import android.content.Context

object Config {
    fun getServerUrl(context: Context): String {
        return context.getSharedPreferences("app", Context.MODE_PRIVATE)
            .getString("server_url", "") ?: ""
    }

    fun setServerUrl(context: Context, url: String) {
        context.getSharedPreferences("app", Context.MODE_PRIVATE).edit()
            .putString("server_url", url.trim())
            .apply()
    }
}
