package raa.example.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View

class CustomView(context: Context) : View(context) {

    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = 30f
    }

    private val text1 = "Hello"
    private val text2 = "World!"

    private var staticLayout1: StaticLayout? = null
    private var staticLayout2: StaticLayout? = null

    init {
        staticLayout1 = StaticLayout.Builder.obtain(text1, 0, text1.length, textPaint, 400)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .build()

        staticLayout2 = StaticLayout.Builder.obtain(text2, 0, text2.length, textPaint, 400)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .build()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(400, staticLayout1?.height?.plus(staticLayout2?.height ?: 0) ?: 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate(0f, staticLayout1?.height?.toFloat() ?: 0f)

        staticLayout1?.draw(canvas)
        staticLayout2?.draw(canvas)
    }
}