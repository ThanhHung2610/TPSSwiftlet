package com.example.tpsembedding.HouseManager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tpsembedding.CurrentActivityTracker
import com.example.tpsembedding.R

class ScanningActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
    }

    override fun onResume() {
        super.onResume()
        CurrentActivityTracker.currentActivity = this
    }
    override fun onPause() {
        super.onPause()
        if (CurrentActivityTracker.currentActivity == this) {
            CurrentActivityTracker.currentActivity = null
        }
    }
}
