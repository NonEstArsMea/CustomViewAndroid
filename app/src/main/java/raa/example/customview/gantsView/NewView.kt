package raa.example.customview.gantsView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import raa.example.customview.CellClass
import raa.example.customview.R

class NewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Основная информация об строках и колонках
    private val minRowHight = 250
    private val namesRowHight = 170
    private val columnWidth = 400f


    private val contentWidth: Int
        get() = width + 500
    private val contentHeight: Int
        get() = height + 500


    // Отвечает за зум и сдвиги
    private val transformations = Transformations()

    // Значения последнего эвента
    private val lastPoint = PointF()
    private var lastPointerId = 0

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
        drawLine(
            dateTextSize + transformations.translationX,
            0f,
            dateTextSize + transformations.translationX,
            height.toFloat(),
            mainSeparatorsPaint
        )

        // Линия для отделения даты
        drawLine(
            0f,
            namesRowHight.toFloat() + transformations.translationY,
            width.toFloat(),
            namesRowHight.toFloat() + transformations.translationY,
            mainSeparatorsPaint
        )
    }


    private fun Canvas.drawRowsAndDates() {


        var lastY = namesRowHight.toFloat()
        val lastX = transformations.translationX

        var rowHeight: Int
        val paddingLeftAndRight = 5
        var textY: Float
        var textX: Float

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
                /* source = */ texts[index],
                /* start = */  0,
                /* end = */    texts[index].length,
                /* paint = */  dateNamePaint,
                /* width = */  dateTextSize.toInt() - 2 * paddingLeftAndRight
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

            textY = (lastY + (rowHeight - staticLayout.height) / 2) + transformations.translationY
            textX = paddingLeftAndRight.toFloat() + lastX

            this.save()
            this.translate(textX, textY)
            staticLayout.draw(this)
            this.restore()


            lastY += rowHeight

            rowRect.offsetTo(0, lastY.toInt() + transformations.translationY.toInt())
            rowPaint.color = rowColors[index % 2]
            drawRect(rowRect, rowPaint)



        }

    }


    private fun Canvas.drawPeriods() {
        val currentPeriods = listOf("12 ", "123 ", "123\n456", "___3 ", "______6 ")
        var nameY: Float
        var nameX: Float

        var lastX = dateTextSize + transformations.translationX
        var lastY = transformations.translationY

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

            nameY = ((namesRowHight - staticLayout.height) / 2).toFloat() + transformations.translationY
            nameX = lastX

            this.save()
            this.translate(nameX, nameY.toFloat())
            staticLayout.draw(this)
            this.restore()

            lastX += columnWidth


            // Разделитель
            drawLine(lastX, 0f, lastX, height.toFloat(), separatorsPaint)
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


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastPoint.set(event.x, event.y)
                lastPointerId = event.getPointerId(0)
                true
            }

            MotionEvent.ACTION_MOVE -> {

                // Если размер контента меньше размера View - сдвиг недоступен
                if (width < contentWidth) {
                    val pointerId = event.getPointerId(0)
                    // Чтобы избежать скачков - сдвигаем, только если поинтер(палец) тот же, что и раньше
                    if (lastPointerId == pointerId) {
                        transformations.addTranslation(event.x - lastPoint.x, event.y - lastPoint.y)
                    }

                    // Запоминаем поинтер и последнюю точку в любом случае
                    lastPoint.set(event.x, event.y)
                    lastPointerId = event.getPointerId(0)

                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }


    companion object {
        const val COUNT_OF_LESSONS = 6
    }

    private inner class Transformations {
        var translationX = 0f
            private set
        var translationY = 0f
            private set

        // На сколько максимально можно сдвинуть диаграмму
        private val minTranslationX: Float
            get() = (width - contentWidth).coerceAtMost(0).toFloat()
        private val minTranslationY: Float
            get() = (height - contentHeight).coerceAtMost(0).toFloat()

        fun addTranslation(dx: Float, dy: Float) {
            translationX = (translationX + dx).coerceIn(minTranslationX, 0f)
            translationY = (translationY + dy).coerceIn(minTranslationY, 0f)
            invalidate()
        }


    }
}