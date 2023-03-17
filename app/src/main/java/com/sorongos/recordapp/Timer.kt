package com.sorongos.recordapp

import android.os.Looper

class Timer(listener: OnTimerTickListener) {
    //thread와 차이?
    /**100ms 마다 초기화*/
    private var duration: Long = 0L
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val runnable: Runnable = object: Runnable{
        override fun run() {
            duration += 40L
            handler.postDelayed(this, 100L)
            listener.onTick(duration)
        }
    }

    /**처음 실행c*/
    fun start(){
        handler.postDelayed(runnable, 40L)
    }
    fun stop(){
        handler.removeCallbacks(runnable)
    }
}

interface OnTimerTickListener{
    fun onTick(duration: Long)
}