package com.kingbird.arcfacelogin.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.kingbird.arcfacelogin.utils.Constants
import com.kingbird.arcfacelogin.utils.PermissionsUtils
import com.kingbird.arcfacelogin.utils.SpUtil.readString
import com.kingbird.arcfacelogin.utils.SpUtil.writeBoolean
import com.kingbird.arcfacelogin.view.ArcfaceView
import com.orhanobut.logger.Logger

/**
 * @ClassName: PermissionsActivity
 * @Description: 权限申请
 * @Author: Pan
 * @CreateDate: 2019/11/25 10:51
 */
class PermissionsActivity : AppCompatActivity() {

    private var context: Context? = null
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA
    )

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            Logger.e("开始申请权限")
            PermissionsUtils.chekPermissions(this, permissions, permissionsResult)
        } else {
            Logger.e("不需要申请")
            writeBoolean(Constants.PERMISSONS, true)
//            Base.intentActivity("5")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionsUtils.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    /**
     * 创建监听权限的接口对象
     */
    private var permissionsResult: PermissionsUtils.Companion.IPermissionsResult = object :
        PermissionsUtils.Companion.IPermissionsResult {
        override fun passPermissons() {
            Logger.e("权限通过")
            writeBoolean(Constants.PERMISSONS, true)
            val view = ArcfaceView(context!!)
            view.startCamera()
            val packageManager: PackageManager = getPackageManager()
            val intent = readString(
                Constants.MAIN_APP_NAME
            )?.let {
                packageManager.getLaunchIntentForPackage(
                    it
                )
            }
            intent?.let { startActivity(it) }
        }

        override fun forbitPermissons() {
            PermissionsUtils.chekPermissions(this@PermissionsActivity, permissions, this)
        }
    }
}