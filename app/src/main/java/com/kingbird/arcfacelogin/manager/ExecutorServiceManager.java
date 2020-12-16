package com.kingbird.arcfacelogin.manager;

import com.orhanobut.logger.Logger;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理类
 *
 * @author panyingdao
 * @date 2018/6/12/012.
 */
public class ExecutorServiceManager {

    private static ExecutorServiceManager executorServiceManager;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 线程池参数初始化
     */
    private ExecutorServiceManager() {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        Logger.e("numberOfCores= " + numberOfCores);
        scheduledExecutorService = new ScheduledThreadPoolExecutor(numberOfCores * 6,
                new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d").daemon(true).build());
    }

    /**
     * 单例
     */
    public static ExecutorServiceManager getInstance() {
        if (executorServiceManager == null) {
            synchronized (ThreadManager.class) {
                if (executorServiceManager == null) {
                    executorServiceManager = new ExecutorServiceManager();
                }
            }
        }
        return executorServiceManager;
    }

    /**
     * 创建定时任务
     */
    public void schedule(Runnable runnable,
                         long delay, TimeUnit unit) {
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.schedule(runnable, delay, unit);
        }
    }

    /**
     * 创建周期性定时任务
     */
    public void scheduleAtFixedRate(Runnable command,
                                    long initialDelay,
                                    long period,
                                    TimeUnit unit) {
        try {
            if (!scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建周期性定时任务（又返回值）
     */
    public ScheduledFuture<?> scheduleAtFixedRate2(Runnable command,
                                                   long initialDelay,
                                                   long period,
                                                   TimeUnit unit) {
        try {
            if (!scheduledExecutorService.isShutdown()) {
                return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 停止所以任务
     */
    public void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
