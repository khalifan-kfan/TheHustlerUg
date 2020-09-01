package com.example.thehustler.Activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import  com.example.thehustler.Adapter.CommentsRecyclerAdaptor;
import  com.example.thehustler.Model.Blogpost;
import  com.example.thehustler.Model.Comments;
import  com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private Toolbar answersToolbar;
    private EditText Answer;
    private ImageView Sendbtn;
    private String PostID;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private RecyclerView Answerslists;

    private List<Comments> answers_list;
    private  List<Users> user_list;
    private CommentsRecyclerAdaptor commentsRecyclerAdaptor;
    private  String PostOwnerId;

    private  String CurrentUser_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        answersToolbar = findViewById(R.id.toolbar_answers);
        setSupportActionBar(answersToolbar);
        getSupportActionBar().setTitle("Answers");



        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        CurrentUser_id = auth.getCurrentUser().getUid();

        PostID = getIntent().getStringExtra("post_Id");
        PostOwnerId = getIntent().getStringExtra("postownerId");

        Answerslists = findViewById(R.id.answersrecyclerview);
        Answer= findViewById(R.id.answersET);
        Sendbtn = findViewById(R.id.sendbtn);


        // recycler view fire base list
        answers_list =new ArrayList<>();
        user_list = new ArrayList<>();
        commentsRecyclerAdaptor =  new CommentsRecyclerAdaptor(answers_list,user_list);
        Answerslists.setHasFixedSize(true);
        Answerslists.setLayoutManager(new LinearLayoutManager(CommentActivity.this));
        Answerslists.setAdapter(commentsRecyclerAdaptor);

        firestore.collection("Posts/"+PostID+"/Answers").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(CommentActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentsID = doc.getDocument().getId();
                                    final Comments comments = doc.getDocument().toObject(Comments.class);
                                    String Commentuser_id = doc.getDocument().getString("user_id");
                                    firestore.collection("Users").document(Commentuser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Users users = task.getResult().toObject(Users.class);

                                                user_list.add(users);
                                                answers_list.add(comments);

                                                commentsRecyclerAdaptor.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
        Sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String ans =Answer.getText().toString();
                if(!ans.isEmpty()){
                    Map<String, Object> ansMap = new HashMap<>();
                    ansMap.put("answer",ans);
                    ansMap.put("user_id",CurrentUser_id);
                    ansMap.put("timestamp", FieldValue.serverTimestamp());
                    firestore.collection("Posts/"+PostID+"/Answers").add(ansMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Answer.setText("");
                                final String Status ="Comment";
                                Map<String, Object> myansMap = new HashMap<>();
                                myansMap.put("status", Status);
                                myansMap.put("notId",CurrentUser_id);
                                myansMap.put("timestamp", FieldValue.serverTimestamp());
                                myansMap.put("postId",PostID);
                                firestore.collection("Users/"+PostOwnerId+"/NotificationBox")
                                        .document(CurrentUser_id)
                                        .set(myansMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(CommentActivity.this, "did not properly save", Toast.LENGTH_SHORT).show();
                                        }
                                        //send notification now
                                        firestore.collection("Users").document(PostOwnerId)
                                                .collection("Tokens")
                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                        for (DocumentChange doc : value.getDocumentChanges()) {
                                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                String token = doc.getDocument().getString("token");
                                                                NotSender
                                                                        .sendNotifications(CommentActivity.this,token
                                                                                ,Status,"New comment Alert");
                                                            }
                                                        }

                                                    }
                                                });

                                        NotSender.Updatetoken();


                                    }
                                });

                            }else {
                                Toast.makeText(CommentActivity.this, "did not save post", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}