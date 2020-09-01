package com.example.thehustler.NotifyHandler;



import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.iid.FirebaseInstanceId;

import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;



public class IdTokenService extends  FirebaseMessagingService {

    String refreshToken;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()){
                    refreshToken = task.getResult().getToken();
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
        if(user!=null){
            updateToken(refreshToken);
        }

    }

    private void updateToken(final String refreshToken1) {

        Token token1= new Token(refreshToken1);
        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Tokens").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .set(token1);

    }
}
