package raa.example.customview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import raa.example.customview.extencion.dpToPx

@SuppressLint("Recycle")
class CastomView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attr, defStyleAttr) {

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULTS_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = Color.GREEN
    private var initials: String = "??"

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()

    private lateinit var resultBM: Bitmap
    private lateinit var maskBM: Bitmap
    private lateinit var srctBM: Bitmap

    init {
        if (attr != null) {
            val ta: TypedArray = context.obtainStyledAttributes(attr, R.styleable.CastomView)
            borderWidth = ta.getDimension(
                R.styleable.CastomView_borderWidth,
                context.dpToPx(DEFAULTS_BORDER_WIDTH)
            )

            borderColor = ta.getColor(
                R.styleable.CastomView_borderColor,
                DEFAULTS_BORDER_COLOR
            )

            initials = ta.getString(R.styleable.CastomView_initials) ?: "??"
            ta.recycle()
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    private fun setup() {
        with(maskPaint) {
            color = Color.RED
            style = Paint.Style.FILL
        }

        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareBitmaps(w: Int, h: Int) {
        maskBM = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBM = maskBM.copy(Bitmap.Config.ARGB_8888, true)
        val maskCanvas = Canvas(maskBM)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        srctBM = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultBitmap = Canvas(resultBM)

        resultBitmap.drawBitmap(maskBM, viewRect, viewRect, null)
        resultBitmap.drawBitmap(srctBM, viewRect, viewRect, maskPaint)
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.e("ViewMask", "onAttachedToWindow")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val initSize = resolveDefaulteSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
        Log.e(
            "ViewMask", """onMeasure
            ${resolveDefaulteSize(widthMeasureSpec)}
            ${initSize}
        """.trimMargin()
        )
    }

    private fun resolveDefaulteSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.AT_MOST -> context.dpToPx(DEFAULTS_SIZE).toInt()
            MeasureSpec.UNSPECIFIED -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(w == 0) return

        with(viewRect){
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareBitmaps(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        Log.e("ViewMask", "onDraw")

        canvas.drawBitmap(maskBM, viewRect, viewRect, null)

        val half = (borderWidth / 2). toInt()
        viewRect.inset(half, half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
    }

    companion object {
        private const val DEFAULTS_BORDER_WIDTH = 4
        private const val DEFAULTS_BORDER_COLOR = Color.GREEN
        private const val DEFAULTS_SIZE = 120

    }
}