package com.kingbird.arcfacelogin.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.baidu.aip.util.Base64Util
import com.kingbird.arcfacelogin.utils.FaceRequestIpi
import com.kingbird.arcfacelogin.R
import com.kingbird.arcfacelogin.activity.PermissionsActivity
import com.kingbird.arcfacelogin.helper.CameraHelper
import com.kingbird.arcfacelogin.helper.CameraListener
import com.kingbird.arcfacelogin.manager.ThreadManager
import com.kingbird.arcfacelogin.utils.Constants
import com.kingbird.arcfacelogin.utils.SpUtil
import com.kingbird.arcfacelogin.utils.ToastUtil
import com.kingbird.arcfacelogin.widget.CircleTextureBorderView
import com.kingbird.arcfacelogin.widget.RoundTextureView
import com.orhanobut.logger.Logger
import kotlin.math.min

/**
 * 说明：
 *
 * @author :Pan Yingdao
 * @date : on 2020/12/14 0014 11:56:44
 */
class ArcfaceView : ConstraintLayout, CameraListener {

    private var mContext: Context? = null
    private var mActivity: Activity? = null
    private var mCameraHelper: CameraHelper? = null
    lateinit var mBtnSubmit: Button
    lateinit var mBtnRegister: Button
    lateinit var mBorderView: CircleTextureBorderView
    private var mTextureView: RoundTextureView? = null
    private var CAMERA_ID = CameraHelper.CAMERA_ID_FRONT
    private var mIsTakingPhoto = false
    private var type = 2

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    fun initAdvertising(activity: Activity) {
        mActivity = activity
    }

    //初始化UI，可根据业务需求设置默认值。
    private fun initView(context: Context) {
        mContext = context
        Logger.e("context对象：$mContext")
        LayoutInflater.from(context).inflate(R.layout.activity_face, this, true)

        mTextureView = findViewById<View>(R.id.round_texture_view) as RoundTextureView
        mBorderView = findViewById<View>(R.id.border_view) as CircleTextureBorderView

        mBtnSubmit = findViewById<View>(R.id.btn_submit) as Button
        mBtnRegister = findViewById<View>(R.id.btn_register) as Button

        // 扫描动画开关
        mBorderView.setScanEnabled(true)

        mBtnSubmit.setOnClickListener {
            openSubmit()
        }

        mTextureView!!.setOnClickListener {
            mBtnSubmit.isEnabled = true
            setResumePreview()
        }

        mBtnRegister.setOnClickListener {
            openRegister()
        }

        mTextureView!!.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mTextureView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val params = mTextureView!!.layoutParams
                val sideLength = min(mTextureView!!.width, mTextureView!!.height * 3 / 4)
                params.width = sideLength
                params.height = sideLength
                mTextureView!!.layoutParams = params
                mTextureView!!.turnRound()
                mBorderView.setCircleTextureWidth(sideLength)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (SpUtil.readBoolean(Constants.PERMISSONS)) {
                        initCamera()
                        Logger.e("已获得相关权限")
                    } else {
                        val intent = Intent(mContext, PermissionsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mContext!!.startActivity(intent)
                        Logger.e("开始申请权限：$mContext")
                    }
                } else {
                    SpUtil.writeBoolean(Constants.PERMISSONS, true)
                    initCamera()
                    Logger.e("不需要申请")
                }

            }
        })
    }

    /**
     *  开启人脸注册
     */
    fun openRegister() {
        type = 1
        mIsTakingPhoto = true
        mBorderView.setTipsText("提交数据中...", true)
        mCameraHelper?.takePhoto()
    }

    /**
     *  开始人脸认证
     */
    fun openSubmit() {
        type = 2
        mIsTakingPhoto = true
        mBorderView.setTipsText("提交数据中...", true)
        mCameraHelper?.takePhoto()
        mBtnSubmit.isEnabled = false
    }

     fun initCamera() {
        val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
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
            .mContext(mContext!!)
            .previewOn(mTextureView!!)
            .previewViewSize(
                Point(
                    mTextureView!!.layoutParams.width,
                    mTextureView!!.layoutParams.height
                )
            )
            .rotation(mActivity!!.windowManager?.defaultDisplay?.rotation ?: 0)
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
            val params = mTextureView?.layoutParams
            // 横屏
            if (displayOrientation % 180 == 0) {
                if (params != null) {
                    params.height = params.width * previewSize.height / previewSize.width
                }
            }
            // 竖屏
            else {
                if (params != null) {
                    params.height = params.width * previewSize.width / previewSize.height
                }
            }
            if (mTextureView != null) {
                mTextureView!!.layoutParams = params
            }
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
//                        Toast.makeText(
//                            mContext, "人脸添加成功！", Toast.LENGTH_LONG
//                        ).show()
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        mBorderView.setParam()
                        sendFaceBroadcast("com.kingbird.REGISTER_SUCCESS")
                    }
                } else {
                    post {
//                        Toast.makeText(
//                            mContext, "人脸添加失败！", Toast.LENGTH_LONG
//                        ).show()
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        mBorderView.setParam()
                        sendFaceBroadcast("com.kingbird.REGISTER_FAIL")
                    }
                }
            } else if (type == 2) {
                val result = FaceRequestIpi.searchFaces("2", postImage)
                post {
                    Logger.e("人脸认证最终结果：$result")
                    if (result) {
                        mBtnSubmit.isEnabled = true
                        setResumePreview()
                        switchText("人脸认证成功", true)
                        ToastUtil.showShortToast(mContext!!, "人脸认证成功！")
                        mBorderView.setScanEnabled(true)
                        mBorderView.setParam()
                        sendFaceBroadcast("com.kingbird.VERIFIED_SUCCESS")
                    } else {
                        mBorderView.setScanEnabled(true)
                        setResumePreview()
                        switchText("人脸认证失败", true)
                        sendFaceBroadcast("com.kingbird.VERIFIED_FAIL")
//                        Toast.makeText(
//                            mContext, mContext!!.getString(R.string.search_fail),
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }
            }
        }
    }

    override fun onCameraClosed() {}
    override fun onCameraError(e: Exception) {}

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
     *  发送人脸广播
     */
    private fun sendFaceBroadcast(action: String) {
        val intent = Intent(action)
        mContext!!.sendBroadcast(intent)
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

    companion object {
        private const val TAG = "CombineTextReveal"
    }
}
