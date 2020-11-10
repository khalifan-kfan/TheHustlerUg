package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LaunchActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String CurrentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    Tologin();
                }else {
                    CurrentUserId = auth.getCurrentUser().getUid();
                    firestore.collection("Users").document(CurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.isSuccessful()){
                                if(!task.getResult().exists()) {

                                    Intent inforIntent = new Intent(LaunchActivity.this, InforSettings.class);
                                    startActivity(inforIntent);
                                    finish();
                                }
                            }else {
                                String e = task.getException().getMessage();
                                Toast.makeText(LaunchActivity.this,"Error:"+e,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        },1000);

    }

    private void Tologin() {
        Intent login = new Intent(LaunchActivity.this, LoginActivity.class);
        startActivity(login);
        finish();
    }
}