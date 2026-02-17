package com.ruoyi.mobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MonitorActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)
        
        supportActionBar?.title = "系统监控"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        startSimulatingData()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun startSimulatingData() {
        val runnable = object : Runnable {
            override fun run() {
                updateProgress(R.id.progress_cpu, R.id.text_cpu)
                updateProgress(R.id.progress_memory, R.id.text_memory)
                // Disk usually doesn't change fast, so maybe skip or update rarely
                // updateProgress(R.id.progress_disk, R.id.text_disk)
                
                handler.postDelayed(this, 3000) // Update every 3 seconds
            }
        }
        handler.post(runnable)
    }

    private fun updateProgress(progressId: Int, textId: Int) {
        val progressBar = findViewById<ProgressBar>(progressId)
        val textView = findViewById<TextView>(textId)

        // Simulate some fluctuation around the current value
        var current = progressBar.progress
        val change = random.nextInt(11) - 5 // -5 to +5
        current += change
        if (current < 0) current = 0
        if (current > 100) current = 100

        progressBar.progress = current
        textView.text = "$current%"
    }
}