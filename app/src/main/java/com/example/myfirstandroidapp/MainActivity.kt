package com.example.myfirstandroidapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.basic_components)

        val txtV = findViewById<TextView>(R.id.bsc_txt)
        txtV.text = "Welcome to Android Development"


        //LOGCAT
        Log.v("Test Tag Verbose", "MSG")
        Log.d("Test Tag Debug", "MSG")
        Log.i("Test Tag Info", "MSG")
        Log.w("Test Tag Warning", "MSG")
        Log.e("Test Tag Error", "MSG")
        Log.wtf("Test Tag Assert", "MSG")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.basic_comp)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}