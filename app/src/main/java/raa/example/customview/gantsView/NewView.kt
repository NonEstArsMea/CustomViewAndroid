package raa.example.customview.gantsView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
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


    private val minRowHight = 150
    private val namesRowHight = 120



    private val rowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val separatorsPaint = Paint().apply {
        strokeWidth = 0f
        color = Color.GRAY
    }

    private val contentWidth: Int
        get() = (periodWidth * 6).toInt()


    private val periodNamePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.gant_period_name_text_size)
        color = ContextCompat.getColor(context, R.color.grey_500)
    }

    // Rect для рисования строк
    private val rowRect = Rect()

    // Чередующиеся цвета строк
    private val rowColors = listOf(
        Color.BLUE,
        Color.GREEN
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            6 * 100
        } else {
            // Даже если AT_MOST занимаем все доступное место, т.к. может быть зум
            MeasureSpec.getSize(widthMeasureSpec)
        }

        // Высота всех строк с тасками + строки с периодами
        val contentHeight = minRowHight * (16) + namesRowHight
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            // Нас никто не ограничивает - занимаем размер контента
            MeasureSpec.UNSPECIFIED -> contentHeight
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


    private fun Canvas.drawRows() {
        repeat(COUNT_OF_DAYS + 1) { index ->
            rowRect.offsetTo(0, index * minRowHight + namesRowHight)
            rowPaint.color = rowColors[index % 2]
            drawRect(rowRect, rowPaint)
        }
    }



    private fun Canvas.drawPeriods() {
        val currentPeriods = listOf("__2 ","___3 ","_____5 ","___3 ","______6 ")
        val nameY = periodNamePaint.getTextBaselineByCenter(minRowHight / 2f)
        var lastX = 0f
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

    private fun Paint.getTextBaselineByCenter(center: Float) = center - (descent() + ascent()) / 2

    override fun onDraw(canvas: Canvas) = with(canvas) {
        drawRows()
        drawPeriods()
    }


    private var timeTable: List<CellClass> = emptyList()
    private var uiTimeTable: List<CellClass> = emptyList()

    private val periodWidth = 100f

    fun setTimeTable(timeTable: List<CellClass>) {
        if (timeTable != this.timeTable) {
            this.timeTable = timeTable
            //uiTimeTable = timeTable.map(::)
            // Сообщаем, что нужно пересчитать размеры
            requestLayout()
            // Сообщаем, что нужно перерисоваться
            invalidate()
        }
    }


//    private inner class UiTask(val task: Task) {
//        // Rect с учетом всех преобразований
//        val rect = RectF()
//
//        // Начальный Rect для текущих размеров View
//        private val untransformedRect = RectF()
//
//        // Если false, таск рисовать не нужно
//        val isRectOnScreen: Boolean
//            get() = rect.top < height && (rect.right > 0 || rect.left < width)
//
//        fun updateInitialRect(index: Int) {
//            fun getX(date: LocalDate): Float? {
//                val periodIndex =
//                    periods.getValue(periodType).indexOf(periodType.getDateString(date))
//                return if (periodIndex >= 0) {
//                    periodWidth * (periodIndex + periodType.getPercentOfPeriod(date))
//                } else {
//                    null
//                }
//            }
//
//            untransformedRect.set(
//                getX(task.dateStart) ?: -5f,
//                40f * (index + 1f),
//                getX(task.dateEnd) ?: width.toFloat(),
//                40f * (index + 2f),
//            )
//            rect.set(untransformedRect)
//        }
//    }

    companion object {
        const val COUNT_OF_DAYS = 16
        private const val MAX_SCALE = 2f
    }
}