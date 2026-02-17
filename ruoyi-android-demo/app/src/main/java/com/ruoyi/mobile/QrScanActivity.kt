package com.ruoyi.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.journeyapps.barcodescanner.CaptureActivity

class QrScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startScanner()
    }

    private fun startScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("请扫描电脑端登录二维码")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.captureActivity = PortraitCaptureActivity::class.java
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "取消扫码", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                val uuid = result.contents
                handleScannedUuid(uuid)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    
    private fun handleScannedUuid(uuid: String) {
        val baseUrl = Config.getServerUrl(this)
        if (baseUrl.isEmpty()) {
            Toast.makeText(this, "服务器地址未配置", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val finalUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        // Add Token Interceptor
        val token = getSharedPreferences("app", Context.MODE_PRIVATE).getString("token", "") ?: ""
        val client = okhttp3.OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(finalUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)

        // 1. Notify Backend: Scanned
        println("Scanning QR: $uuid with token: $token")
        
        // 强制打印完整的 URL 方便调试
        println("Request URL: ${retrofit.baseUrl()}login/qr/scan")
        
        api.scanQr(QrBody(uuid)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showConfirmDialog(api, uuid)
                } else {
                    println("Scan Failed: ${response.code()} ${response.errorBody()?.string()}")
                    val msg = when(response.code()) {
                        401 -> "登录已过期，请重新登录"
                        404 -> "接口未找到 (404)"
                        500 -> "服务器错误 (500)"
                        else -> "请求失败 (${response.code()})"
                    }
                    Toast.makeText(this@QrScanActivity, msg, Toast.LENGTH_LONG).show()
                    if (response.code() == 401) {
                         // Token 失效，退出登录
                         getSharedPreferences("app", Context.MODE_PRIVATE).edit().remove("token").apply()
                         // 跳转回登录页 (可选)
                         startActivity(Intent(this@QrScanActivity, LoginActivity::class.java))
                    }
                    finish()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@QrScanActivity, "网络连接失败: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
    
    private fun showConfirmDialog(api: ApiService, uuid: String) {
        val username = getSharedPreferences("app", Context.MODE_PRIVATE).getString("username", "") ?: ""
        
        AlertDialog.Builder(this)
            .setTitle("确认登录")
            .setMessage("确认在电脑端登录账号：$username ？")
            .setPositiveButton("确认登录") { _, _ ->
                confirmLogin(api, uuid, username)
            }
            .setNegativeButton("取消") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun confirmLogin(api: ApiService, uuid: String, username: String) {
        // Log UUID and Username for debugging
        println("Confirming Login - UUID: $uuid, User: $username")
        
        api.confirmQr(QrConfirmBody(uuid, username)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@QrScanActivity, "登录成功！", Toast.LENGTH_SHORT).show()
                } else {
                    // Show error code
                    Toast.makeText(this@QrScanActivity, "确认登录失败: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@QrScanActivity, "网络连接失败: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
}
