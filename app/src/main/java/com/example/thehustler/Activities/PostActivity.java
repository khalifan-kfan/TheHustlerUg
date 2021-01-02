package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Adapter.CommentsRecyclerAdaptor;
import  com.example.thehustler.Adapter.ImageAdp;
import  com.example.thehustler.Fragments.Home_Fragment;
import  com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Comments;
import  com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore firestore;


    List<Blogpost> blog = new ArrayList<>();
    List<Users> user = new ArrayList<>();
    private CircleImageView userProfileImageView;
    private TextView userNameView;
    private TextView dateView;

    private ImageView popbut;
    private TextView descView;
    private ViewPager2 blogImageView;

    private TextView commentsView,commentsNumber;
    private ImageView likeBtn;
    private TextView likeCounter;
    private String userId;
    private  String blogPostId;


    private EditText Answer;
    private FloatingActionButton Sendbtn;
    private RecyclerView Answerslists;
    private List<Comments> answers_list;
    private  List<Users> user_list;
    private CommentsRecyclerAdaptor commentsRecyclerAdaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final Toolbar tb = findViewById(R.id.toolbar_post_);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //tb
        //tb.setTitle("Comments");

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        final String currentUserId = auth.getCurrentUser().getUid();
        Intent intent = getIntent();
        userId = intent.getStringExtra("UserId");
        blogPostId = intent.getStringExtra("blogPostId");
        //final String blogPostId = getArguments().getString("blogPostId");
        //final String userId = getArguments().getString("userId");


        Answerslists = findViewById(R.id.answersrecyclerview);
        Answer= findViewById(R.id.answersET);
        Sendbtn = findViewById(R.id.sendbtn);

        userProfileImageView =findViewById(R.id.circularUser);
        userNameView = findViewById(R.id.Post_user);
        dateView = findViewById(R.id.datePost);
        popbut = findViewById(R.id.popimagedell);
        blogImageView = findViewById(R.id.pageview);
        descView = findViewById(R.id.postdescribe);
        commentsView = findViewById(R.id.Comentcount2);
        commentsNumber =findViewById(R.id.commentCountR);
        likeBtn = findViewById(R.id.likeuserz2);
        likeCounter =findViewById(R.id.likescount23);




        if(userId.equals(currentUserId)) {
            popbut.setEnabled(true);
            popbut.setVisibility(View.VISIBLE);
            userProfileImageView.setEnabled(false);
        }
        userProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent account = new Intent(PostActivity.this,AnotherUserAccount.class);
                account.putExtra("UserId",userId);
                startActivity(account);
            }
        });


        commentsView.setText("comments");


        firestore.collection("Posts/"+blogPostId+"/Answers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                    commentsNumber.setText(Integer.toString(count));

                }else{
                    commentsNumber.setText("");
                }

            }
        });


        firestore.collection("Posts/"+blogPostId+"/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    int counter = queryDocumentSnapshots.size();
                    likeCounter.setText(Integer.toString(counter));
                } else {
                    likeCounter.setText(" ");
                }
            }
        });

        firestore.collection("Posts/"+blogPostId+"/likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    likeBtn.setImageDrawable(PostActivity.this.getDrawable(R.drawable.ic_nfava));
                } else {
                    likeBtn.setImageDrawable(PostActivity.this.getDrawable(R.drawable.ic_fava));
                }
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Posts/"+blogPostId+"/likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> likeMap = new HashMap<>();
                                likeMap.put("timestamp", FieldValue.serverTimestamp());
                                firestore.collection("Posts/"+blogPostId+"/likes").document(currentUserId).set(likeMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!userId.equals(currentUserId)) {
                                                    final String status = "like";
                                                    Map<String, Object> mylikeMap = new HashMap<>();
                                                    mylikeMap.put("status",status);
                                                    mylikeMap.put("notId",currentUserId);
                                                    mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                    mylikeMap.put("postId",blogPostId);
                                                    mylikeMap.put("mark","unseen");
                                                    firestore.collection("Users/"+userId+"/NotificationBox")
                                                            .add(mylikeMap)
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                    if(!task.isSuccessful()){
                                                                        Toast.makeText(PostActivity.this, "did not properly liked", Toast.LENGTH_SHORT).show();
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
                                                                                                    .sendNotifications(PostActivity.this,token
                                                                                                            ,status,"New Like Alert");
                                                                                        }
                                                                                    }

                                                                                }
                                                                            });

                                                                    NotSender.Updatetoken();

                                                                }
                                                            });
                                                }
                                            }
                                        });


                            } else {
                                firestore.collection("Posts/"+blogPostId+"/likes").document(currentUserId).delete();
                            }
                        }else {
                            Toast.makeText(PostActivity.this, "your offline", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        DocumentReference userDocRef = firestore.collection("Users").document(userId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    user.add(0,null);
                    DocumentSnapshot documentSnapshot= task.getResult();
                    user.set(0, documentSnapshot.toObject(Users.class));

                    String Name = user.get(0).getName().get(0);
                    String profileImageUrl = user.get(0).getImage();
                    String profilethmb = user.get(0).getThumb();
                    setUserData(Name, profileImageUrl, profilethmb);
                }else {
                    String e = task.getException().getMessage();
                    Toast.makeText(PostActivity.this,e+"failed",Toast.LENGTH_LONG).show();
                }
            }
        });


        final DocumentReference blogDocRef = firestore.collection("Posts").document(blogPostId);
        blogDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                blog.add(0,null);
                blog.set(0, documentSnapshot.toObject(Blogpost.class));

                try {
                    long millisecond = blog.get(0).getTimeStamp().getTime();
                    String blogDate = DateFormat.format("d/MM/yyyy", new Date(millisecond)).toString();
                    dateView.setText(blogDate);
                } catch (Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if ((blog.get(0).getImage_url())!=null && (blog.get(0).getPost_image_thumb())!=null) {
                    List<String> Image_uri = new ArrayList<>();
                    List<String> thumb_uri = new ArrayList<>();
                    int i = 0;
                    while (i < blog.get(0).getImage_url().size()) {
                        Image_uri.add(blog.get(0).getImage_url().get(i));
                        thumb_uri.add(blog.get(0).getPost_image_thumb().get(i));
                        i++;
                    }


                    setBlogData(Image_uri,thumb_uri);
                }else {
                    blogImageView.setEnabled(false);
                    blogImageView.setVisibility(View.GONE);
                }
                String blogDesc = blog.get(0).getDescription();
                descView.setText(blogDesc);
            }
        });


        popbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop(userId, blog, user,0);

            }
        });

        // recycler view fire base list
        answers_list =new ArrayList<>();
        user_list = new ArrayList<>();
        commentsRecyclerAdaptor =  new CommentsRecyclerAdaptor(answers_list,user_list);
        Answerslists.setHasFixedSize(true);
        Answerslists.setLayoutManager(new LinearLayoutManager(PostActivity.this));
        Answerslists.setAdapter(commentsRecyclerAdaptor);
        firestore.collection("Posts/"+blogPostId+"/Answers").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(PostActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentsID = doc.getDocument().getId();
                                    final Comments comments = doc.getDocument().toObject(Comments.class).withID(commentsID);
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
                    Answer.setText("");
                    Map<String, Object> ansMap = new HashMap<>();
                    ansMap.put("answer",ans);
                    ansMap.put("user_id",currentUserId);
                    ansMap.put("timestamp", FieldValue.serverTimestamp());
                    firestore.collection("Posts/"+blogPostId+"/Answers").add(ansMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Answer.setText("");
                                final String Status ="Comment";
                                Map<String, Object> myansMap = new HashMap<>();
                                myansMap.put("status", Status);
                                myansMap.put("notId",currentUserId);
                                myansMap.put("timestamp", FieldValue.serverTimestamp());
                                myansMap.put("postId",blogPostId);
                                myansMap.put("mark","unseen");
                                firestore.collection("Users/"+userId+"/NotificationBox")
                                        .add(myansMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if(!task.isSuccessful()) {
                                            Toast.makeText(PostActivity.this, "did not properly save", Toast.LENGTH_SHORT).show();
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
                                                                        .sendNotifications(PostActivity.this,token
                                                                                ,Status,"New comment Alert");
                                                            }
                                                        }

                                                    }
                                                });
                                        NotSender.Updatetoken();
                                    }
                                });
                            }else {
                                Toast.makeText(PostActivity.this, "did not save post", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }

    private void setBlogData(List<String> blogImageUrl, List<String> blogImageThumbUrl) {

        blogImageView.setAdapter( new ImageAdp(blogImageUrl,blogImageThumbUrl,blogImageView));

        blogImageView.setClipToPadding(false);
        blogImageView.setClipChildren(false);
        blogImageView.setOffscreenPageLimit(3);
        blogImageView.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer cpt = new CompositePageTransformer();
        cpt.addTransformer(new MarginPageTransformer(40));
        cpt.addTransformer((new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1-Math.abs(position);
                page.setScaleY(0.95F+r*0.15F);
            }

        }
        ));
        blogImageView.setPageTransformer(cpt);


        // Glide.with(this)
        //       .load(blogImageUrl)
        //     .thumbnail(Glide.with(this).load(blogImageThumbUrl))
        //   .into(blogImageView);


    }

    private void setUserData(String name, String thumb, String profileImageUrl) {
        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.ic_person);
        Glide.with(this).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl)

                .into(userProfileImageView);
        userNameView.setText(name);

    }
    public  void pop(final String user_id, final List <Blogpost> postlist, final List <Users> users, final int i ){
        PopupMenu popupMenu = new PopupMenu(PostActivity.this,popbut);
        popupMenu.getMenuInflater().inflate(R.menu.popdown, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                firestore.collection("Posts").document(user_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(PostActivity.this, "deleteted....",Toast.LENGTH_SHORT).show();
                        postlist.remove(i);
                        users.remove(i);

                        AppCompatActivity activity = (AppCompatActivity)PostActivity.this;
                        Home_Fragment homeFragment = new Home_Fragment();
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, homeFragment).addToBackStack(null).commit();

                    }
                });

                return true;
            }
        });
        popupMenu.show();
    }

}
