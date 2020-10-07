package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Adapter.AnotherUserRecycler;
import  com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Chats;
import  com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnotherUserAccount extends AppCompatActivity {
    private Button Ureview;

    private CircleImageView YourImage;
    private TextView Yourname,approval,approvedCounter,ApprovalCounter,gigCount;
    private TextView name2,sex,city,country,work;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String  userId;
    ArrayList <Blogpost> userBlogList;
    private ImageView textme;
    AnotherUserRecycler myRecycler;
    private  String CurrentUser;
    private DocumentSnapshot lastVisible;
    private Boolean loadFirst= true;

    //menu for start job, block, report, photos (may be)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_user_account);

        final Toolbar tb = findViewById(R.id.toolbarAccount);
        setSupportActionBar(tb);
       tb.setTitle("Users");


        RecyclerView yourposts = findViewById(R.id.Your_posts);
        yourposts.setHasFixedSize(true);
       yourposts.setNestedScrollingEnabled(false);
        YourImage = findViewById(R.id.toolfaceAnother);
        Yourname = findViewById(R.id.your_name);
        approval = findViewById(R.id.Approvaltv);
        approvedCounter = findViewById(R.id.appd1);
        ApprovalCounter =findViewById(R.id.apprscounter);
        textme = findViewById(R.id.textMe);
        Ureview = findViewById(R.id.Ureview);
        name2 = findViewById(R.id.sec_name);
        sex = findViewById(R.id.uSex);
        city =findViewById(R.id.uCity);
        country =findViewById(R.id.uCountry);
        work =findViewById(R.id.uwork);
        gigCount=findViewById(R.id.textView4);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        //get account user id
        userId = intent.getStringExtra("UserId");

        //get my id
        CurrentUser = auth.getCurrentUser().getUid();

        userBlogList = new ArrayList<>();
        myRecycler = new AnotherUserRecycler(userBlogList);


        yourposts.setLayoutManager(new LinearLayoutManager(AnotherUserAccount.this));
        yourposts.setAdapter(myRecycler);


        Ureview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviewIntent = new Intent(AnotherUserAccount.this,Reviews.class);
                reviewIntent.putExtra("UserId",userId);
                startActivity(reviewIntent);
            }
        });


        DocumentReference userDocRef = firestore.collection("Users").document(userId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    DocumentSnapshot documentSnapshot= task.getResult();
                    Users user = documentSnapshot.toObject(Users.class);
                    Yourname.setText(user.getName().get(0));
                    name2.setText(user.getName().get(1));
                    sex.setText(user.getSex());
                    country.setText(user.getCountry());
                    city.setText(user.getCity());
                    work.setText(user.getWork());

                    String profileImageUrl = user.getImage();
                    String profilethmb = user.getThumb();
                    RequestOptions placeholderOption = new RequestOptions();
                    placeholderOption.placeholder(R.drawable.ic_person);
                    Glide.with(AnotherUserAccount.this).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).thumbnail(Glide.with(AnotherUserAccount.this).load(profilethmb))
                            .into(YourImage);

                    tb.setTitle(user.getName().get(0));
                }else {
                    String e = task.getException().getMessage();
                    Toast.makeText(AnotherUserAccount.this,e+"failed",Toast.LENGTH_LONG).show();
                }
            }
        });
       yourposts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    LoadmorePosts();
                }

            }
        });
       //unsolved
        Query accountQuery = firestore.collection("Users")
                .document(userId).collection("Posts")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .limit(4);
        accountQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    if (loadFirst) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        userBlogList.clear();
                        //usersList.clear();
                    }
                    for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String docID = doc.getDocument().getId();
                            final String postID = doc.getDocument().getString("post_id");

                            firestore.collection("Posts").document(postID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            Blogpost blogPost = task.getResult().toObject(Blogpost.class).withID(postID);
                                            if (loadFirst) {
                                                // usersList.add(users);
                                                userBlogList.add(blogPost);
                                            } else {
                                                // usersList.add(0, users);
                                                userBlogList.add(0, blogPost);
                                            }
                                            // userBlogList.add(blogPost);
                                            myRecycler.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                    loadFirst = false;
                }else {
                    Toast.makeText(AnotherUserAccount.this, "nothing here", Toast.LENGTH_LONG).show();
                }
            }
        });

        firestore.collection("Users/"+userId+"/Gigs")
                .whereEqualTo("status","done")
                .whereEqualTo("to_id",userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                    gigCount.setText(Integer.toString(count));
                }else{
                    gigCount.setText(0);
                }
            }
        });

        firestore.collection("Users/"+userId+"/Approvals").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                  ApprovalCounter.setText(Integer.toString(count));

                }else{
                  ApprovalCounter.setText(0);
                }

            }
        });

        firestore.collection("Users/"+userId+"/Approved").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                    approvedCounter.setText(Integer.toString(count));

                }else{
                    approvedCounter.setText("none");
                }

            }
        });



        //getting approvals
        firestore.collection("Users/"+CurrentUser+"/Approved").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){

                    approval.setText("disapprove...");
                    approval.setBackgroundColor(Color.parseColor("#FF63D486"));
                }else{
                    approval.setText("approve...");
                    approval.setBackgroundColor(Color.parseColor("#23221F"));
                }
            }
        });

        //setting aproval
      approval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("Users/"+userId+"/Approvals").document(CurrentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                final Map<String, Object> ApprovalMap = new HashMap<>();
                               ApprovalMap.put("timestamp", FieldValue.serverTimestamp());
                                firestore.collection("Users").document(userId).collection("Approvals")
                                        .document(CurrentUser).set(ApprovalMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            firestore.collection("Users/"+CurrentUser+"/Approved").document(userId)
                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        if(!task.getResult().exists()){
                                                            Map<String, Object> ApprovedMap = new HashMap<>();
                                                            ApprovedMap.put("timestamp", FieldValue.serverTimestamp());
                                                            firestore.collection("Users").document(CurrentUser).collection("Approved").document(userId)
                                                                    .set(ApprovedMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    if (!userId.equals(CurrentUser)) {
                                                                    final String status = "Approval";
                                                                    Map<String, Object> mylikeMap = new HashMap<>();
                                                                    mylikeMap.put("status",status);
                                                                    mylikeMap.put("notId",CurrentUser);
                                                                    mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                                    mylikeMap.put("postId",null);
                                                                    firestore.collection("Users/"+userId+"/NotificationBox").add(mylikeMap)
                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                    if(!task.isSuccessful()){
                                                                                        Toast.makeText(AnotherUserAccount.this, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
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
                                                                                                       .sendNotifications(AnotherUserAccount.this,token
                                                                                                               ,status,"New approval Alert");
                                                                                           }
                                                                                       }

                                                                                   }
                                                                               });

                                                                       NotSender.Updatetoken();
                                                                     }
                                                                }
                                                            });

                                                        }else{
                                                            firestore.collection("Users")
                                                                    .document(CurrentUser).collection("Approved")
                                                                    .document(userId)
                                                                    .delete();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            } else {
                                //firestore.collection("Users/"+userId+"/NotificationBox").document(CurrentUser).delete();

                                firestore.collection("Users")
                                        .document(CurrentUser).collection("Approved").document(userId)
                                        .delete();
                                firestore.collection("Users")
                                        .document(userId).collection("Approvals").document(CurrentUser)
                                        .delete();
                            }
                        }else {
                            Toast.makeText(AnotherUserAccount.this, "your offline", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


      textme.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent message = new Intent(AnotherUserAccount.this,ChatActivity.class);
              message.putExtra("ReceiverUID",userId);
              startActivity(message);
          }
      });

    }

    private void LoadmorePosts() {
        Query Nextquery =  firestore.collection("Users")
                .document(userId).collection("Posts")
                .orderBy("timeStamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(4);
        Nextquery.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String docID = doc.getDocument().getId();
                            final String postID = doc.getDocument().getString("post_id");
                            firestore.collection("Posts").document(postID).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            Blogpost blogPost = task.getResult().toObject(Blogpost.class).withID(postID);
                                            // usersList.add(users);
                                            userBlogList.add(blogPost);
                                            // userBlogList.add(blogPost);
                                            myRecycler.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }


}