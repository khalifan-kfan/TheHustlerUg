package com.example.thehustler.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateActivity extends AppCompatActivity {

    private EditText Regemail,password,conformPassword;
    private Button Create,Login;
    private ProgressBar processes;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        auth = FirebaseAuth.getInstance();

        Regemail = findViewById(R.id.regEmail);
        password = findViewById(R.id.regPassword);
        conformPassword = findViewById(R.id.comformpassword);
        Create = findViewById(R.id.button2Create);
        Login = findViewById(R.id.back_to_login);
        processes= findViewById(R.id.progressBar_Create);


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Regemail.getText().toString();
                String pass = password.getText().toString();
                String confpass= conformPassword.getText().toString();

                if(!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confpass)){

                    if(pass.equals(confpass)){

                        processes.setVisibility(View.VISIBLE);
                        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    sendtosetup();
                                }else{
                                    String e = task.getException().getMessage();
                                    Toast.makeText(CreateActivity.this,"error:"+e,Toast.LENGTH_LONG).show();
                                }

                                processes.setVisibility(View.INVISIBLE);
                            }
                        });


                    }else{

                        Toast.makeText(CreateActivity.this,"passwords must be the same",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });



    }

    private void sendtosetup() {
        Intent infor = new Intent(CreateActivity.this,InforSettings.class);
        startActivity(infor);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Tomain();
        }
    }

    private void Tomain() {
        Intent main = new Intent(CreateActivity.this,MainActivity.class);
        startActivity(main);
        finish();

    }
}
