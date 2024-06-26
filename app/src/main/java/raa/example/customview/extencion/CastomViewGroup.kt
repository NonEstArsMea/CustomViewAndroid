package raa.example.customview.extencion

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class CastomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val firstChild: View?
        get() = if (childCount > 0) getChildAt(0) else null
    private val secondChild: View?
        get() = if (childCount > 1) getChildAt(1) else null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        firstChild?.let { measureChild(it, widthMeasureSpec) }
        firstChild?.let { measureChild(it, widthMeasureSpec) }

        val firstWidth = firstChild?.measuredWidth ?: 0
        val firstHeigth = firstChild?.measuredHeight ?: 0
        val secondWidth = secondChild?.measuredWidth ?: 0
        val secondHeigth = secondChild?.measuredHeight ?: 0

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val childrenOnSameLine =
            firstWidth + secondWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED

        val width = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> firstWidth + secondWidth
            MeasureSpec.EXACTLY -> widthSize

            MeasureSpec.AT_MOST -> {
                if (childrenOnSameLine) {
                    firstWidth + secondWidth
                } else {
                    max(firstWidth, secondWidth)
                }
            }

            else -> error("onMeasure error by you")
        }

        val height = if (childrenOnSameLine) {
            max(firstHeigth, secondHeigth)
        } else {
            firstHeigth + secondHeigth
        }

        setMeasuredDimension(width, height)
    }

    private fun measureChild(it: View, widthMeasureSpec: Int) {
        val specSize = MeasureSpec.getSize(widthMeasureSpec)

        val childWidthSpec = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> widthMeasureSpec
            MeasureSpec.EXACTLY -> widthMeasureSpec
            MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.AT_MOST)
            else -> error("---------")
        }

        val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        it.measure(childWidthSpec, childHeightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        firstChild?.layout(
            0,
            0,
            firstChild?.measuredWidth ?: 0,
            firstChild?.measuredHeight ?: 0,
        )

        secondChild?.layout(
            r - l - (secondChild?.measuredWidth ?: 0),
            b - t - (secondChild?.measuredHeight ?: 0),
            r - l,
            b - t
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

}

//private val firstChild: View?
//    get() = if (childCount > 0) getChildAt(0) else null
//private val secondChild: View?
//    get() = if (childCount > 1) getChildAt(1) else null
//
//override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//    checkChildCount()
//
//    firstChild?.let { measureChild(it, widthMeasureSpec) }
//    secondChild?.let { measureChild(it, widthMeasureSpec) }
//
////        val firstWidth = firstChild?.measuredWidth ?: 0
////        val firstHeight = firstChild?.measuredHeight ?: 0
////        val secondWidth = secondChild?.measuredWidth ?: 0
////        val secondHeight = secondChild?.measuredHeight ?: 0
////
////        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
////        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
////
////        val childrenOnSameLine = firstWidth + secondWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED
////        val width = when (widthMode) {
////            MeasureSpec.UNSPECIFIED -> firstWidth + secondWidth
////            MeasureSpec.EXACTLY -> widthSize
////
////            MeasureSpec.AT_MOST -> {
////                if (childrenOnSameLine) {
////                    firstWidth + secondWidth
////                } else {
////                    max(firstWidth, secondWidth)
////                }
////            }
////
////            else -> error("Unreachable")
////        }
////
////        val height = if (childrenOnSameLine) {
////            max(firstHeight, secondHeight)
////        } else {
////            firstHeight + secondHeight
////        }
////
////        setMeasuredDimension(width, height)
//}
//
//override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//    firstChild?.layout(
//        0,
//        0,
//        firstChild?.measuredWidth ?: 0,
//        firstChild?.measuredHeight ?: 0
//    )
//    secondChild?.layout(
//        r - l - (secondChild?.measuredWidth ?: 0),
//        b - t - (secondChild?.measuredHeight ?: 0),
//        r - l,
//        b - t
//    )
//}
//
//private fun measureChild(child: View, widthMeasureSpec: Int) {
//    val specSize = MeasureSpec.getSize(widthMeasureSpec)
//
//    val childWidthSpec = when (MeasureSpec.getMode(widthMeasureSpec)) {
//        MeasureSpec.UNSPECIFIED -> widthMeasureSpec
//        MeasureSpec.AT_MOST -> widthMeasureSpec
//        MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(specSize, MeasureSpec.AT_MOST)
//        else -> error("Unreachable")
//    }
//    val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
//
//    child.measure(childWidthSpec, childHeightSpec)
//}
//
//private fun checkChildCount() {
//    if (childCount > 2) error("CustomViewGroup should not contain more than 2 children")
//}
//
//}