package com.kingbird.arcfacelogin.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.Toast
import com.baidu.aip.util.Base64Util
import com.kingbird.arcfacelogin.FaceRequestIpi
import com.kingbird.arcfacelogin.R
import com.kingbird.arcfacelogin.activity.PermissionsActivity
import com.kingbird.arcfacelogin.application.FaceApplication
import com.kingbird.arcfacelogin.helper.CameraHelper
import com.kingbird.arcfacelogin.helper.CameraListener
import com.kingbird.arcfacelogin.manager.ThreadManager
import com.kingbird.arcfacelogin.utils.Constants
import com.kingbird.arcfacelogin.utils.SpUtil
import com.kingbird.arcfacelogin.utils.SpUtil.readBoolean
import com.kingbird.arcfacelogin.utils.SpUtil.writeBoolean
import com.kingbird.arcfacelogin.widget.CircleTextureBorderView
import com.kingbird.arcfacelogin.widget.RoundTextureView
import com.orhanobut.logger.Logger
import com.orhanobut.logger.Logger.e
import kotlinx.android.synthetic.main.activity_face.view.*
import java.io.File
import kotlin.math.min

/**
 * 说明：
 *
 * @author :Pan Yingdao
 * @date : on 2020/12/14 0014 11:56:44
 */
class ArcfaceView : View, CameraListener {

    companion object {
        private const val TAG = "ArcfaceView"
        private var CAMERA_ID = CameraHelper.CAMERA_ID_FRONT
        private const val CAMERA_ID_BACK = CameraHelper.CAMERA_ID_BACK
    }

    private lateinit var mTextureView: RoundTextureView
    private lateinit var mBorderView: CircleTextureBorderView
    private lateinit var mBtnSubmit: Button

    private var mCameraHelper: CameraHelper? = null

    private var mIsTakingPhoto = false
    private var type = 2
    private lateinit var mContext: Context;
    private lateinit var mActivity: Activity;


    constructor(context: Context) : super(context) {
        init(context)
    }


    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init(context)
    }

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributes,
        defStyleAttr
    ) {
        init(context)

    }

    fun initAdvertising(activity: Activity) {
        mActivity = activity
//        Base.setActivity(activity)
        init(activity)
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            if (readBoolean(Constants.PERMISSONS)) {
//                startAdvertisting()
                startCamera()
                e("已获得相关权限")
            } else {
                val intent = Intent(activity, PermissionsActivity::class.java)
                activity.startActivity(intent)
                e("开始申请权限")
            }
        } else {
            writeBoolean(Constants.PERMISSONS, true)
//            startAdvertisting()
            startCamera()
            e("不需要申请")
        }
    }

