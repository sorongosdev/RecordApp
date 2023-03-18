package com.sorongos.recordapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**소리데이터, 계속 추가됨*/
    private val ampList = mutableListOf<Float>()

    /**그려질 애들의 데이터*/
    private val rectList = mutableListOf<RectF>()
    private val rectWidth = 15f
    private var tick = 0

    private val redPaint = Paint().apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (rectF in rectList) {
            canvas?.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {

        val height = this.height
        //amplitude 보정치
        val amplitude = maxAmplitude / Short.MAX_VALUE * this.height * 0.8f

        ampList.add(amplitude)
        rectList.clear()

        /**가로에 몇개의 rect?*/
        val maxRect = (this.width / rectWidth).toInt()

        //최신거 업데이트
        val amps = ampList.takeLast(maxRect)

        /**인덱스값과 함께 받아옴*/
        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2 // 중간으로 보내고, 위로 절반 올라감
            rectF.bottom = rectF.top + amp
            rectF.left = i * rectWidth // 오른쪽으로 x좌표가 늘어남
            rectF.right = rectF.left + (rectWidth - 5f)

            rectList.add(rectF)
        }

        invalidate() //ondraw를 다시부름
    }

    /**녹음한 거 재생*/
    fun replayAmplitude() {
        rectList.clear()

        val maxRect = (this.width / rectWidth).toInt()
        val amps = ampList.take(tick).takeLast(maxRect)

        /**리스트 재구성*/
        for ((i, amp) in amps.withIndex()) {
            val rectF = RectF()
            rectF.top = (this.height / 2) - amp / 2
            rectF.bottom = rectF.top + amp
            rectF.left = i * rectWidth // 오른쪽으로 x좌표가 늘어남
            rectF.right = rectF.left + (rectWidth - 5f)

            rectList.add(rectF)
        }

        tick++

        invalidate()
    }

    fun clearData() {
        ampList.clear()
    }

    fun clearWave() {
        rectList.clear()
        tick = 0
        invalidate()
    }
}