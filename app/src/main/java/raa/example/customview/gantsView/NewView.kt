package raa.example.customview.gantsView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.content.ContextCompat
import raa.example.customview.CellClass
import raa.example.customview.R
import java.lang.Integer.max
import java.time.LocalDate
import kotlin.time.times

class NewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Основная информация об строках и колонках
    private val minRowHight = 250
    private val namesRowHight = 170
    private val columnWidth = 400f

    private var dateTextSize = 200f

    private val contentWidth: Int
        get() = max(width, (columnWidth * 6 + dateTextSize).toInt())
    private val contentHeight: Int
        get() = height + 500


    // Отвечает за зум и сдвиги
    private val transformations = Transformations()

    // Радиус скругления углов таски
    private val taskCornerRadius = resources.getDimension(R.dimen.gant_task_corner_radius)

    // Вертикальный отступ таски внутри строки
    private val taskVerticalMargin = resources.getDimension(R.dimen.gant_task_vertical_margin)

    // Для фигур тасок
    private val taskShapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GRAY
    }

    // Значения последнего эвента
    private val lastPoint = PointF()
    private var lastPointerId = 0

    private val timeStartOfLessonsList = listOf(
        "9:00", "10:30",
        "10:45", "12:15",
        "12:30", "14:00",
        "14:15", "16:15",
        "16:25", "17:55",
    )


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
        rowRect.set(
            /* left = */ 0,
            /* top = */0,
            /* right = */w,
            /* bottom = */ minRowHight
        )

    }

    private fun Canvas.drawTimeAndDateLine() {
        // Линия для отделения времени
        drawLine(
            dateTextSize * transformations.scaleFactor + transformations.translationX,
            0f,
            dateTextSize * transformations.scaleFactor + transformations.translationX,
            height.toFloat(),
            mainSeparatorsPaint
        )

        // Линия для отделения даты
        drawLine(
            0f,
            namesRowHight * transformations.scaleFactor + transformations.translationY,
            width.toFloat(),
            namesRowHight * transformations.scaleFactor + transformations.translationY,
            mainSeparatorsPaint
        )
    }


    private fun Canvas.drawRowsAndDates() {


        var lastY = namesRowHight.toFloat() * transformations.scaleFactor
        val lastX = transformations.translationX

        var rowHeight: Int
        val paddingLeftAndRight = 5
        var textY: Float
        var textX: Float

        val texts = listOf(
            "Текст \n12345678910112\n1314151617181920212223242526",
            "Текст 2",
            "Текст 3",
            "Текст \n" +
                    "12345678910112\n" +
                    "1314151617181920212223242526",
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

            rowHeight = if (minRowHight > staticLayout.height) {
                minRowHight
            } else {
                staticLayout.height
            }


            rowRect.set(
                /* left = */ 0,
                /* top = */
                (lastY + transformations.translationY).toInt(),
                /* right = */
                width,
                /* bottom = */
                (lastY + (rowHeight * transformations.scaleFactor) + transformations.translationY).toInt()
            )

            rowPaint.color = rowColors[index % 2]
            drawRect(rowRect, rowPaint)

            val lesson = LessonsRect("noLesson", 2, lastY.toInt(), rowHeight)
            if (!lesson.isRectVisible) {
                lesson.updateInitialRect()
                lesson.draw(this)
            }


            textY =
                (lastY + (rowHeight - staticLayout.height) * transformations.scaleFactor / 2) + transformations.translationY
            textX = paddingLeftAndRight.toFloat() + lastX

            this.save()
            this.translate(textX, textY)
            this.scale(transformations.scaleFactor, transformations.scaleFactor)
            staticLayout.draw(this)
            this.restore()

            lastY += (rowHeight * transformations.scaleFactor)

        }

    }


    private fun Canvas.drawPeriods() {
        val currentPeriods = listOf("12 ", "123 ", "123\n456", "___3 ", "______6 ")
        var textY: Float
        var textX: Float

        var lastX = dateTextSize * transformations.scaleFactor + transformations.translationX

        val layoutColumnWidth = columnWidth * transformations.scaleFactor

        currentPeriods.forEachIndexed { index, periodName ->
            // По X текст рисуется относительно его начала
            val staticLayout = StaticLayout.Builder.obtain(
                periodName, 0, periodName.length, dateNamePaint,
                layoutColumnWidth.toInt()
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()

            textY =
                ((namesRowHight - staticLayout.height) / 2 * transformations.scaleFactor) + transformations.translationY
            textX = lastX + (columnWidth - staticLayout.width) / 2 * transformations.scaleFactor

            this.save()
            this.translate(textX, textY)
            this.scale(transformations.scaleFactor, transformations.scaleFactor)
            staticLayout.draw(this)
            this.restore()

            lastX += (columnWidth * transformations.scaleFactor)


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


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        return if (event.pointerCount > 1) scaleDetector.onTouchEvent(event) else processMove(event)
    }

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return run {
                    transformations.addScale(detector.scaleFactor)
                    true
                }
            }
        })


    private fun processMove(event: MotionEvent): Boolean {
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

        var scaleFactor = 1.0f
        private val minScaleFactor = 0.5f
        private val maxScaleFactor = 2.0f

        var translationX = 0f
            private set
        var translationY = 0f
            private set

        // На сколько максимально можно сдвинуть диаграмму
        private val minTranslationX: Float
            get() = (width - contentWidth * scaleFactor).coerceAtMost(0f).toFloat()
        private val minTranslationY: Float
            get() = (height - contentHeight * scaleFactor).coerceAtMost(0f).toFloat()

        fun addTranslation(dx: Float, dy: Float) {
            translationX = (translationX + dx).coerceIn(minTranslationX, 0f)
            translationY = (translationY + dy).coerceIn(minTranslationY, 0f)
            Log.e("tag_ Translation", " X $translationX, Y $translationY")
            invalidate()
        }

        fun addScale(sx: Float) {
            scaleFactor = (scaleFactor * sx).coerceIn(minScaleFactor, maxScaleFactor)
            if(scaleFactor > minScaleFactor && scaleFactor < maxScaleFactor){
                translationX = (translationX * sx).coerceIn(minTranslationX, 0f)
                translationY = (translationY * sx).coerceIn(minTranslationY, 0f)
                Log.e("taag of scale", "$scaleFactor , X $translationX, Y $translationY")
                invalidate()
            }

        }


    }

    private inner class LessonsRect(
        val lesson: String,
        val dayOfLesson: Int,
        val lastY: Int,
        val hightOfRow: Int
    ) {

        var rect = RectF()

        var rectRadius = 20f * transformations.scaleFactor
        private var verticalLineSize = 15f * transformations.scaleFactor


        private val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLACK
            strokeWidth = 1f
            setBackgroundColor(Color.WHITE)
        }

        private val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidth
            color = Color.BLACK
        }
        private val strokeWidth = 1f
        private val path = Path()
        private val path2 = Path()

        // Начальный Rect для текущих размеров View
        private val untransformedRect = RectF()

        // Если false, таск рисовать не нужно
        val isRectVisible: Boolean
            get() = (rect.top > height) and (rect.bottom < 0) and (rect.right < width) and (rect.left > 0)

        fun updateInitialRect() {

            fun getX(index: Int): Float {
                return (index * columnWidth) + dateTextSize
            }

            fun getEndX(index: Int): Float {
                return ((index + 1) * columnWidth) + dateTextSize
            }

            untransformedRect.set(
                getX(dayOfLesson) * transformations.scaleFactor + transformations.translationX + 5f,
                lastY.toFloat() + transformations.translationY + 5f,
                getEndX(dayOfLesson) * transformations.scaleFactor + transformations.translationX - 5f,
                (lastY + hightOfRow * transformations.scaleFactor).toFloat() + transformations.translationY - 5f,
            )
            rect.set(untransformedRect)
        }

        fun draw(canvas: Canvas) {
            // Draw rounded rectangle

            paint.color = Color.WHITE
            val strokeRect = RectF(
                rect.left - strokeWidth,
                rect.top - strokeWidth,
                rect.right + strokeWidth,
                rect.bottom + strokeWidth
            )
            // Отрисовка самого прямоугольника
            path.addRoundRect(rect, rectRadius, rectRadius, Path.Direction.CW)
            canvas.drawPath(path, paint)

            paint.color = resources.getColor(R.color.blue_200)
            // Отрисовка боковой части
            with(path) {
                reset()
                addRoundRect(
                    rect.left,
                    rect.top,
                    rect.left + 100f,
                    rect.bottom,
                    rectRadius,
                    rectRadius,
                    Path.Direction.CW
                )
            }

            with(path2) {
                reset()
                addRect(
                    rect.left + verticalLineSize,
                        rect.top,
                        rect.right,
                        rect.bottom, Path.Direction.CW
                )
                op(path, Path.Op.REVERSE_DIFFERENCE)
            }

            canvas.drawPath(path2, paint)
            canvas.drawRoundRect(strokeRect, rectRadius, verticalLineSize, strokePaint)
        }

    }
}