package com.example.edgedetection_for_android.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.edgedetection_for_android.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWidgets()
    }

    private fun initWidgets() {
        activity_main_start_process_btn.setOnClickListener {
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }
    }
}