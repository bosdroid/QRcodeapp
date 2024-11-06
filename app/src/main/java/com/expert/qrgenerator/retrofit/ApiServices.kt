package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.model.FeedbackResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiServices {

    // THIS IS THE POST REQUEST FOR CREATING VCARD QR CODE
    @FormUrlEncoded
    @POST("api/savetrackabledata.php")
    suspend fun saveTrackableData(
        @Field("qr_id") qrId: String,
        @Field("target_url") targetUrl: String):Response<JsonObject?>


    // THIS IS THE POST REQUEST FOR CREATING VCARD QR CODE
    @FormUrlEncoded
    @POST("api/vcard.php")
    suspend fun createVCard(
        @Field("user_id") userId: String,
        @Field("type") type: String,
        @Field("first_name") firstName: String,
                            @Field("last_name") lastName: String,
                            @Field("company_name") companyName: String,
                            @Field("job_title") jobTitle: String,
                            @Field("dob") dob: String,
                            @Field("phone") phone: String,
                            @Field("email") email: String,
                            @Field("website") website: String,
                            @Field("address") address: String,
                            @Field("file_name") fileName: String):Response<JsonObject?>

    // THIS IS THE POST REQUEST FOR UPDATE QR CODE IMAGE
    @Multipart
    @POST("api/updateqrcodeimage.php")
    suspend fun uploadQrCodeImage(@Part image: MultipartBody.Part,
                                  @Part("name") name: RequestBody
                            ):Response<JsonObject?>


    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @FormUrlEncoded
    @POST("api/create_dynamic_qr_code.php")
    suspend fun createDynamicQrCode(@Field("login") login: String,
                                    @Field("qrId") qrId: String,
                                    @Field("userUrl") userUrl: String,
                                    @Field("userType") userType: String,):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/webpage/create/template1")
    suspend fun createCouponQrCode(@Body body: JsonObject):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING FEEDBACK QR CODE
    @POST("service/webpage/create/feedbacktemplate")
    suspend fun createFeedbackQrCode(@Body body: JsonObject):Response<JsonObject?>

    @POST("service/user/google/add")
    suspend fun signUp(@Body body: JsonObject):Response<JsonObject?>

    @GET("service/user/google/{email}")
    suspend fun signIn(@Path("email") email:String):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING SOCIAL NETWORK QR CODE
    @POST("service/webpage/create/sntemplate")
    suspend fun createSnTemplate(@Body body: JsonObject):Response<JsonObject?>

    @GET("service/feedback/{id}")
   suspend fun getAllFeedbacks(@Path("id") id:String):Response<FeedbackResponse?>

}