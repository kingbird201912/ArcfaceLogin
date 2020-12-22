package com.kingbird.arcfacelogin.utils

import android.content.Context
import android.widget.Toast

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.kingbird.arcfacelogin.utils
 * @ClassName:      ToastUtil
 * @Description:    java类作用描述
 * @Author:         pan
 */

class ToastUtil {

    companion object{
         var mToast: Toast ?= null

        /**
         * 弹出短提示
         * @param context 上下文
         * @param message 文本提示
         *
         */
        fun showShortToast(context : Context,message:String){
            showToastMessage(context,message,Toast.LENGTH_SHORT)
         }


        /**
         * 弹出Toast提示
         * @param context 上下文
         * @param message 要显示的message
         * @param duration 时间长短
         *
         */
        fun showToastMessage(context:Context,message: String,duration:Int){
            //?.表示如果为空就走右边
            mToast?:Toast.makeText(context,message,duration)
            mToast?.setText(message)
            mToast?.duration = duration
            mToast?.show()
        }

    }
}