package com.kingbird.arcfacelogin.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.kingbird.arcfacelogin.R
import com.kingbird.arcfacelogin.utils.ScreenUtils
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 说明：扫描效果 View
 *
 * @author :Pan Yingdao
 * @date : on 2020/11/07
 */
class CircleTextureBorderView : View {

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mArcPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mTextureViewWidth: Int = measuredWidth

    private var mColor = Color.CYAN

    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mTipsText: String = "请放入人脸"

    private var mTextHeight = 0F

    private val mAnimPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mAnimWidth = 0F

    private var mLastY = 0F

    private val mAnimator = ValueAnimator.ofFloat(0F, mTextureViewWidth.toFloat(), 0F)

    private var mScanEnabled = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        mPaint.strokeWidth = ScreenUtils.dip2px(context, 3F).toFloat()
        mPaint.style = Paint.Style.STROKE
        mArcPaint.style = Paint.Style.FILL
        mArcPaint.color = Color.parseColor("#7F000000")
        mTextPaint.color = Color.WHITE
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.textSize = ScreenUtils.dip2px(context, 12F).toFloat()
        mTextPaint.strokeWidth = 1F
        mAnimPaint.style = Paint.Style.STROKE
        mAnimPaint.strokeWidth = 10F

        mTextHeight = mTextPaint.fontMetrics.descent - mTextPaint.fontMetrics.ascent
        attributeSet?.apply {
            val a = context.obtainStyledAttributes(this, R.styleable.CircleTextureBorderView)
            mColor = a.getColor(
                R.styleable.CircleTextureBorderView_circleTextureBorderColor,
                Color.CYAN
            )
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setParam()
    }

    fun setParam() {
        if (!mScanEnabled) {
            Log.i("BorderView", "not enable scan animation.")
        }
        val shader = LinearGradient(
            0F, 0F, width.toFloat(), 0F,
            intArrayOf(Color.parseColor("#3FBAF1"), Color.parseColor("#603FBAF1")),
            null, Shader.TileMode.CLAMP
        )
        mAnimPaint.shader = shader

        if (mAnimator.isRunning) {
            mAnimator.cancel()
        }

        mAnimator.setFloatValues(0F, mTextureViewWidth.toFloat(), 0F)
        mAnimator.duration = 2500L
        mAnimator.repeatCount = ValueAnimator.INFINITE
        mAnimator.interpolator = LinearInterpolator()

        mAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            val r = mTextureViewWidth.toFloat() / 2
            val x = (measuredWidth.toFloat() - mAnimWidth) / 2

            if (mAnimWidth != 0F) {
                if (mAnimWidth < value) {
                    // increase
                    mLastY = measuredHeight.toFloat() / 2 - sqrt(
                        r.toDouble().pow(2) - (x - measuredWidth.toFloat() / 2).toDouble().pow(2)
                    ).toFloat()
                } else if (mAnimWidth > value) {
                    // decrease
                    mLastY = measuredHeight.toFloat() / 2 + sqrt(
                        r.toDouble().pow(2) - (x - measuredWidth.toFloat() / 2).toDouble().pow(2)
                    ).toFloat()
                } else if (mAnimWidth == value && mAnimWidth == 2 * r) {
                    mLastY = measuredHeight.toFloat() / 2
                }
            }
            mAnimWidth = value
            postInvalidate()
        }

        mAnimator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (mScanEnabled) {
            val l = (measuredWidth - mAnimWidth) / 2
            canvas?.drawLine(l, mLastY, l + mAnimWidth, mLastY, mAnimPaint)
        }

        mPaint.color = mColor
        val radius = mTextureViewWidth.toFloat() / 2
        canvas?.drawCircle(measuredWidth.toFloat() / 2, measuredWidth.toFloat() / 2, radius, mPaint)
        // 外边框比内部大10-12dp 边框的厚度为1
        mPaint.strokeWidth = 1F
        canvas?.drawCircle(
            measuredWidth.toFloat() / 2, measuredWidth.toFloat() / 2,
            radius + ScreenUtils.dip2px_20(context), mPaint
        )
        mPaint.strokeWidth = ScreenUtils.dip2px(context, 3F).toFloat()

        val left = (measuredWidth - mTextureViewWidth.toFloat()) / 2
        val right = (measuredWidth + mTextureViewWidth.toFloat()) / 2
        val top = (measuredHeight - mTextureViewWidth.toFloat()) / 2
        val bottom = (measuredHeight + mTextureViewWidth.toFloat()) / 2

        canvas?.drawArc(left, top, right, bottom, 150F, -120F, false, mArcPaint)
        canvas?.drawText(
            mTipsText, (measuredWidth.toFloat() - mTextPaint.measureText(mTipsText)) / 2,
            (measuredHeight.toFloat() + mTextureViewWidth / 2) / 2 + mTextHeight * 1.5F, mTextPaint
        )
    }

    fun setTipsText(str: String, stopAnim: Boolean = false) {
        this.mTipsText = str
        if (stopAnim) {
            mAnimator.cancel()
            mScanEnabled = false
        }
        postInvalidate()
    }

    fun setCircleTextureWidth(width: Int) {
        this.mTextureViewWidth = width
        setParam()
    }

    fun setScanEnabled(scanEnabled: Boolean) {
        mScanEnabled = scanEnabled
    }

    fun stop() {
        mScanEnabled = false
        if (mAnimator.isRunning) {
            mAnimator.cancel()
            mAnimator.removeAllUpdateListeners()
        }
    }
}