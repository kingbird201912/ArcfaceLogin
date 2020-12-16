package com.kingbird.arcfacelogin.manager

import com.orhanobut.logger.Logger
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.util.concurrent.*

/**
 * 线程管理类
 * @author Pan
 * @date 2020/9/18
 */
object ThreadManager  {

    private val executorService: ExecutorService

    fun doExecute(runnable: Runnable) {
        executorService.execute(runnable)
    }

    fun <T> submit(task: Callable<T>): Future<T> {
        return executorService.submit(task)
    }

    fun shutdown() {
        if (executorService.isShutdown) {
            executorService.shutdown()
        }
    }

    init {
        val numberOfCores = Runtime.getRuntime().availableProcessors()
        Logger.e("线程数：$numberOfCores")
        val keepAliveTimeUnit = TimeUnit.SECONDS
        val taskQueue: BlockingQueue<Runnable> =
            LinkedBlockingQueue()
        val namedThredFactory: ThreadFactory =
            BasicThreadFactory.Builder().namingPattern("threadmanager-pool-%d").build()
        executorService = ThreadPoolExecutor(
            numberOfCores,
            numberOfCores * 8,
            0L,
            keepAliveTimeUnit,
            taskQueue,
            namedThredFactory,
            ThreadPoolExecutor.AbortPolicy()
        )
    }
}