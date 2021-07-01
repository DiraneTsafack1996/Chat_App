package com.example.dirane.Fragments

import com.example.dirane.Notifications.MyResponse
import com.example.dirane.Notifications.Sender
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAxNjVIwY:AAAAekRw4wU:APA91bFilxAuwVF1GTAqO_8jGSiSCTPg2n1vJcPJjvrZob3p8CXUYX1RIvohkAfGdnvXeSmfdupdQF3DUryN4CZQZm2MVX4PdGd8251EsKUya5n6K0oWgM7QfL3uy0f6GtCGUmmbOY5_"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?
}