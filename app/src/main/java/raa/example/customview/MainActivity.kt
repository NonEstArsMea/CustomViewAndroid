package raa.example.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import raa.example.customview.gantsView.GantView
import raa.example.customview.gantsView.Task
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}