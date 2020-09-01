package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import  com.example.thehustler.Adapter.CommentsRecyclerAdaptor;
import  com.example.thehustler.Adapter.ReviewAdapter;
import  com.example.thehustler.Model.Comments;
import  com.example.thehustler.Model.ReviewModel;
import  com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Reviews extends AppCompatActivity {

    private EditText review;
    private FloatingActionButton send;

    private TextView reviewie;
    private CircleImageView reviewie_face;
    private ProgressBar sendin;
    private CardView et;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private List<ReviewModel> review_list;
    private  List<Users> user_list;
    private ReviewAdapter reviewAdapter;
    private String currentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);


        reviewie = findViewById(R.id.reviewie);//my name
        reviewie_face = findViewById(R.id.profile_image);//my face
        sendin = findViewById(R.id.rsending_progress);//progress
        review = findViewById(R.id.review_et);//msg
        send = findViewById(R.id.send_btn);//send msg
        RecyclerView posted_reviews = findViewById(R.id.review_rv);//recycler view
        sendin.setVisibility(View.INVISIBLE);

        et= findViewById(R.id.et_cardView);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        currentUID = auth.getCurrentUser().getUid();

        Intent intent = getIntent();
        final String userId = intent.getStringExtra("UserId");
        review_list = new ArrayList<>();
        user_list = new ArrayList<>();
        reviewAdapter =  new ReviewAdapter(review_list,user_list);
        posted_reviews.setHasFixedSize(true);
        posted_reviews.setLayoutManager(new LinearLayoutManager(Reviews.this));
        posted_reviews.setAdapter(reviewAdapter);
        if(userId.equals(currentUID)) {
            et.setEnabled(false);
            send.setEnabled(false);
            review.setEnabled(false);
            review.setVisibility(View.GONE);
            et.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
            firestore.collection("Users/"+currentUID +"/Reviews").orderBy("reviewdate", Query.Direction.ASCENDING)
                    .addSnapshotListener(Reviews.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        String reviewID = doc.getDocument().getId();
                                        final ReviewModel reviews = doc.getDocument().toObject(ReviewModel.class);
                                        String Reviewer_id = doc.getDocument().getString("reviewerId");
                                        firestore.collection("Users").document(Reviewer_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Users users = task.getResult().toObject(Users.class);
                                                    user_list.add(users);
                                                    review_list.add(reviews);
                                                    reviewAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                Toast.makeText(Reviews.this, "Zero Reviews", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }else {
            firestore.collection("Users/" + userId + "/Reviews").orderBy("reviewdate", Query.Direction.ASCENDING)
                    .addSnapshotListener(Reviews.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        String reviewID = doc.getDocument().getId();
                                        final ReviewModel reviews = doc.getDocument().toObject(ReviewModel.class);
                                        String Reviewer_id = doc.getDocument().getString("reviewerId");
                                        firestore.collection("Users").document(Reviewer_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Users users = task.getResult().toObject(Users.class);
                                                    user_list.add(users);
                                                    review_list.add(reviews);
                                                    reviewAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                Toast.makeText(Reviews.this, "he has Zero Reviews", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendin.setVisibility(View.VISIBLE);
                    final String rv = review.getText().toString();
                    if (!rv.isEmpty()) {


                        Map<String, Object> ansMap = new HashMap<>();
                        ansMap.put("reviewText", rv);
                        ansMap.put("reviewerId", currentUID);
                        ansMap.put("reviewdate", FieldValue.serverTimestamp());
                        firestore.collection("Users/" + userId + "/Reviews")
                                .add(ansMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    review.setText("");
                                    final String Status = "Review";
                                    Map<String, Object> myansMap = new HashMap<>();
                                    myansMap.put("status", Status);
                                    myansMap.put("notId", currentUID);
                                    myansMap.put("timestamp", FieldValue.serverTimestamp());
                                    myansMap.put("postId", null);
                                    firestore.collection("Users/" + userId + "/NotificationBox")
                                            .document(currentUID)
                                            .set(myansMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(Reviews.this, "did not properly save", Toast.LENGTH_SHORT).show();
                                                //sendin.setVisibility(View.INVISIBLE);
                                            }
                                            //send notification now
                                            firestore.collection("Users").document(userId)
                                                    .collection("Tokens")
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                            for (DocumentChange doc : value.getDocumentChanges()) {
                                                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                    String token = doc.getDocument().getString("token");
                                                                    NotSender
                                                                            .sendNotifications(Reviews.this,token
                                                                                    ,Status,"New Review Alert");
                                                                }
                                                            }

                                                        }
                                                    });

                                            NotSender.Updatetoken();






                                            sendin.setVisibility(View.INVISIBLE);
                                        }
                                    });

                                } else {
                                    Toast.makeText(Reviews.this, "did not save review", Toast.LENGTH_SHORT).show();
                                    sendin.setVisibility(View.INVISIBLE);
                                    review.setText("");
                                }
                            }
                        });

                    } else {
                        Toast.makeText(Reviews.this, "write a review", Toast.LENGTH_LONG).show();
                        sendin.setVisibility(View.INVISIBLE);
                        //   review.setText("");
                    }
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent2 = getIntent();
         String Id = intent2.getStringExtra("UserId");

        if(currentUID.equals(Id)) {
            DocumentReference userDocRef = firestore.collection("Users").document(currentUID);
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
                        Toast.makeText(Reviews.this, e + "failed to load dp", Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            DocumentReference userDocRef = firestore.collection("Users").document(Id);
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
                        Toast.makeText(Reviews.this, e + "failed to load dp", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setOwner(String name, String profileImageUrl, String profilethmb) {
        reviewie.setText(name);

        if(profileImageUrl != null || profilethmb != null){
            Glide.with(this)
                    .load(profileImageUrl)
                    .thumbnail(Glide.with(Reviews.this)
                            .load(profilethmb))
                    .into(reviewie_face);
        }

    }


}