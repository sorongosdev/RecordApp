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

    private val redPaint = Paint().apply{
        color = Color.RED
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for(rectF in rectList){
            canvas?.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {

        ampList.add(maxAmplitude/10)
        rectList.clear()

        /**하나의 rect의 width*/
        val rectWidth = 10f
        /**가로에 몇개의 rect?*/
        val maxRect= (this.width/rectWidth).toInt()

        //최신거 업데이트
        val amps = ampList.takeLast(maxRect)

        /**인덱스값과 함께 받아옴*/
        for((i,amp) in amps.withIndex()){
            val rectF = RectF()
            rectF.top = 0f
            rectF.bottom = amp
            rectF.left = i * rectWidth // 오른쪽으로 x좌표가 늘어남
            rectF.right = rectF.left + rectWidth

            rectList.add(rectF)
        }

        invalidate() //ondraw를 다시부름
    }
}