//    fun startAdvertisting() {
//        createFile()
//        if (activity == null) {
//            activity = getActivity()
//        }
//        if (isFirstStart(getActivity())) {
//            addLitepal()
//        }
//        init(getActivity())
//        var cpuId: String = readString(activity, Const.CPU_ID)
//        if (cpuId.isEmpty()) {
//            cpuId = MacUtil.getCpuId(activity)
//            SharedPreferencesUtils.writeString(activity, Const.CPU_ID, cpuId)
//        } else {
//            e("CPUID已存在：$cpuId")
//        }
//        CustomActivityManager.getInstance().setTopActivity(activity)
//        initView(activity)
//    }

    private fun init(context: Context) {
        mContext = context
        mTextureView = findViewById(R.id.round_texture_view)
        mBorderView = findViewById(R.id.border_view)

        mBtnSubmit = findViewById(R.id.btn_submit)

        // 扫描动画开关
        mBorderView.setScanEnabled(true)

        mBtnSubmit.setOnClickListener {
            type = 2
            mIsTakingPhoto = true
            mBorderView.setTipsText("提交数据中...", true)
            mCameraHelper?.takePhoto()
            mBtnSubmit.isEnabled = false
        }

        mTextureView.setOnClickListener {
            mBtnSubmit.isEnabled = true
            setResumePreview()
        }

        btn_register.setOnClickListener {
            type = 1
            mIsTakingPhoto = true
            mBorderView.setTipsText("提交数据中...", true)
            mCameraHelper?.takePhoto()
        }

        mTextureView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mTextureView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val params = mTextureView.layoutParams
                val sideLength = min(mTextureView.width, mTextureView.height * 3 / 4)
                params.width = sideLength
                params.height = sideLength
                mTextureView.layoutParams = params
                mTextureView.turnRound()
                mBorderView.setCircleTextureWidth(sideLength)

//                PermissionX.init(mActivity)
//                    .permissions(
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.CAMERA
//                    )
//                    .request { allGranted, _, deniedList ->
//                        if (allGranted) {
//                startCamera()
//                        } else {
//                            Toast.makeText(
//                                mContext,
//                                "These permissions are denied: $deniedList",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }

            }
        })
    }

    /**
     *  相机相关初始化
     */
    fun startCamera() {
        createFile()
        initCamera()
    }

    /**
     *  创建本地文件夹
     */
    private fun createFile() {
        val systemUrl: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContext.getExternalFilesDir(null)!!.path
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

    /**
     *  更新提示文字
     */
    private fun switchText(shadowContent: String, stopAnim: Boolean = false) {
        post {
            if (shadowContent.isNotEmpty()) {
                mBorderView.setTipsText(shadowContent, stopAnim)
            }
        }
    }

    fun onResume() {
        Log.e(TAG, "main currentThread = ${Thread.currentThread().name}")
        if (!mIsTakingPhoto) {
            mCameraHelper?.start()
        }
    }

    fun onPause() {
        if (!mIsTakingPhoto) {
            mCameraHelper?.stop()
        }
    }

    fun onDestroy() {
        mCameraHelper?.release()
        mBorderView.stop()
    }

    private fun initCamera() {
        val manager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
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
            .mContext(FaceApplication.getContext())
            .previewOn(mTextureView)
            .previewViewSize(
                Point(
                    mTextureView.layoutParams.width,
                    mTextureView.layoutParams.height
                )
            )
            .rotation(FaceApplication.getActivity().windowManager?.defaultDisplay?.rotation ?: 0)
            .build()
        mCameraHelper?.start()
        switchText("请点把人脸保持在框内")
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
        post {
            // 将预览控件和预览尺寸比例保持一致 避免拉伸
            val params = mTextureView.layoutParams
            // 横屏
            if (displayOrientation % 180 == 0) {
                params.height = params.width * previewSize.height / previewSize.width
            }
            // 竖屏
            else {
                params.height = params.width * previewSize.width / previewSize.height
            }
            mTextureView.layoutParams = params
        }
    }

    override fun onPreview(byteArray: ByteArray) {
        Log.i(TAG, "onPreview: ")
        post {
            switchText("检测人脸中..", true)
        }

        // 这里通过Base64转换类将图像数据转换格式 因为SDK检测的都用BASE64的图片
        val postImage = Base64Util.encode(byteArray)
        Logger.e("Base64 大小：${postImage!!.length}")
        ThreadManager.doExecute {
            if (type == 1) {
                val personNum = FaceRequestIpi.getPersonListNum("2")
                Logger.e("personNum: $personNum")
                val person = FaceRequestIpi.getPersonList("2")
                val personNumS: String = (person + 1).toString()
                Logger.e("新增人员ID：$personNumS")

                val result = FaceRequestIpi.createPerson(
                    "2",
                    "某人",
                    personNumS,
                    postImage,
                    "",
                    false
                )
                if (result) {
                    post {
                        mBorderView.setTipsText("人脸添加成功！", true)
                        Toast.makeText(
                            mContext, "人脸添加成功！", Toast.LENGTH_LONG
                        ).show()
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        mBorderView.setParam()
                    }
                } else {
                    post {
                        Toast.makeText(
                            mContext, "人脸添加失败！", Toast.LENGTH_LONG
                        ).show()
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        mBorderView.setParam()
                    }
                }
            } else if (type == 2) {
                val result = FaceRequestIpi.searchFaces("2", postImage)
                post {
                    Logger.e("人脸搜索最终结果：$result")
                    if (result) {
                        mBtnSubmit.isEnabled = true
                        setResumePreview()
                        switchText("人脸验证成功", true)
                        Toast.makeText(
                            mContext, "人脸搜索成功！", Toast.LENGTH_LONG
                        ).show()
                        mBorderView.setScanEnabled(true)
                        mBorderView.setParam()
                    } else {
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        switchText("人脸验证失败", true)
                        Toast.makeText(
                            mContext, mContext.getString(R.string.search_fail),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }
    }

    override fun onCameraClosed() {
        TODO("Not yet implemented")
    }

    override fun onCameraError(e: Exception) {
        TODO("Not yet implemented")
    }
}