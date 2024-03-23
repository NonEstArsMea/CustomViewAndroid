package raa.example.customview.gantsView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
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

    // Основная информация об строках и колонках
    private val minRowHight = 250
    private val namesRowHight = 170
    private val columnWidth = 200f

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

    private fun Canvas.drawTimeAndDateLine() {
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


    private fun Canvas.drawRowsAndDates() {


        var lastY = namesRowHight

        var rowHeight: Int
        val paddingLeftAndRight = 5
        var textY: Float

        val texts = listOf(
            "Текст \n12345678910112\n1314151617181920212223242526",
            "Текст 2",
            "Текст 3",
            "Текст 4",
            "Текст 2",
            "Текст 3",
            "Текст 4"
        )

        repeat(COUNT_OF_LESSONS) { index ->

            val staticLayout = StaticLayout.Builder.obtain(
                texts[index], 0, texts[index].length, dateNamePaint,
                dateTextSize.toInt() - 2 * paddingLeftAndRight
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()

            rowHeight = if (lastY > staticLayout.height) {
                minRowHight
            } else {
                staticLayout.height
            }

            textY = (lastY + (rowHeight - staticLayout.height) / 2).toFloat()

            this.save()
            this.translate(paddingLeftAndRight.toFloat(), textY)
            staticLayout.draw(this)
            this.restore()


            lastY += rowHeight

            rowRect.offsetTo(0, lastY)
            rowPaint.color = rowColors[index % 2]
            drawRect(rowRect, rowPaint)

            drawLine(0f, lastY.toFloat(), 1000f, lastY.toFloat(), mainSeparatorsPaint)


        }

    }


    private fun Canvas.drawPeriods() {
        val currentPeriods = listOf("12 ", "123 ", "123\n456", "___3 ", "______6 ")
        var nameY : Float
        var lastX = dateTextSize
        currentPeriods.forEachIndexed { index, periodName ->
            // По X текст рисуется относительно его начала
            val staticLayout = StaticLayout.Builder.obtain(
                periodName, 0, periodName.length, dateNamePaint,
                columnWidth.toInt()
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()

            nameY = ((namesRowHight - staticLayout.height) / 2).toFloat()

            this.save()
            this.translate(lastX, nameY.toFloat())
            staticLayout.draw(this)
            this.restore()

            lastX += columnWidth


            // Разделитель
            val separatorX = lastX
            drawLine(separatorX, 0f, separatorX, height.toFloat(), separatorsPaint)
        }
    }

    private fun Paint.getTextBaselineByCenter() = (descent() + ascent()) / 2

    override fun onDraw(canvas: Canvas) = with(canvas) {
        drawRowsAndDates()
        drawPeriods()
        drawTimeAndDateLine()
    }


    private var timeTable: List<CellClass> = emptyList()


    fun setTimeTable(timeTable: List<CellClass>) {
        if (timeTable != this.timeTable) {
            this.timeTable = timeTable
            requestLayout()
            invalidate()
        }
    }


    companion object {
        const val COUNT_OF_LESSONS = 6
    }

    private class Transformations{



    }
}