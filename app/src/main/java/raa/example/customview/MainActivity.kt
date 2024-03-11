package raa.example.customview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import raa.example.customview.gantsView.GantView
import raa.example.customview.gantsView.NewView
import raa.example.customview.gantsView.Task
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = findViewById<NewView>(R.id.gant)

        view.setTimeTable(
            listOf(
                CellClass(
                    subject = "Автомобильная подготовка",
                    teacher = "Кузнецов Е . В .",
                    classroom = "1 / 220",
                    studyGroup = "213",
                    date = "4 - 03 - 2024",
                    subjectType = "6.4 Групповое занятие",
                    startTime = "09:00",
                    endTime = "10:30",
                    subjectNumber = 1,
                    noEmpty = true,
                    text = null,
                    lessonTheme = null,
                    color = 2131231033,
                    viewType = null,
                    viewSize = null,
                    isGone = true,
                    department = "34"
                ),
                CellClass(
                    subject = "Автомобильная подготовка",
                    teacher = "Кузнецов Е . В .",
                    classroom = "1 / 220",
                    studyGroup = "213",
                    date = "4 - 03 - 2024",
                    subjectType = "6.4 Групповое занятие",
                    startTime = "09:00",
                    endTime = "10:30",
                    subjectNumber = 2,
                    noEmpty = true,
                    text = null,
                    lessonTheme = null,
                    color = 2131231033,
                    viewType = null,
                    viewSize = null,
                    isGone = true,
                    department = "34"
                ),
                CellClass(
                    subject = "Автомобильная подготовка",
                    teacher = "Кузнецов Е . В .",
                    classroom = "1 / 220",
                    studyGroup = "213",
                    date = "4 - 03 - 2024",
                    subjectType = "6.4 Групповое занятие",
                    startTime = "09:00",
                    endTime = "10:30",
                    subjectNumber = 3,
                    noEmpty = true,
                    text = null,
                    lessonTheme = null,
                    color = 2131231033,
                    viewType = null,
                    viewSize = null,
                    isGone = true,
                    department = "34"
                ),
                CellClass(
                    subject = "Автомобильная подготовка",
                    teacher = "Кузнецов Е . В .",
                    classroom = "1 / 220",
                    studyGroup = "213",
                    date = "4 - 03 - 2024",
                    subjectType = "6.4 Групповое занятие",
                    startTime = "09:00",
                    endTime = "10:30",
                    subjectNumber = 5,
                    noEmpty = true,
                    text = null,
                    lessonTheme = null,
                    color = 2131231033,
                    viewType = null,
                    viewSize = null,
                    isGone = true,
                    department = "34"
                )
            )
        )
    }
}

data class CellClass(
    var subject: String? = null,
    var teacher: String? = null,
    var classroom: String? = null,
    var studyGroup: String? = null,
    var date: String? = null,
    var subjectType: String? = null,
    var startTime: String? = null,
    var endTime: String? = null,
    var subjectNumber: Int? = null,
    var noEmpty: Boolean,
    var text: String? = null,
    var lessonTheme: String? = null,
    var color: Int = Color.YELLOW,
    val viewType: Int? = null,
    val viewSize: Int? = null,
    var isGone: Boolean = true,
    var department: String? = null,
)