package com.example.ggestagram

import android.view.View

abstract class DoubleClickListener : View.OnClickListener {
    private var lastClickTime: Long = 0
    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()
        // 마지막으로 클릭한 시간과의 차이가 DOUBLE_CLICK_TIME_DELTA 상수 값보다 작으면 더블 클릭으로 판단하고 onDoubleClick 함수를 호출합니다.
        // 그렇지 않으면 마지막 클릭 시간을 갱신
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }
    abstract fun onDoubleClick(v: View)
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds 300 밀리초(0.3초)
    }
}

