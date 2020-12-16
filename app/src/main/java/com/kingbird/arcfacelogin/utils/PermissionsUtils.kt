package com.kingbird.arcfacelogin.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.kingbird.arcfacelogin.utils.SpUtil.writeBoolean
import com.orhanobut.logger.Logger
import java.util.*

/**
 * 文件名：权限工具类
 * 创建者：Pan Yingdao
 * 创建日期：2019/6/27/027 13:57
 * 描述：TODO
 *
 * @author Administrator
 */
class PermissionsUtils {

    companion object{
    /**
     * 权限请求码
     */
    private val mRequestCode = 100
     var mPermissionsResult: IPermissionsResult? = null

    fun chekPermissions(
        context: Activity,
        permissions: Array<String>,
        permissionsResult: IPermissionsResult
    ) {
        mPermissionsResult = permissionsResult
        //6.0才用动态权限
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            permissionsResult.passPermissons()
            return
        }

        //创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        val mPermissionList: MutableList<String> = ArrayList()
        //逐个判断你要的权限是否已经通过
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Logger.e("添加的权限：$permission")
                //添加还未授予的权限
                mPermissionList.add(permission)
            }
        }

        //申请权限
        //有权限没有通过，需要申请
        if (mPermissionList.size > 0) {
            Logger.e("开始申请权限：")
            //            ActivityCompat.requestPermissions(context, permissions, mRequestCode);
            context.requestPermissions(permissions, mRequestCode)
        } else {
            //说明权限都已经通过，可以做你想做的事情去
            permissionsResult.passPermissons()
            Logger.e("申请权限通过：")
            writeBoolean(Constants.PERMISSONS, true)
        }
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    fun onRequestPermissionsResult(
        context: Activity, requestCode: Int,
        grantResults: IntArray
    ) {
        //有权限没有通过
        var hasPermissionDismiss = false
        if (mRequestCode == requestCode) {
            for (grantResult in grantResults) {
                if (grantResult == -1) {
                    hasPermissionDismiss = true
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                //跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                showSystemPermissionsSettingDialog(context)
            } else {
                //全部权限通过，可以进行下一步操作。。。
                mPermissionsResult!!.passPermissons()
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    private var mPermissionDialog: AlertDialog? = null
    private fun showSystemPermissionsSettingDialog(context: Activity) {
        val mPackName = context.packageName
        if (mPermissionDialog == null) {
            mPermissionDialog = AlertDialog.Builder(context)
                .setMessage("已禁用权限，请手动授予")
                .setPositiveButton("设置") { dialog, which ->
                    cancelPermissionDialog()
                    val packageUri = Uri.parse("package:$mPackName")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                    context.startActivity(intent)
                    //                            context.finish();
                }
                .setNegativeButton("取消") { dialog, which -> //关闭页面或者做其他操作
                    cancelPermissionDialog()
                    //mContext.finish();
                    mPermissionsResult!!.forbitPermissons()
                }
                .create()
        }
        mPermissionDialog!!.show()
    }

    /**
     * 关闭对话框
     */
    private fun cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog!!.cancel()
            mPermissionDialog = null
        }
    }

    interface IPermissionsResult {
        /**
         * 允许通过
         */
        fun passPermissons()

        /**
         * 禁止通过
         */
        fun forbitPermissons()
    }

//    companion object {
//        private var permissionsUtils: PermissionsUtils? = null
////        val instance: PermissionsUtils?
//            get() {
//                if (permissionsUtils == null) {
//                    permissionsUtils = PermissionsUtils()
//                }
//                return permissionsUtils
//            }
    }
}