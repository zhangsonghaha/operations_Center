package com.ruoyi.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ruoyi.mobile.databinding.ActivityLoginBinding
import com.ruoyi.mobile.databinding.ActivityHomeBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if already logged in
        val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val savedUrl = prefs.getString("server_url", "http://10.0.2.2:8080")
        
        binding.etServer.setText(savedUrl)
        
        // Only auto-login if URL is set and valid
        // 强制取消自动登录，确保用户手动登录以获取最新 Token
        /* 
        if (token != null && !savedUrl.isNullOrEmpty()) {
            goToHome()
            return
        }
        */

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val serverUrl = binding.etServer.text.toString()
            
            if (serverUrl.isEmpty()) {
                Toast.makeText(this, "Please enter server URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Save URL
            Config.setServerUrl(this, serverUrl.trim())
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            login(username, password, serverUrl.trim())
        }
    }

    private fun login(u: String, p: String, baseUrl: String) {
        try {
            // Ensure URL ends with / and trim whitespace
            var finalUrl = baseUrl.trim()
            if (!finalUrl.endsWith("/")) {
                finalUrl += "/"
            }

            // Add Logging
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(finalUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ApiService::class.java)

            api.login(LoginBody(u, p)).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.token != null) {
                        val token = response.body()!!.token
                        getSharedPreferences("app", Context.MODE_PRIVATE).edit()
                            .putString("token", token)
                            .putString("username", u)
                            .putString("server_url", finalUrl) // Save correct URL
                            .apply()
                        goToHome()
                    } else {
                        // Try to parse error body
                        val errorMsg = try {
                            response.errorBody()?.string() ?: "Unknown Error"
                        } catch (e: Exception) { "Parse Error" }
                        
                        Toast.makeText(this@LoginActivity, "Login Failed: Code ${response.code()}", Toast.LENGTH_LONG).show()
                        println("LOGIN_ERROR: $errorMsg")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Config Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
