package com.example.myfirstandroidapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.basic_components)

        val usrTxtEdit = findViewById<EditText>(R.id.bsc_editUsername)
        val passTxtEdit = findViewById<EditText>(R.id.bsc_editPwd)
        val clickMeBtn = findViewById<Button>(R.id.bsc_btn)
        val shUsrPwd = findViewById<TextView>(R.id.bsc_usrpwd)
        val sharedPref = getSharedPreferences("LOGIN_DATA", MODE_PRIVATE)
        shUsrPwd.text = "${sharedPref.getString("username","username")} | ${sharedPref.getString("username","password")}"

        clickMeBtn.setOnClickListener {
            val username = usrTxtEdit.text.toString()
            val password = passTxtEdit.text.toString()
            Log.d("Login Clicked", "Username: $username, Password: $password")
            //Store in SharedPreferences
            sharedPref.edit() {
                putString("username", username)
                putString("password", password)
            }

        }

    }
}