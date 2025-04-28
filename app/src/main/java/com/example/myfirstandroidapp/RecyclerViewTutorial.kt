package com.example.myfirstandroidapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewTutorial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val attractionsList: MutableList<TravelData> by lazy {
            arrayListOf(
                TravelData(
                    "เกาะสมุย",
                    "อำเภอเกาะสมุย สุราษฎร์ธานี",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTX22c-UvSzxFwk-0BlsEIHEKgeRSBFHmyxKFZZs6ZRhzNvqoMU"
                ),
                TravelData(
                    "นํ้าตกห้วยแม่ขมิ้น",
                    "ตำบล แม่กระบุง อำเภอศรีสวัสดิ์ กาญจนบุรี",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcQxwx9zsPP7uY9-GCDVeuV0TCK0S_OIHxGhl9v5b0NljMg4vu3d"
                ),
                TravelData(
                    "ภูกระดึง",
                    "อำเภอภูกระดึง จังหวัดเลย",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTBQ0JtPrQwiCEY8_aX3VJPc9LiDLoekkjq9Iwov9WvCBNHBIhA"
                ),
                TravelData(
                    "เขาค้อ",
                    "อำเภอเขาค้อ จังหวัดเพชรบูรณ์",
                    "https://i0.wp.com/travelblog.expedia.co.th/wp-content/uploads/2017/12/cover-%E0%B9%80%E0%B8%82%E0%B8%B2%E0%B8%84%E0%B9%89%E0%B8%AD.jpg?resize=1140%2C550&ssl=1"
                ),
                TravelData(
                    "อุทยานแห่งชาติภูสอยดาว",
                    "ตำบล ห้วยมุ่น อำเภอนํ้าปาด อุตรดิตถ์",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcREP_PeFZ8Qh7XTCUaZxGv-DEEuxhrS6NsUodm3cpPddfodCR_A"
                )
            )
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.recycler_view_tutorial)

        val rv = findViewById<RecyclerView>(R.id.myRecyclerView)
        rv.layoutManager = LinearLayoutManager(applicationContext)
        rv.adapter = RecyclerAdapter(attractionsList)
        rv.setHasFixedSize(true) //Use when know how long data will be
        rv.setItemViewCacheSize(20)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerViewTutorial)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}