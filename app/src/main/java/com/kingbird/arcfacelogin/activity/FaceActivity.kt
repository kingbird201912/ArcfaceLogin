package com.kingbird.arcfacelogin.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.aip.util.Base64Util
import com.kingbird.arcfacelogin.utils.FaceRequestIpi
import com.kingbird.arcfacelogin.R
import com.kingbird.arcfacelogin.helper.CameraHelper
import com.kingbird.arcfacelogin.helper.CameraListener
import com.kingbird.arcfacelogin.manager.ThreadManager
import com.kingbird.arcfacelogin.utils.Constants
import com.kingbird.arcfacelogin.utils.PermissionsUtils
import com.kingbird.arcfacelogin.utils.SpUtil
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_face.*
import java.io.File
import kotlin.math.min

class FaceActivity : AppCompatActivity(), CameraListener {

    companion object {
        private const val TAG = "MainActivity"
        private var CAMERA_ID = CameraHelper.CAMERA_ID_FRONT
        private const val CAMERA_ID_BACK = CameraHelper.CAMERA_ID_BACK
    }

    private var mCameraHelper: CameraHelper? = null

    private var mIsTakingPhoto = false
    private var type = 2

    fun startCameraActivity(activity: Context) {
        val intent = Intent(activity, FaceActivity::class.java)
        activity.startActivity(intent)
    }

    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face)
        initViews()
    }

    private fun initViews() {
        // 扫描动画开关
        border_view.setScanEnabled(true)
        btn_submit.visibility = View.VISIBLE
        btn_register.visibility = View.VISIBLE

        btn_submit.setOnClickListener {
            type = 2
            mIsTakingPhoto = true
            border_view.setTipsText("提交数据中...", true)
            mCameraHelper?.takePhoto()
            btn_submit.isEnabled = false
        }

        round_texture_view.setOnClickListener {
            btn_submit.isEnabled = true
            setResumePreview()
        }

        btn_register.setOnClickListener {
            type = 1
            mIsTakingPhoto = true
            border_view.setTipsText("提交数据中...", true)
            mCameraHelper?.takePhoto()
        }

        round_texture_view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                round_texture_view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val params = round_texture_view.layoutParams
                val sideLength = min(round_texture_view.width, round_texture_view.height * 3 / 4)
                params.width = sideLength
                params.height = sideLength
                round_texture_view.layoutParams = params
                round_texture_view.turnRound()
                border_view.setCircleTextureWidth(sideLength)

                PermissionsUtils.chekPermissions(this@FaceActivity, permissions, permissionsResult)

            }
        })
    }

    /**
     *  创建本地文件夹
     */
    private fun createFile() {
        val systemUrl: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getExternalFilesDir(null)!!.path
        } else {
            Constants.ROOT_DIRECTORY_URL
        }
        SpUtil.writeString(Constants.YA_SUO_IMAGE, systemUrl)
        Logger.e("获取外置SD路径$systemUrl")

        val appDir = File(systemUrl!!)
        if (!appDir.exists()) {
            val isSuccess = appDir.mkdirs()
            Logger.e("创建情况$isSuccess")
        }
    }

    /**
     *  相机重置
     */
    private fun setResumePreview() {
        this.mIsTakingPhoto = false
        switchText("请点把人脸保持在框内")
        // 先stop再start 重置一下参数
        mCameraHelper?.stop()
        mCameraHelper?.start()
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "main currentThread = ${Thread.currentThread().name}")
        if (!mIsTakingPhoto) {
            mCameraHelper?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!mIsTakingPhoto) {
            mCameraHelper?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCameraHelper?.release()
        border_view.stop()
    }

    private fun initCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraIds = manager!!.cameraIdList
        Logger.e("设备摄像头数目：${cameraIds.size}")
        if (cameraIds.isNotEmpty()) {
            try {
                //后置摄像头存在
                if (cameraIds[0] != null) {
                    Logger.e("后置摄像头")
                    CAMERA_ID = CameraHelper.CAMERA_ID_BACK
                }
                if (cameraIds[1] != null) {
                    CAMERA_ID = CameraHelper.CAMERA_ID_FRONT
                    Logger.e("前置摄像头")
                }
            } catch (e: java.lang.Exception) {
                Logger.e("异常原因：$e")
            }
        } else {
            Logger.e("没有摄像头")
        }
        mCameraHelper = CameraHelper.Companion.Builder()
            .cameraListener(this)
            .specificCameraId(CAMERA_ID)
            .mContext(applicationContext)
            .previewOn(round_texture_view)
            .previewViewSize(
                Point(
                    round_texture_view.layoutParams.width,
                    round_texture_view.layoutParams.height
                )
            )
            .rotation(windowManager?.defaultDisplay?.rotation ?: 0)
            .build()
        mCameraHelper?.start()
        switchText("请点把人脸保持在框内")
    }

    override fun onCameraClosed() {

    }

    override fun onCameraError(e: Exception) {

    }

    override fun onCameraOpened(
        cameraDevice: CameraDevice,
        cameraId: String,
        previewSize: Size,
        displayOrientation: Int,
        isMirror: Boolean
    ) {
        Log.i(TAG, "onCameraOpened:  previewSize = ${previewSize.width}  x  ${previewSize.height}")
        // 相机打开时，添加右上角的view用于显示原始数据和预览数据
        runOnUiThread {
            // 将预览控件和预览尺寸比例保持一致 避免拉伸
            val params = round_texture_view.layoutParams
            // 横屏
            if (displayOrientation % 180 == 0) {
                params.height = params.width * previewSize.height / previewSize.width
            }
            // 竖屏
            else {
                params.height = params.width * previewSize.width / previewSize.height
            }
            round_texture_view.layoutParams = params
        }

    }

    override fun onPreview(byteArray: ByteArray) {
        Log.i(TAG, "onPreview: ")
        runOnUiThread {
            switchText("检测人脸中...", true)
        }

        // 这里通过Base64转换类将图像数据转换格式 因为SDK检测的都用BASE64的图片
        val postImage = Base64Util.encode(byteArray)
        Logger.e("Base64 大小：${postImage!!.length}")
        ThreadManager.doExecute {
            if (type == 1) {
                faceRegsiter("2", "某人", postImage, "", false)
            } else if (type == 2) {
                faceVerified("2", postImage)
            }
        }
    }

    /**
     *  人脸注册
     */
    private fun faceRegsiter(
        groupId: String,
        personName: String,
        postImage: String,
        url: String,
        isUrl: Boolean
    ) {
        val personNum = FaceRequestIpi.getPersonListNum("2")
        Logger.e("personNum: $personNum")
        val person = FaceRequestIpi.getPersonList("2")
        val personNumS: String = (person + 1).toString()
        Logger.e("新增人员ID：$personNumS")

        val result = FaceRequestIpi.createPerson(
            groupId,
            personName,
            personNumS,
            postImage,
            url,
            isUrl
        )
        if (result) {
            runOnUiThread {
                border_view.setTipsText("人脸注册成功！", true)
                border_view.setScanEnabled(true)
                setResumePreview()
                border_view.setParam()
                sendFaceBroadcast("com.kingbird.REGISTER_SUCCESS")
                finish()
            }
        } else {
            runOnUiThread {
                border_view.setTipsText("人脸注册失败！", true)
                border_view.setScanEnabled(true)
                setResumePreview()
                border_view.setParam()
                sendFaceBroadcast("com.kingbird.REGISTER_FAIL")
            }
        }
    }

    /**
     *  人脸认证
     */
    private fun faceVerified(groupIds: String, postImage: String) {
        val result = FaceRequestIpi.searchFaces(groupIds, postImage)
        runOnUiThread {
            Logger.e("人脸搜索最终结果：$result")
            if (result) {
                btn_submit.isEnabled = true
                setResumePreview()
                switchText("人脸认证成功", true)
                border_view.setScanEnabled(true)
                border_view.setParam()
                sendFaceBroadcast("com.kingbird.VERIFIED_SUCCESS")
                finish()
            } else {
                border_view.setScanEnabled(true)
                setResumePreview()
                switchText("人脸认证失败", true)
                sendFaceBroadcast("com.kingbird.VERIFIED_FAIL")
                Toast.makeText(
                    this@FaceActivity, getString(R.string.search_fail),
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    /**
     *  发送人脸广播
     */
    private fun sendFaceBroadcast(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    /**
     *  更新提示文字
     */
    private fun switchText(shadowContent: String, stopAnim: Boolean = false) {
        runOnUiThread {
            if (shadowContent.isNotEmpty()) {
                border_view.setTipsText(shadowContent, stopAnim)
            }
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
            SpUtil.writeBoolean(Constants.PERMISSONS, true)
            initCamera()
            createFile()
            val packageManager: PackageManager = packageManager
            val intent = SpUtil.readString(
                Constants.MAIN_APP_NAME
            )?.let {
                packageManager.getLaunchIntentForPackage(
                    it
                )
            }
            intent?.let { startActivity(it) }
        }

        override fun forbitPermissons() {
            PermissionsUtils.chekPermissions(this@FaceActivity, permissions, this)
        }
    }

}
