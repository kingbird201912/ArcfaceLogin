package com.kingbird.arcfacelogin.utils

import android.os.Environment

/**
 * 说明：
 *
 * @author :Pan Yingdao
 * @date : on 2020/10/29 0029 15:04:21
 */
object Constants {

    @JvmField
    var APP_NAME="arcfaceLogin"

    @JvmField
    var REGION="ap-guangzhou"

    @JvmField
    var YA_SUO_IMAGE="yaSuoIamge"

    /**
     * 申请权限结果
     */
    @JvmField
    var PERMISSONS = "permissons"
    @JvmField
    var MAIN_APP_NAME = "mainName"

    val ROOT_DIRECTORY_URL = Environment.getExternalStorageDirectory().toString() + "/"
}