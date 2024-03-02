package raa.example.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.BLUE
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.animation.doOnRepeat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import raa.example.customview.extencion.dpToPx
import kotlin.math.max
import kotlin.math.truncate
import kotlin.random.Random

@SuppressLint("Recycle")
class AvatarImageView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attr, defStyleAttr) {

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULTS_BORDER_WIDTH)

    @ColorInt
    private var borderColor: Int = Color.GREEN
    private var initials: String = "??"

    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val initialsPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private val borderRect = Rect()

    private var isAvatarMode = true

    private var size = 0

    init {
        if (attr != null) {
            val ta: TypedArray = context.obtainStyledAttributes(attr, R.styleable.AvatarImageView)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageView_borderWidth_shader_s,
                context.dpToPx(DEFAULTS_BORDER_WIDTH)
            )

            borderColor = ta.getColor(
                R.styleable.AvatarImageView_borderColor_shader_s,
                DEFAULTS_BORDER_COLOR
            )

            initials = ta.getString(R.styleable.AvatarImageView_initials_shader_s) ?: "??"
            ta.recycle()
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
        setOnClickListener {
            handleLongClick()
        }
    }

    private fun setup() {

        with(borderPaint) {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
            isAntiAlias = true
        }

    }

    private fun prepareShader(w: Int, h: Int) {

        if (w == 0 || (drawable == null)) return
        val srcBM = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)
        avatarPaint.shader = BitmapShader(srcBM, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val initSize = resolveDefaulteSize(widthMeasureSpec)
        setMeasuredDimension(max(initSize, size), max(initSize, size))
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
        if (w == 0) return

        with(viewRect) {
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareShader(w, h)
    }

    override fun onDraw(canvas: Canvas) {

        if (drawable != null && isAvatarMode) {
            drawAvatar(canvas)
        } else {
            drawInitials(canvas)
        }

        val half = (borderWidth / 2).toInt()
        borderRect.set(viewRect)
        borderRect.inset(half, half)
        canvas.drawOval(borderRect.toRectF(), borderPaint)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (isAvatarMode) prepareShader(width, height)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (isAvatarMode) prepareShader(width, height)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (isAvatarMode) prepareShader(width, height)
    }

    fun setInitials(initials: String) {
        this.initials = initials
        if (!isAvatarMode) {
            invalidate()
        }
    }

    fun setBorderColor(@ColorInt color: Int) {
        borderColor = color
        borderPaint.color = borderColor
        invalidate()
    }

    fun setBorderWidth(@ColorInt color: Int) {
        borderWidth = context.dpToPx(width)
        borderPaint.strokeWidth = borderWidth
        invalidate()
    }

    private fun drawAvatar(canvas: Canvas) {
        canvas.drawOval(viewRect.toRectF(), avatarPaint)
    }

    private fun drawInitials(canvas: Canvas) {
        initialsPaint.color = initialsToColor(initials)
        canvas.drawOval(viewRect.toRectF(), initialsPaint)
        with(initialsPaint) {
            color = WHITE
            textAlign = Paint.Align.CENTER
            textSize = height * 0.33f

        }

        val offset = (initialsPaint.descent() + initialsPaint.ascent()) / 2
        canvas.drawText(
            initials,
            viewRect.exactCenterX(),
            viewRect.exactCenterY() - offset,
            initialsPaint
        )
    }

    private fun handleLongClick(): Boolean {

        val va = ValueAnimator.ofInt(width, width * 2).apply {
            duration = 300
            interpolator = LinearInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1
        }

        va.addUpdateListener {
            size = it.animatedValue as Int
            requestLayout()
        }
        va.doOnRepeat {
            toogleMode()
        }
        va.start()
        return true
    }

    private fun toogleMode() {
        isAvatarMode = !isAvatarMode
        invalidate()
    }

    private fun initialsToColor(letters: String): Int {
        val b = letters[0].toByte()
        val len = bgColors.size
        val d = b / len.toDouble()
        val index = ((d - truncate(d)) * len).toInt()
        return Color.argb(
            255,
            Random.nextInt(0, 255),
            Random.nextInt(0, 255),
            Random.nextInt(0, 255)
        )

    }

    override fun onSaveInstanceState(): Parcelable? {
        val saveState = SavedState(super.onSaveInstanceState())
        saveState.isAvatarMode = isAvatarMode
        saveState.borderWidth = borderWidth
        saveState.borderColor = borderColor
        return saveState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            isAvatarMode = state.isAvatarMode
            borderWidth = state.borderWidth
            borderColor = state.borderColor
        } else {
            super.onRestoreInstanceState(state)
        }

    }

    companion object {
        private const val DEFAULTS_BORDER_WIDTH = 4
        private const val DEFAULTS_BORDER_COLOR = Color.GREEN
        private const val DEFAULTS_SIZE = 120

        private val bgColors = arrayOf(
            Color.parseColor("#7BC862"),
            Color.parseColor("#EBC862"),
            Color.parseColor("#FBC862"),
            Color.parseColor("#6BC862"),
            Color.parseColor("#4BC862"),
            Color.parseColor("#1BC862"),
        )
    }

    private class SavedState : BaseSavedState, Parcelable {
        var isAvatarMode: Boolean = true
        var borderWidth: Float = 0f
        var borderColor: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            isAvatarMode = parcel.readInt() == 1
            borderWidth = parcel.readFloat()
            borderColor = parcel.readInt()
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (isAvatarMode) 1 else 0)
            dst.writeFloat(borderWidth)
            dst.writeInt(borderColor)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}