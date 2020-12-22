package com.kingbird.arcfacelogin.utils

import com.kingbird.mylibrary.IFace
import com.orhanobut.logger.Logger
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.iai.v20200303.IaiClient
import com.tencentcloudapi.iai.v20200303.models.*
import org.json.JSONObject
import kotlin.collections.ArrayList


/**
 * 说明： 人脸相关接口
 *
 * @author :Pan Yingdao
 *
 * @date : on 2020/10/30 0030 15:47:39
 */
object FaceRequestIpi {


    fun getPersonListNum(groupId: String): Long {
        var personNum: Long? = null
        try {
            val cred = Credential(IFace.SECRET_ID, IFace.SECRET_KEY)
            val httpProfile = HttpProfile()
            httpProfile.endpoint = IFace.HTTP_PROFILE
            val clientProfile = ClientProfile()
            clientProfile.httpProfile = httpProfile
            val client = IaiClient(cred, Constants.REGION, clientProfile)
            val req = GetPersonListNumRequest()
            req.groupId = groupId
            val resp: GetPersonListNumResponse = client.GetPersonListNum(req)
            Logger.e("人员数量大小请求返回：" + GetPersonListNumResponse.toJsonString(resp))

            personNum = resp.personNum
//            Logger.e("人员数量大小：" + resp.personNum)
        } catch (e: TencentCloudSDKException) {
            Logger.e("人脸表大小获取异常：$e")
        }
        return personNum!!

    }

    /**
     *  创建人员人脸资料
     *   导包303
     */
    fun createPerson(
        groupId: String,
        personName: String,
        personId: String,
        image: String,
        url: String,
        isUrl: Boolean
    ): Boolean {
        try {
            val cred = Credential(IFace.SECRET_ID, IFace.SECRET_KEY)
            val httpProfile = HttpProfile()
            httpProfile.endpoint = IFace.HTTP_PROFILE
            val clientProfile = ClientProfile()
            clientProfile.httpProfile = httpProfile
            val client = IaiClient(cred, Constants.REGION, clientProfile)
            val req = CreatePersonRequest()
            req.groupId = groupId
            req.personName = personName
            req.personId = personId
            req.image = image
            if (isUrl) {
                req.url = url
            }
            val resp: CreatePersonResponse = client.CreatePerson(req)
            Logger.e("创建个人数据结果：" + CreatePersonResponse.toJsonString(resp))

            if (resp.faceId.isNotEmpty()) {
                return true
            }

        } catch (e: TencentCloudSDKException) {
            Logger.e("人脸添加异常：$e")
            if (e.toString().contains("图片中没有人脸")) {
                Logger.e("图片中没有人脸。")
            } else if (e.toString().contains("人员ID已经存在。人员ID不可重复。")) {
                val personIdL: Long = personId.toLong() + 1
                Logger.e("递增1后的人脸ID：$personIdL")
                return createPerson(groupId, personName, personIdL.toString(), image, url, isUrl)
            }
            return false
        }
        return false
    }

    /**
     *  人脸搜索
     *  导包303
     */
    fun searchFaces(groupIds: String, image: String): Boolean {
        try {
            val cred = Credential(IFace.SECRET_ID, IFace.SECRET_KEY)
            val httpProfile = HttpProfile()
            httpProfile.endpoint = IFace.HTTP_PROFILE
            val clientProfile = ClientProfile()
            clientProfile.httpProfile = httpProfile
            val client = IaiClient(cred, Constants.REGION, clientProfile)
            val req = SearchFacesRequest()
            req.groupIds = arrayOf(groupIds)
            req.image = image
            val resp: SearchFacesResponse = client.SearchFaces(req)

//            Logger.e("人脸搜索参数：" + CreatePersonResponse.toJsonString(resp))
            Logger.e("人脸搜索结果：" + resp.results[0].candidates.size)

            val jsonString2 = JSONObject(CreatePersonResponse.toJsonString(resp))
//            Logger.e("数据：" + jsonString2.getJSONArray("Results"))

            val result = jsonString2.getJSONArray("Results").getJSONObject(0)
//            Logger.e("Candidates数据：$result")
            for (i in resp.results[0].candidates.indices) {

                val candidate = result.getJSONArray("Candidates").getJSONObject(i)
//                Logger.e("candidate 数据：$candidate")
//                Logger.e("人脸ID：${candidate.getString("FaceId")}")
//                Logger.e("人员ID：${candidate.getString("PersonId")}")
                val score = candidate.getLong("Score")
                Logger.e("相似度：$score")
                if (score > 75) {
                    return true
                }
            }

        } catch (e: TencentCloudSDKException) {
            Logger.e("人脸搜索异常：$e")
            return false
        }
        return false
    }

    /**
     *  获取人员列表
     */
    fun getPersonList(groupId: String): Long {
        val list = ArrayList<Long>()
        try {
            val cred = Credential(IFace.SECRET_ID, IFace.SECRET_KEY)
            val httpProfile = HttpProfile()
            httpProfile.endpoint = IFace.HTTP_PROFILE
            val clientProfile = ClientProfile()
            clientProfile.httpProfile = httpProfile
            val client = IaiClient(cred, Constants.REGION, clientProfile)
            val req = GetPersonListRequest()
            req.groupId = groupId
            req.offset = 0L
            req.limit = 10L
            val resp = client.GetPersonList(req)
//            Logger.e("获取人员列表数据：" + GetPersonListResponse.toJsonString(resp))
            val jsonString = JSONObject(CreatePersonResponse.toJsonString(resp))
            val personInfos = jsonString.getJSONArray("PersonInfos")
            val personSize = resp.personInfos.size
            for (i in 0 until personSize) {
                val candidate = personInfos.getJSONObject(i)
                Logger.e("循环获取到的PersonId：" + candidate.getLong("PersonId"))
                list.add(candidate.getLong("PersonId"))
            }
            list.sort()

            Logger.e("最后一个PersonId：" + list.last())

            return list.last()

//            System.out.println(GetPersonListResponse.toJsonString(resp))
        } catch (e: TencentCloudSDKException) {
            println(e.toString())
            return 0
        }
//        return 0
    }
}