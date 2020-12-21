package com.example.thehustler.Activities;


import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thehustler.NotifyHandler.Token;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginBtn;
    private Button mCreateBtn;
    private TextView forgetting;

    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailField =  findViewById(R.id.logEmail);
        mPasswordField = findViewById(R.id.logPassword);
        mLoginBtn =  findViewById(R.id.buttonLogin);
        mCreateBtn = findViewById(R.id.button2Create);
        forgetting = findViewById(R.id.forgotPassword);

        mProgressBar =  findViewById(R.id.progressBar_login);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent regIntent = new Intent(LoginActivity.this, CreateActivity.class);
                startActivity(regIntent);

            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    mProgressBar.setVisibility(View.VISIBLE);

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                   checkIfEmailVerified();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                mProgressBar.setVisibility(View.INVISIBLE);

                            }
                        });

                }

            }
        });
        forgetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText reset = new EditText(v.getContext());
                final AlertDialog.Builder passwordReset = new AlertDialog.Builder(v.getContext());
                passwordReset.setTitle("Reset Email");
                passwordReset.setMessage("Enter the email you signed up with");
                passwordReset.setView(reset);
                passwordReset.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String emailadd = reset .getText().toString().trim();
                        mAuth.sendPasswordResetEmail(emailadd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()){
                                  Toast.makeText(LoginActivity.this,"check email to reset",Toast.LENGTH_SHORT).show();
                              }else{
                                Toast.makeText(LoginActivity.this,"Reset email has not been sent,"+task.getException().getMessage(),Toast.LENGTH_LONG ).show();
                              }
                            }
                        });
                    }
                });
                passwordReset.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordReset.create().show();

            }
        });


    }
    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if (user.isEmailVerified())
        {
          /*  mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {
                   // sendToMain();
                    //Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
                    String token = getTokenResult.getToken();
                    String cid = mAuth.getCurrentUser().getUid();
                    Token tok = new Token(token);
                    mFirestore.collection("Users")
                            .document(cid)
                            .collection("Tokens").document(cid)
                            .set(tok);
                }
            });*/

            // user is verified, so you can finish this activity or send user to activity which you want.
            sendToMain();
            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(LoginActivity.this,"check your email " +
                    "and make sure you verified it ",Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("email not verified");
            builder.setMessage("do you want your verification email resent?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    final FirebaseUser user = mAuth.getCurrentUser();
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(LoginActivity.this,"email has been resent to",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this," Some thing went wrong," +
                                    "try again",Toast.LENGTH_LONG).show();
                            // make the resend button visible

                        }
                    });
                }
            });
            // Set the alert dialog no button click listener
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    //restart this activity
                    overridePendingTransition(0, 0);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    private void sendToMain() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String CurrentUserId = mAuth.getCurrentUser().getUid();

            CurrentUserId = mAuth.getCurrentUser().getUid();
            mFirestore.collection("Users").document(CurrentUserId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                if(!task.getResult().exists()) {
                                    Intent inforIntent = new Intent(LoginActivity.this, InforSettings.class);
                                    startActivity(inforIntent);
                                    finish();
                                }else {
                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }else {
                                String e = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error:"+e,Toast.LENGTH_LONG).show();
                            }
                        }
                    });


    }
}
