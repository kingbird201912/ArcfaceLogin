package com.kingbird.arcfacelogin.utils

import android.app.Activity
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * 说明：
 *
 * @author :Pan Yingdao
 * @date : on 2020/11/05 0005 16:46:08
 */
object BaseUtil {


    private var mActivity: Activity? = null

    fun setActivity(activity: Activity?) {
        mActivity = activity
    }

    fun getActivity(): Activity? {
        return mActivity
    }

    /*
   * bitmap转base64
   * */
    fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.flush()
                baos.close()
                val bitmapBytes = baos.toByteArray()
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }
}