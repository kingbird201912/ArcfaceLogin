package com.kingbird.arcfacelogin.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy


/**
 *
 * @author Administrator
 * @date 2016/5/9
 */
class FaceApplication : Application() {

    companion object {

        private const val TAG = "MainActivity"

        var context: Application? = null
        var activity = null

        fun getContext(): Context {
            return context!!
        }

        fun getActivity(): Activity {
            return activity!!
        }

        fun initLogger() {
            val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true) //（可选）是否显示线程信息。 默认值为true
                .methodCount(2) // （可选）要显示的方法行数。 默认2
                .methodOffset(0) // （可选）设置调用堆栈的函数偏移值，0的话则从打印该Log的函数开始输出堆栈信息，默认是0
//                .logStrategy(customLog) //（可选）更改要打印的日志策略。 默认LogCat
                .tag("MyTAG") //（可选）每个日志的全局标记。 默认PRETTY_LOGGER（如上图）
                .build()
            Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
//            Logger.addLogAdapter(AndroidLogAdapter())
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        activity = activity
//        LitePal.initialize(this);
        initLogger()

        Log.e(TAG, "初始化")
        Logger.e(TAG, "初始化")
    }


}