package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import  com.example.thehustler.Adapter.MessageRecycler;
import  com.example.thehustler.Model.Messages;
import  com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private FirebaseFirestore firestore;

    MessageRecycler adapter;
    RecyclerView myRecyclerView;
    Toolbar toolbar;
    TextView mDisplayNameTV;
    ImageView mProfileIV;
    EditText mMessageET;
    ProgressBar sendingProgress;

    private String Myname;
    private String MyuserId;
    private String RecieverUId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbarchat);
        setSupportActionBar(toolbar);

        /*
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signinScreenIntent = new Intent(ChatActivity.this, InforSettings.class);
                startActivity(signinScreenIntent);
            }
        });
*/
        mDisplayNameTV = findViewById(R.id.display_name_text);
        mProfileIV = findViewById(R.id.profile_image);
        mMessageET = findViewById(R.id.message_et);
        sendingProgress = findViewById(R.id.sending_progress);
        sendingProgress.setVisibility(View.INVISIBLE);
        Intent i = getIntent();
        RecieverUId = i.getStringExtra("ReceiverUID");

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Myname = auth.getCurrentUser().getDisplayName();
        MyuserId = auth.getCurrentUser().getUid();

        adapter = new MessageRecycler(MyuserId);
        myRecyclerView = findViewById(R.id.chat_rv);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.setAdapter(adapter);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatter = new Intent(ChatActivity.this, AnotherUserAccount.class);
                chatter.putExtra("UserId",RecieverUId);
                startActivity(chatter);
            }
        });
        FloatingActionButton fab = findViewById(R.id.send_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(ChatActivity.this);
                String text = mMessageET.getText().toString();
                if(!TextUtils.isEmpty(text)){
                    sendingProgress.setVisibility(View.VISIBLE);
                    Messages message = new Messages(MyuserId, Myname, text);
                    sendMessage(message);
                }
            }
        });



    }

    private void sendMessage(final Messages message) {


        firestore.collection("Users/"+RecieverUId+"/Chats").document(MyuserId).collection("Messages")
                .add(message)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        sendingProgress.setVisibility(View.INVISIBLE);
                        if(!task.isSuccessful()){
                            Toast.makeText(ChatActivity.this, "Message sending failed", Toast.LENGTH_SHORT).show();
                        }else{
                            mMessageET.setText("");
                            firestore.collection("Users/"+MyuserId+"/Chats").document(RecieverUId).collection("Messages")
                            .add(message)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "Message  store failed", Toast.LENGTH_SHORT).show();
                                    }else{
                                         Map<String, Object> chatdata = new HashMap<>();
                                        chatdata.put("latest_update", FieldValue.serverTimestamp());
                                        chatdata.put("ChatterId",MyuserId);
                                        firestore.collection("Users/"+RecieverUId+"/Chats").document(MyuserId).set(chatdata, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Map<String, Object> mychatdata = new HashMap<>();
                                                        mychatdata.put("latest_update", FieldValue.serverTimestamp());
                                                        mychatdata.put("ChatterId",RecieverUId);
                                                        firestore.collection("Users/"+MyuserId+"/Chats").document(RecieverUId).set(mychatdata, SetOptions.merge())
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ChatActivity.this, "plot failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, "plot failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();
       FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser == null){
            Intent loginScreenIntent = new Intent(this, LoginActivity.class);
            startActivity(loginScreenIntent);
            finish();
        }else{
            if(currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()){
                Intent signinScreenIntent = new Intent(this,InforSettings.class);
                startActivity(signinScreenIntent);
            }else{

                DocumentReference userDocRef = firestore.collection("Users").document( RecieverUId);
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            Users user = documentSnapshot.toObject(Users.class);
                            String Name = user.getName().get(0);
                            String profileImageUrl = user.getImage();
                            String profilethmb = user.getThumb();
                            setOwner(Name, profileImageUrl, profilethmb);
                        } else {
                            String e = task.getException().getMessage();
                            Toast.makeText(ChatActivity.this,e+"failed to load dp",Toast.LENGTH_LONG).show();
                        }
                    }
                });


                 mDisplayNameTV.setText(currentUser.getDisplayName());
                Uri imageUrl = currentUser.getPhotoUrl();

                if(imageUrl != null){
                    Glide.with(this)
                            .load(imageUrl)
                           .into(mProfileIV);
                }

                startListeningForMessages();
            }
        }


    }

    private void setOwner(String name, String profileImageUrl, String profilethmb) {
        mDisplayNameTV.setText(name);


        if(profileImageUrl != null || profilethmb != null){
            Glide.with(this)
                    .load(profileImageUrl)
                    .thumbnail(Glide.with(ChatActivity.this)
                            .load(profilethmb))
                    .into(mProfileIV);
        }

        startListeningForMessages();
    }

    private void startListeningForMessages() {

        firestore.collection("Users/"+MyuserId+"/Chats").document(RecieverUId).collection("Messages")
                .orderBy("dateSent")
                 .addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if(!snapshots.isEmpty()) {
                    if (e != null) {
                        //an error has occured
                        Toast.makeText(ChatActivity.this, "Couldnt recover messages", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Messages> messages = snapshots.toObjects(Messages.class);
//                            ArrayList<MessageDTO> messages = new ArrayList<>();

//                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                                if (dc.getType() == DocumentChange.Type.ADDED) {
////
//                                    messages.add(dc.getDocument().toObject(MessageDTO.class));
//
//                                }
//                            }

                        adapter.setData(messages);
                        myRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }else {
                    Toast.makeText(ChatActivity.this, "nothing here", Toast.LENGTH_LONG).show();

                }
            }
        });
    }




    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    }
