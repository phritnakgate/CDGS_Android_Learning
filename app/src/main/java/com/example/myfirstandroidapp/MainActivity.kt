package com.example.myfirstandroidapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.landing_page)


        val loginBtn = findViewById<Button>(R.id.lnd_login)
        loginBtn.setOnClickListener {
            Log.d("Login Button", "Login button clicked")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        val signUpBtn = findViewById<Button>(R.id.lnd_register)
        signUpBtn.setOnLongClickListener {
            Log.d("Sign Up Button", "Sign up button long clicked")
            true
        }

        //LOGCAT
//        Log.v("Test Tag Verbose", "MSG")
//        Log.d("Test Tag Debug", "MSG")
//        Log.i("Test Tag Info", "MSG")
//        Log.w("Test Tag Warning", "MSG")
//        Log.e("Test Tag Error", "MSG")
//        Log.wtf("Test Tag Assert", "MSG")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.landing_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}