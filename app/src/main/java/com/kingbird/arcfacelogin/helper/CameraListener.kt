package com.kingbird.arcfacelogin.helper

import android.hardware.camera2.CameraDevice
import android.util.Size
import java.lang.Exception


/**
 * 说明：相机监听
 *
 * @author :Pan Yingdao
 * @date : on 2020/11/07
 */
interface CameraListener {

    /**
     * 打开时执行
     *
     * @param cameraDevice    相机实例
     * @param cameraId        相机ID
     * @param isMirror        是否镜像显示
     */
    fun onCameraOpened(cameraDevice: CameraDevice, cameraId: String, previewSize: Size,
                       displayOrientation: Int, isMirror: Boolean)

    /**
     * 预览数据回调
     *
     * @param image     预览的Image对象
     */
    fun onPreview(byteArray: ByteArray)

    /**
     * 当相机关闭时执行
     */
    fun onCameraClosed()

    /**
     * 当出现异常
     */
    fun onCameraError(e: Exception)
}