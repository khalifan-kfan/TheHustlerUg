package com.example.thehustler.NotifyHandler;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotSender {

    public static void sendNotifications(final Context c, String usertoken, String title, String message) {
       ServiceAPI apiService = Client.getClient("https://fcm.googleapis.com/").create(ServiceAPI.class);


        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponce>() {
            @Override
            public void onResponse(Call<MyResponce> call, Response<MyResponce> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {

                        Toast.makeText(c, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponce> call, Throwable t) {

            }
        });
        //Updatetoken();

    }

    public static void Updatetoken() {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
            if(task.isSuccessful()){
                String refreshToken = task.getResult().getToken();
                Token token1= new Token(refreshToken);
                FirebaseFirestore.getInstance().collection("Users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("Tokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .set(token1);
            }else {
                return;
            }
            }
        });
    }
}
