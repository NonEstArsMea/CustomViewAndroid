package raa.example.customview.gantsView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import raa.example.customview.CellClass
import raa.example.customview.R
import java.time.LocalDate

class NewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private val minRowHight = 250
    private val namesRowHight = 170

    private val timeStartOfLessonsList = listOf(
        "9:00", "10:30",
        "9:00", "10:30",
        "9:00", "10:30",
        "9:00", "10:30",
        "9:00", "10:30",
    )

    private val dateTextSize = 200f


    private val rowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val separatorsPaint = Paint().apply {
        strokeWidth = 0f
        color = Color.GRAY
    }

    private val mainSeparatorsPaint = Paint().apply {
        strokeWidth = 2f
        color = Color.BLACK
    }


    private val periodNamePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.gant_period_name_text_size)
        color = ContextCompat.getColor(context, R.color.grey_500)
    }

    private val dateNamePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.gant_period_name_text_size)
        isFakeBoldText = true
        color = ContextCompat.getColor(context, R.color.grey_500)
    }

    // Rect для рисования строк
    private val rowRect = Rect()

    // Чередующиеся цвета строк
    private val rowColors = listOf(
        ContextCompat.getColor(context, R.color.red_themes_500),
        Color.WHITE
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            6 * 100
        } else {
            // Даже если AT_MOST занимаем все доступное место, т.к. может быть зум
            MeasureSpec.getSize(widthMeasureSpec)
        }

        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            // Нас никто не ограничивает - занимаем размер контента
            MeasureSpec.UNSPECIFIED -> heightSpecSize
            // Ограничение "не больше, не меньше" - занимаем столько, сколько пришло в спеке
            MeasureSpec.EXACTLY -> heightSpecSize
            // Можно занять меньше места, чем пришло в спеке, но не больше
            MeasureSpec.AT_MOST -> heightSpecSize.coerceAtMost(heightSpecSize)
            // Успокаиваем компилятор, сюда не попадем
            else -> error("Unreachable")
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Размер изменился, надо пересчитать ширину строки
        rowRect.set(0, 0, w, minRowHight)

    }

    val builder = StaticLayout.Builder.obtain(" text", 0, " text".length, dateNamePaint,
        dateNamePaint.measureText(" text").toInt()
    )


    private fun Canvas.drawRowsAndDates() {

        val nameTimeY = dateNamePaint.getTextBaselineByCenter()
        var text = ""
        var lastY = namesRowHight
        repeat(COUNT_OF_LESSONS) { index ->
            lastY += minRowHight
            rowRect.offsetTo(0, lastY)
            rowPaint.color = rowColors[index % 2]
            drawRect(rowRect, rowPaint)


            text = timeStartOfLessonsList[index * 2]
            var textWidth = dateNamePaint.measureText(text)
            var textX = (dateTextSize - textWidth) / 2
            var textY = (lastY - namesRowHight * 2 / 3 - nameTimeY)

            drawText(text, textX, textY, dateNamePaint)

            this.save()
            this.translate(paddingLeft.toFloat(), paddingTop.toFloat())
            builder.build().draw(this)
            this.restore()

            drawLine(0f, lastY.toFloat(), 1000f, lastY.toFloat(), mainSeparatorsPaint)



        }
        // Линия для отделения времени
        drawLine(dateTextSize, 0f, dateTextSize, height.toFloat(), mainSeparatorsPaint)

        // Линия для отделения даты
        drawLine(
            0f,
            namesRowHight.toFloat(),
            width.toFloat(),
            namesRowHight.toFloat(),
            mainSeparatorsPaint
        )
    }


    private fun Canvas.drawPeriods() {
        val currentPeriods = listOf("__2 ", "___3 ", "_____5 ", "___3 ", "______6 ")
        val nameY = periodNamePaint.getTextBaselineByCenter()
        var lastX = dateTextSize
        currentPeriods.forEachIndexed { index, periodName ->
            // По X текст рисуется относительно его начала
            val textWidth = periodNamePaint.measureText(periodName)
            val nameX = lastX
            drawText(periodName, nameX, nameY, periodNamePaint)
            lastX += textWidth
            // Разделитель
            val separatorX = lastX
            drawLine(separatorX, 0f, separatorX, height.toFloat(), separatorsPaint)
        }
    }

    private fun Paint.getTextBaselineByCenter() = (descent() + ascent()) / 2

    override fun onDraw(canvas: Canvas) = with(canvas) {
        drawRowsAndDates()
        drawPeriods()
    }


    private var timeTable: List<CellClass> = emptyList()

    private val periodWidth = 100f

    fun setTimeTable(timeTable: List<CellClass>) {
        if (timeTable != this.timeTable) {
            this.timeTable = timeTable
            requestLayout()
            invalidate()
        }
    }


    companion object {
        const val COUNT_OF_LESSONS = 5
    }
}