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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
        /*
        FirebaseAuth.AuthStateListener listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    // NOTE: this Activity should get onpen only when the user is not signed in, otherwise
                    // the user will receive another verification email.
                    sendVerificationEmail();
                } else {
                    // User is signed out
                }
                // ...
            }
        };
*/

        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Regemail.getText().toString();
                final String pass = password.getText().toString();
                String confpass= conformPassword.getText().toString();

                if(!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confpass)){
                    if(pass.equals(confpass)){
                        processes.setVisibility(View.VISIBLE);
                        createAccount(email,pass);
                       // sendVerificationEmail(email,pass);

                    }else{
                        Toast.makeText(CreateActivity.this,"passwords must be the same",Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(CreateActivity.this,"fill all the fields please",Toast.LENGTH_LONG).show();
                }
            }
        });



    }

    private void sendVerificationEmail() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
         if (user == null) throw new AssertionError();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                processes.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()){
                    Toast.makeText(CreateActivity.this,"email has been sent ",Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    sendtosetup();

                }else {
                    Toast.makeText(CreateActivity.this," Some thing went wrong," +
                            "click create account again to retry",Toast.LENGTH_LONG).show();
                    // make the resend button visible
                    // email not sent, so display message and restart the activity or do whatever you wish to do
                    //restart this activity
                    overridePendingTransition(0, 0);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());

                }

            }
        });

    }

    private void createAccount(String email,String pass){
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CreateActivity.this,"account has been created email being sent",Toast.LENGTH_LONG).show();
                    sendVerificationEmail();
                }else{
                    String e = task.getException().getMessage();
                    Toast.makeText(CreateActivity.this,"error:"+e,Toast.LENGTH_LONG).show();
                    processes.setVisibility(View.INVISIBLE);
                }

               // processes.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void sendtosetup() {
        Intent infor = new Intent(CreateActivity.this,LoginActivity.class);
        startActivity(infor);
        finish();
    }
    /*
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
                Intent main = new Intent(CreateActivity.this,MainActivity.class);
                startActivity(main);
        }

    }
*/
}
