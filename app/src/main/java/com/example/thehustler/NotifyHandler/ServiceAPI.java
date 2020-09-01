package com.example.thehustler.NotifyHandler;



import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ServiceAPI {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAARy6BIVA:APA91bECfmP_WTf4f9B2LITqd1EnpJArUtljfm3fmip8zoUuvMdLkFr02HfYpvydsggGskacoX-HWlNCbR7mrSvUvPIqcYhIUxrZqsICNcwZzVnI67XTKLWYzaHQCQ5p6HFN86EdUWvt"
            }
            )
    @POST("fcm/send")
    Call<MyResponce> sendNotification(@Body NotificationSender body);
}
