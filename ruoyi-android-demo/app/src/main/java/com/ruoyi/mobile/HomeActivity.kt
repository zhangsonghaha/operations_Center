package com.ruoyi.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.android.material.navigation.NavigationView
import com.ruoyi.mobile.databinding.ActivityHomeBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup Drawer Toggle
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup Navigation View
        binding.navView.setNavigationItemSelectedListener(this)
        
        // Fetch User Info to update Drawer Header
        fetchUserInfo()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already on Home
            }
            R.id.nav_scan -> {
                startActivity(Intent(this, QrScanActivity::class.java))
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "系统设置功能开发中", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                // Clear Token
                getSharedPreferences("app", Context.MODE_PRIVATE).edit().remove("token").apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun fetchUserInfo() {
        val baseUrl = Config.getServerUrl(this).trim()
        if (baseUrl.isEmpty()) {
            return
        }
        val finalUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val token = getSharedPreferences("app", Context.MODE_PRIVATE).getString("token", "") ?: ""

        val client = OkHttpClient.Builder().addInterceptor { chain ->
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
        
        api.getInfo().enqueue(object : Callback<UserInfoResponse> {
            override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                if (response.isSuccessful) {
                    val info = response.body()
                    if (info != null && info.user != null) {
                        val user = info.user
                        
                        // Update Drawer Header
                        if (binding.navView.headerCount > 0) {
                            val headerView = binding.navView.getHeaderView(0)
                            val avatarIv = headerView.findViewById<ImageView>(R.id.imageView)
                            val nameTv = headerView.findViewById<TextView>(R.id.nav_header_title)
                            val emailTv = headerView.findViewById<TextView>(R.id.nav_header_subtitle)

                            if (nameTv != null) nameTv.text = user.nickName ?: "Admin"
                            if (emailTv != null) emailTv.text = user.email ?: "admin@ruoyi.vip"

                            // Load Avatar
                            var avatarPath = user.avatar
                            if (!avatarPath.isNullOrEmpty() && avatarIv != null) {
                                val avatarUrl = if (avatarPath.startsWith("http")) {
                                    avatarPath
                                } else {
                                    val base = finalUrl.removeSuffix("/")
                                    val path = if (avatarPath.startsWith("/")) avatarPath else "/$avatarPath"
                                    "$base$path"
                                }
                                
                                val glideUrl = GlideUrl(avatarUrl, LazyHeaders.Builder()
                                        .addHeader("Authorization", "Bearer $token")
                                        .build())

                                try {
                                    Glide.with(this@HomeActivity)
                                        .load(glideUrl)
                                        .placeholder(android.R.drawable.sym_def_app_icon)
                                        .error(android.R.drawable.sym_def_app_icon)
                                        .into(avatarIv)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(this@HomeActivity, "登录过期", Toast.LENGTH_SHORT).show()
                    getSharedPreferences("app", Context.MODE_PRIVATE).edit().remove("token").apply()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                    finish()
                }
            }
            override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                // Ignore errors for now or log them
            }
        })
    }
}